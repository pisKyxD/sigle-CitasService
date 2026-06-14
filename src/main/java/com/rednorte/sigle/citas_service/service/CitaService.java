package com.rednorte.sigle.citas_service.service;

import com.rednorte.sigle.citas_service.repository.CancelacionRepository;
import com.rednorte.sigle.citas_service.repository.CitaRepository;
import com.rednorte.sigle.citas_service.config.RabbitMQConfig;
import com.rednorte.sigle.citas_service.model.Cancelacion;
import com.rednorte.sigle.citas_service.model.CanceladoPor;
import com.rednorte.sigle.citas_service.model.Cita;
import com.rednorte.sigle.citas_service.model.EstadoCita;
import com.rednorte.sigle.citas_service.model.Medico;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CitaService {

    private final CitaRepository citaRepository;
    private final CancelacionRepository cancelacionRepository;
    private final RabbitTemplate rabbitTemplate;
    private final MedicoService medicoService;

    @CircuitBreaker(name = "citasService", fallbackMethod = "fallbackGetAll")
    public List<Cita> getAll() {
        return citaRepository.findAll();
    }

    public List<Cita> fallbackGetAll(Exception e) {
        return List.of();
    }

    @CircuitBreaker(name = "citasService", fallbackMethod = "fallbackGetById")
    public Cita getById(Long id) {
        return citaRepository.findById(id).orElseThrow(() -> new RuntimeException("Cita no encontrada"));
    }

    public Cita fallbackGetById(Long id, Exception e) {
        throw new RuntimeException("Servicio de citas no disponible temporalmente");
    }

    @CircuitBreaker(name = "citasService", fallbackMethod = "fallbackGetByPacienteId")
    public List<Cita> getByPacienteId(Long pacienteId) {
        return citaRepository.findByPacienteId(pacienteId);
    }

    public List<Cita> fallbackGetByPacienteId(Long pacienteId, Exception e) {
        return List.of();
    }

    @CircuitBreaker(name = "citasService", fallbackMethod = "fallbackGetByPacienteIdPaginado")
    public Page<Cita> getByPacienteIdPaginado(Long pacienteId, Pageable pageable) {
        return citaRepository.findByPacienteId(pacienteId, pageable);
    }

    public Page<Cita> fallbackGetByPacienteIdPaginado(Long pacienteId, Pageable pageable, Exception e) {
        return Page.empty();
    }

    @CircuitBreaker(name = "citasService", fallbackMethod = "fallbackGetByMedicoId")
    public List<Cita> getByMedicoId(Long medicoId) {
        return citaRepository.findByMedicoId(medicoId);
    }

    public List<Cita> fallbackGetByMedicoId(Long medicoId, Exception e) {
        return List.of();
    }

    @CircuitBreaker(name = "citasService", fallbackMethod = "fallbackGetHorasOcupadas")
    public List<String> getHorasOcupadas(Long medicoId, LocalDate fecha) {
        LocalDateTime inicio = fecha.atStartOfDay();
        LocalDateTime fin = fecha.atTime(LocalTime.MAX);
        List<Cita> citas = citaRepository.findByMedicoIdAndFechaHoraBetweenAndEstadoNot(
            medicoId, inicio, fin, EstadoCita.CANCELADA);
        return citas.stream()
            .map(c -> c.getFechaHora().toLocalTime().toString())
            .collect(Collectors.toList());
    }

    public List<String> fallbackGetHorasOcupadas(Long medicoId, LocalDate fecha, Exception e) {
        return List.of();
    }

    @Transactional
    @CircuitBreaker(name = "citasService", fallbackMethod = "fallbackAgendarCita")
    public Cita agendarCita(Cita nuevaCita, Long medicoId) {
        Medico medico = medicoService.getById(medicoId);
        nuevaCita.setMedico(medico);
        nuevaCita.setEstado(EstadoCita.PROGRAMADA);
        return citaRepository.save(nuevaCita);
    }

    public Cita fallbackAgendarCita(Cita nuevaCita, Long medicoId, Exception e) {
        throw new RuntimeException("No se puede agendar la cita. Servicio no disponible temporalmente.");
    }

    @Transactional
    @CircuitBreaker(name = "citasService", fallbackMethod = "fallbackUpdate")
    public Cita update(Long id, Cita data, Long medicoId) {
        Cita existing = getById(id);
        if (medicoId != null) {
            existing.setMedico(medicoService.getById(medicoId));
        }
        existing.setEspecialidad(data.getEspecialidad());
        existing.setFechaHora(data.getFechaHora());
        existing.setEstado(data.getEstado());
        return citaRepository.save(existing);
    }

    public Cita fallbackUpdate(Long id, Cita data, Long medicoId, Exception e) {
        throw new RuntimeException("No se puede actualizar la cita. Servicio no disponible temporalmente.");
    }

    @Transactional
    @CircuitBreaker(name = "citasService", fallbackMethod = "fallbackCancelarCita")
    public Cancelacion cancelarCita(Long idCita, String motivo, CanceladoPor por) {
        Cita cita = getById(idCita);

        cita.setEstado(EstadoCita.CANCELADA);
        citaRepository.save(cita);

        Cancelacion cancelacion = Cancelacion.builder()
                .cita(cita)
                .motivo(motivo)
                .canceladoPor(por)
                .fechaCancelacion(LocalDateTime.now())
                .reasignado(false)
                .build();

        cancelacionRepository.save(cancelacion);

        CancelacionEvento evento = new CancelacionEvento();
        evento.setCitaId(cita.getId());
        evento.setPacienteId(cita.getPacienteId());
        evento.setListaEsperaId(cita.getListaEsperaId());
        evento.setMotivo(motivo);

        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.CANCELACION_ROUTING_KEY, evento);

        return cancelacion;
    }

    public Cancelacion fallbackCancelarCita(Long idCita, String motivo, CanceladoPor por, Exception e) {
        throw new RuntimeException("No se puede cancelar la cita. Servicio no disponible temporalmente.");
    }

    public void delete(Long id) {
        citaRepository.deleteById(id);
    }

    @Data
    public static class CancelacionEvento {
        private Long citaId;
        private Long pacienteId;
        private Long listaEsperaId;
        private String motivo;
    }
}
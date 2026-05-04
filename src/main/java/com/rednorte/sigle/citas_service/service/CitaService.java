package com.rednorte.sigle.citas_service.service;

import com.rednorte.sigle.citas_service.repository.CancelacionRepository;
import com.rednorte.sigle.citas_service.repository.CitaRepository;
import com.rednorte.sigle.citas_service.config.RabbitMQConfig;
import com.rednorte.sigle.citas_service.model.Cancelacion;
import com.rednorte.sigle.citas_service.model.CanceladoPor;
import com.rednorte.sigle.citas_service.model.Cita;
import com.rednorte.sigle.citas_service.model.EstadoCita;
import com.rednorte.sigle.citas_service.model.Medico;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CitaService {

    private final CitaRepository citaRepository;
    private final CancelacionRepository cancelacionRepository;
    private final RabbitTemplate rabbitTemplate;
    private final MedicoService medicoService;

    public List<Cita> getAll() {
        return citaRepository.findAll();
    }

    public Cita getById(Long id) {
        return citaRepository.findById(id).orElseThrow(() -> new RuntimeException("Cita no encontrada"));
    }

    public List<Cita> getByPacienteId(Long pacienteId) {
        return citaRepository.findByPacienteId(pacienteId);
    }

    @Transactional
    public Cita agendarCita(Cita nuevaCita, Long medicoId) {
        Medico medico = medicoService.getById(medicoId);
        nuevaCita.setMedico(medico);
        nuevaCita.setEstado(EstadoCita.PROGRAMADA);
        return citaRepository.save(nuevaCita);
    }

    @Transactional
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

    @Transactional
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

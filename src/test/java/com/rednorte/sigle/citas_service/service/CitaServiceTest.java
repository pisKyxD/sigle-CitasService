package com.rednorte.sigle.citas_service.service;

import com.rednorte.sigle.citas_service.model.Cita;
import com.rednorte.sigle.citas_service.model.EstadoCita;
import com.rednorte.sigle.citas_service.model.Medico;
import com.rednorte.sigle.citas_service.repository.CancelacionRepository;
import com.rednorte.sigle.citas_service.repository.CitaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CitaService")
class CitaServiceTest {

    @Mock private CitaRepository citaRepository;
    @Mock private CancelacionRepository cancelacionRepository;
    @Mock private RabbitTemplate rabbitTemplate;
    @Mock private MedicoService medicoService;

    @InjectMocks
    private CitaService citaService;

    private Cita citaEjemplo;
    private Medico medicoEjemplo;

    @BeforeEach
    void setUp() {
        medicoEjemplo = new Medico();
        medicoEjemplo.setId(1L);

        citaEjemplo = Cita.builder()
            .id(1L)
            .pacienteId(100L)
            .medico(medicoEjemplo)   // ← objeto Medico
            .especialidad("Cardiología")
            .fechaHora(LocalDateTime.now().plusDays(1))
            .estado(EstadoCita.PROGRAMADA)
            .build();
    }

    @Test
    @DisplayName("getAll retorna lista de citas")
    void getAll_retornaListaCitas() {
        when(citaRepository.findAll()).thenReturn(List.of(citaEjemplo));
        List<Cita> resultado = citaService.getAll();
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getEspecialidad()).isEqualTo("Cardiología");
    }

    @Test
    @DisplayName("getById retorna cita cuando existe")
    void getById_retornaCita_cuandoExiste() {
        when(citaRepository.findById(1L)).thenReturn(Optional.of(citaEjemplo));
        Cita resultado = citaService.getById(1L);
        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getPacienteId()).isEqualTo(100L);
    }

    @Test
    @DisplayName("getById lanza excepción cuando no existe")
    void getById_lanzaExcepcion_cuandoNoExiste() {
        when(citaRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> citaService.getById(99L))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Cita no encontrada");
    }

    @Test
    @DisplayName("getByPacienteId retorna citas del paciente")
    void getByPacienteId_retornaCitasPaciente() {
        when(citaRepository.findByPacienteId(100L)).thenReturn(List.of(citaEjemplo));
        List<Cita> resultado = citaService.getByPacienteId(100L);
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getPacienteId()).isEqualTo(100L);
    }

    @Test
    @DisplayName("agendarCita guarda cita con estado PROGRAMADA")
    void agendarCita_guardaCita_conEstadoProgramada() {
        Cita nuevaCita = Cita.builder()
            .pacienteId(200L)
            .especialidad("Neurología")
            .fechaHora(LocalDateTime.now().plusDays(2))
            .build();

        when(medicoService.getById(1L)).thenReturn(medicoEjemplo);
        when(citaRepository.save(any(Cita.class))).thenAnswer(i -> i.getArgument(0));

        Cita resultado = citaService.agendarCita(nuevaCita, 1L);

        assertThat(resultado.getEstado()).isEqualTo(EstadoCita.PROGRAMADA);
        assertThat(resultado.getMedico()).isEqualTo(medicoEjemplo);
        verify(citaRepository, times(1)).save(any(Cita.class));
    }

    @Test
    @DisplayName("delete elimina cita por id")
    void delete_eliminaCita() {
        doNothing().when(citaRepository).deleteById(1L);
        citaService.delete(1L);
        verify(citaRepository, times(1)).deleteById(1L);
    }
}
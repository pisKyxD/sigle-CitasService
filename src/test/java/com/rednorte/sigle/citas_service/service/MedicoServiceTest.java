package com.rednorte.sigle.citas_service.service;

import com.rednorte.sigle.citas_service.model.Medico;
import com.rednorte.sigle.citas_service.repository.MedicoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MedicoService")
class MedicoServiceTest {

    @Mock private MedicoRepository repository;
    @InjectMocks private MedicoService service;

    private Medico medicoEjemplo;

    @BeforeEach
    void setUp() {
        medicoEjemplo = Medico.builder()
            .id(1L)
            .rut("12345678-9")
            .nombre("Dr. Pérez")
            .especialidad("Cardiología")
            .establecimientoId(1L)
            .activo(true)
            .build();
    }

    @Test
    @DisplayName("getAll retorna lista de médicos")
    void getAll_retornaLista() {
        when(repository.findAll()).thenReturn(List.of(medicoEjemplo));
        List<Medico> resultado = service.getAll();
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getNombre()).isEqualTo("Dr. Pérez");
    }

    @Test
    @DisplayName("getById retorna médico cuando existe")
    void getById_retornaMedico() {
        when(repository.findById(1L)).thenReturn(Optional.of(medicoEjemplo));
        Medico resultado = service.getById(1L);
        assertThat(resultado.getRut()).isEqualTo("12345678-9");
    }

    @Test
    @DisplayName("getById lanza excepción cuando no existe")
    void getById_lanzaExcepcion() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getById(99L))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Médico no encontrado");
    }

    @Test
    @DisplayName("create guarda médico correctamente")
    void create_guardaMedico() {
        when(repository.save(any(Medico.class))).thenReturn(medicoEjemplo);
        Medico resultado = service.create(medicoEjemplo);
        assertThat(resultado.getNombre()).isEqualTo("Dr. Pérez");
        verify(repository, times(1)).save(any());
    }

    @Test
    @DisplayName("update actualiza médico correctamente")
    void update_actualizaMedico() {
        Medico datos = Medico.builder()
            .rut("12345678-9")
            .nombre("Dr. González")
            .especialidad("Neurología")
            .establecimientoId(2L)
            .activo(true)
            .build();

        when(repository.findById(1L)).thenReturn(Optional.of(medicoEjemplo));
        when(repository.save(any(Medico.class))).thenAnswer(i -> i.getArgument(0));

        Medico resultado = service.update(1L, datos);

        assertThat(resultado.getNombre()).isEqualTo("Dr. González");
        assertThat(resultado.getEspecialidad()).isEqualTo("Neurología");
    }

    @Test
    @DisplayName("delete elimina médico por id")
    void delete_eliminaMedico() {
        doNothing().when(repository).deleteById(1L);
        service.delete(1L);
        verify(repository, times(1)).deleteById(1L);
    }
}
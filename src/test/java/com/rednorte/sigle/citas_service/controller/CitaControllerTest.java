package com.rednorte.sigle.citas_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rednorte.sigle.citas_service.model.Cita;
import com.rednorte.sigle.citas_service.model.EstadoCita;
import com.rednorte.sigle.citas_service.service.CitaService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CitaController.class)
@DisplayName("CitaController")
class CitaControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean CitaService citaService;

    @Test
    @DisplayName("GET /api/citas retorna 200 con lista")
    void getAll_returns200() throws Exception {
        Cita cita = Cita.builder()
            .id(1L).pacienteId(100L)
            .especialidad("Cardiología")
            .estado(EstadoCita.PROGRAMADA)
            .fechaHora(LocalDateTime.now().plusDays(1))
            .build();

        when(citaService.getAll()).thenReturn(List.of(cita));

        mockMvc.perform(get("/api/citas"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].especialidad").value("Cardiología"));
    }

    @Test
    @DisplayName("GET /api/citas/{id} retorna 200 cuando existe")
    void getById_returns200() throws Exception {
        Cita cita = Cita.builder().id(1L).pacienteId(100L).especialidad("Cardiología").build();
        when(citaService.getById(1L)).thenReturn(cita);

        mockMvc.perform(get("/api/citas/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("GET /api/citas/paciente/{id} retorna citas del paciente")
    void getByPacienteId_returns200() throws Exception {
        Cita cita = Cita.builder().id(1L).pacienteId(100L).especialidad("Neurología").build();
        when(citaService.getByPacienteId(100L)).thenReturn(List.of(cita));

        mockMvc.perform(get("/api/citas/paciente/100"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].pacienteId").value(100));
    }

    @Test
    @DisplayName("POST /api/citas/agendar retorna 200 con cita creada")
    void agendarCita_returns200() throws Exception {
        Cita cita = Cita.builder()
            .pacienteId(100L)
            .especialidad("Cardiología")
            .fechaHora(LocalDateTime.now().plusDays(1))
            .build();

        Map<String, Object> body = Map.of("cita", cita, "medicoId", 1L);

        Cita citaGuardada = Cita.builder().id(1L).pacienteId(100L)
            .especialidad("Cardiología").estado(EstadoCita.PROGRAMADA).build();

        when(citaService.agendarCita(any(Cita.class), anyLong())).thenReturn(citaGuardada);

        mockMvc.perform(post("/api/citas/agendar")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(body)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.estado").value("PROGRAMADA"));
    }

    @Test
    @DisplayName("DELETE /api/citas/{id} retorna 200")
    void delete_returns200() throws Exception {
        mockMvc.perform(delete("/api/citas/1"))
            .andExpect(status().isOk());
    }
}
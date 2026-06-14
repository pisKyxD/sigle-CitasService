package com.rednorte.sigle.citas_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rednorte.sigle.citas_service.model.Medico;
import com.rednorte.sigle.citas_service.service.MedicoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MedicoController.class)
@DisplayName("MedicoController")
class MedicoControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean MedicoService service;

    @Test
    @DisplayName("GET /api/citas/medicos retorna 200 con lista")
    void getAll_returns200() throws Exception {
        Medico medico = Medico.builder()
            .id(1L).rut("12345678-9")
            .nombre("Dr. Pérez").especialidad("Cardiología")
            .establecimientoId(1L).activo(true).build();

        when(service.getAll()).thenReturn(List.of(medico));

        mockMvc.perform(get("/api/citas/medicos"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].nombre").value("Dr. Pérez"));
    }

    @Test
    @DisplayName("GET /api/citas/medicos/{id} retorna 200 cuando existe")
    void getById_returns200() throws Exception {
        Medico medico = Medico.builder().id(1L).nombre("Dr. Pérez").rut("12345678-9")
            .especialidad("Cardiología").establecimientoId(1L).build();

        when(service.getById(1L)).thenReturn(medico);

        mockMvc.perform(get("/api/citas/medicos/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("POST /api/citas/medicos retorna 200 con médico creado")
    void create_returns200() throws Exception {
        Medico medico = Medico.builder()
            .rut("12345678-9").nombre("Dr. Pérez")
            .especialidad("Cardiología").establecimientoId(1L).build();

        Medico creado = Medico.builder().id(1L)
            .rut("12345678-9").nombre("Dr. Pérez")
            .especialidad("Cardiología").establecimientoId(1L).build();

        when(service.create(any(Medico.class))).thenReturn(creado);

        mockMvc.perform(post("/api/citas/medicos")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(medico)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("PUT /api/citas/medicos/{id} retorna 200 con médico actualizado")
    void update_returns200() throws Exception {
        Medico datos = Medico.builder()
            .rut("12345678-9").nombre("Dr. González")
            .especialidad("Neurología").establecimientoId(2L).build();

        Medico actualizado = Medico.builder().id(1L)
            .rut("12345678-9").nombre("Dr. González")
            .especialidad("Neurología").establecimientoId(2L).build();

        when(service.update(anyLong(), any(Medico.class))).thenReturn(actualizado);

        mockMvc.perform(put("/api/citas/medicos/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(datos)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nombre").value("Dr. González"));
    }

    @Test
    @DisplayName("DELETE /api/citas/medicos/{id} retorna 200")
    void delete_returns200() throws Exception {
        mockMvc.perform(delete("/api/citas/medicos/1"))
            .andExpect(status().isOk());
    }
}
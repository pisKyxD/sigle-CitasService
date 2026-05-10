package com.rednorte.sigle.citas_service.controller;

import com.rednorte.sigle.citas_service.model.Cancelacion;
import com.rednorte.sigle.citas_service.model.CanceladoPor;
import com.rednorte.sigle.citas_service.model.Cita;
import com.rednorte.sigle.citas_service.service.CitaService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/citas")
@RequiredArgsConstructor
public class CitaController {

    private final CitaService citaService;

    @GetMapping
    public ResponseEntity<List<Cita>> getAll() {
        return ResponseEntity.ok(citaService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cita> getById(@PathVariable Long id) {
        return ResponseEntity.ok(citaService.getById(id));
    }

    @GetMapping("/paciente/{pacienteId}")
    public ResponseEntity<List<Cita>> getByPacienteId(@PathVariable Long pacienteId) {
        return ResponseEntity.ok(citaService.getByPacienteId(pacienteId));
    }

    @GetMapping("/medico/{medicoId}/horas-ocupadas")
    public ResponseEntity<List<String>> getHorasOcupadas(
            @PathVariable Long medicoId,
            @RequestParam String fecha) {
        return ResponseEntity.ok(citaService.getHorasOcupadas(medicoId, LocalDate.parse(fecha)));
    }

    @PostMapping("/agendar")
    public ResponseEntity<Cita> agendarCita(@Valid @RequestBody AgendarRequest r) {
        return ResponseEntity.ok(citaService.agendarCita(r.getCita(), r.getMedicoId()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Cita> update(@PathVariable Long id, @Valid @RequestBody AgendarRequest r) {
        return ResponseEntity.ok(citaService.update(id, r.getCita(), r.getMedicoId()));
    }

    @PostMapping("/{id}/cancelar")
    public ResponseEntity<Cancelacion> cancelarCita(@PathVariable Long id, @Valid @RequestBody CancelacionRequest request) {
        Cancelacion cancelacion = citaService.cancelarCita(id, request.getMotivo(), request.getCanceladoPor());
        return ResponseEntity.ok(cancelacion);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        citaService.delete(id);
        return ResponseEntity.ok().build();
    }

    @Data
    public static class CancelacionRequest {
        @NotBlank(message = "El motivo es obligatorio")
        private String motivo;

        @NotNull(message = "Debe indicar quién cancela")
        private CanceladoPor canceladoPor;
    }

    @Data
    public static class AgendarRequest {
        @NotNull(message = "La cita es obligatoria")
        private Cita cita;

        @NotNull(message = "El médico es obligatorio")
        private Long medicoId;
    }
}
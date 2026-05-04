package com.rednorte.sigle.citas_service.controller;

import com.rednorte.sigle.citas_service.model.Cancelacion;
import com.rednorte.sigle.citas_service.model.CanceladoPor;
import com.rednorte.sigle.citas_service.model.Cita;
import com.rednorte.sigle.citas_service.service.CitaService;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/agendar")
    public ResponseEntity<Cita> agendarCita(@RequestBody AgendarRequest r) {
        return ResponseEntity.ok(citaService.agendarCita(r.getCita(), r.getMedicoId()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Cita> update(@PathVariable Long id, @RequestBody AgendarRequest r) {
        return ResponseEntity.ok(citaService.update(id, r.getCita(), r.getMedicoId()));
    }

    @PostMapping("/{id}/cancelar")
    public ResponseEntity<Cancelacion> cancelarCita(@PathVariable Long id, @RequestBody CancelacionRequest request) {
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
        private String motivo;
        private CanceladoPor canceladoPor;
    }

    @Data
    public static class AgendarRequest {
        private Cita cita;
        private Long medicoId;
    }
}

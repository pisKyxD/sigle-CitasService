package com.rednorte.sigle.citas_service.controller;

import com.rednorte.sigle.citas_service.model.Medico;
import com.rednorte.sigle.citas_service.service.MedicoService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/citas/medicos")
@RequiredArgsConstructor
public class MedicoController {

    private final MedicoService service;

    @GetMapping
    public ResponseEntity<List<Medico>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Medico> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PostMapping
    public ResponseEntity<Medico> create(@RequestBody Medico m) {
        return ResponseEntity.ok(service.create(m));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Medico> update(@PathVariable Long id, @RequestBody Medico m) {
        return ResponseEntity.ok(service.update(id, m));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok().build();
    }
}

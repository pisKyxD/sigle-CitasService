package com.rednorte.sigle.citas_service.service;

import com.rednorte.sigle.citas_service.model.Medico;
import com.rednorte.sigle.citas_service.repository.MedicoRepository;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MedicoService {

    private final MedicoRepository repository;

    @CircuitBreaker(name = "citasService", fallbackMethod = "fallbackGetAll")
    public List<Medico> getAll() {
        return repository.findAll();
    }

    public List<Medico> fallbackGetAll(Exception e) {
        return List.of();
    }

    @CircuitBreaker(name = "citasService", fallbackMethod = "fallbackGetById")
    public Medico getById(Long id) {
        return repository.findById(id).orElseThrow(() -> new RuntimeException("Médico no encontrado"));
    }

    public Medico fallbackGetById(Long id, Exception e) {
        throw new RuntimeException("Servicio no disponible temporalmente");
    }

    @CircuitBreaker(name = "citasService", fallbackMethod = "fallbackCreate")
    public Medico create(Medico m) {
        return repository.save(m);
    }

    public Medico fallbackCreate(Medico m, Exception e) {
        throw new RuntimeException("No se puede crear el médico. Servicio no disponible temporalmente.");
    }

    @CircuitBreaker(name = "citasService", fallbackMethod = "fallbackUpdate")
    public Medico update(Long id, Medico data) {
        Medico existing = getById(id);
        existing.setRut(data.getRut());
        existing.setNombre(data.getNombre());
        existing.setEspecialidad(data.getEspecialidad());
        existing.setEstablecimientoId(data.getEstablecimientoId());
        existing.setActivo(data.getActivo());
        return repository.save(existing);
    }

    public Medico fallbackUpdate(Long id, Medico data, Exception e) {
        throw new RuntimeException("No se puede actualizar el médico. Servicio no disponible temporalmente.");
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}
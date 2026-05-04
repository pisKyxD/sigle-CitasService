package com.rednorte.sigle.citas_service.service;

import com.rednorte.sigle.citas_service.model.Medico;
import com.rednorte.sigle.citas_service.repository.MedicoRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MedicoService {

    private final MedicoRepository repository;

    public List<Medico> getAll() {
        return repository.findAll();
    }

    public Medico getById(Long id) {
        return repository.findById(id).orElseThrow(() -> new RuntimeException("Médico no encontrado"));
    }

    public Medico create(Medico m) {
        return repository.save(m);
    }

    public Medico update(Long id, Medico data) {
        Medico existing = getById(id);
        existing.setRut(data.getRut());
        existing.setNombre(data.getNombre());
        existing.setEspecialidad(data.getEspecialidad());
        existing.setEstablecimientoId(data.getEstablecimientoId());
        existing.setActivo(data.getActivo());
        return repository.save(existing);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}

package com.rednorte.sigle.citas_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rednorte.sigle.citas_service.model.Cita;
import com.rednorte.sigle.citas_service.model.EstadoCita;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CitaRepository extends JpaRepository<Cita, Long> {
    List<Cita> findByPacienteId(Long pacienteId);
    List<Cita> findByMedicoId(Long medicoId);
    List<Cita> findByMedicoIdAndFechaHoraBetweenAndEstadoNot(Long medicoId, LocalDateTime inicio, LocalDateTime fin, EstadoCita estado);
}
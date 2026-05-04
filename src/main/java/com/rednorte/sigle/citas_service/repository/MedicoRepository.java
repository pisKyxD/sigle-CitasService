package com.rednorte.sigle.citas_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import com.rednorte.sigle.citas_service.model.Medico;

@Repository
public interface MedicoRepository extends JpaRepository<Medico, Long> {}

package com.rednorte.sigle.citas_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rednorte.sigle.citas_service.model.Cancelacion;

@Repository
public interface CancelacionRepository extends JpaRepository<Cancelacion, Long> {}

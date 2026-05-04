package com.rednorte.sigle.citas_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "medicos")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Medico {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String rut;
    private String nombre;
    private String especialidad;
    
    @Column(name = "establecimiento_id")
    private Long establecimientoId;
    
    private Boolean activo;
}

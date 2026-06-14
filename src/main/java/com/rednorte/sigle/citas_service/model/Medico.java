package com.rednorte.sigle.citas_service.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
    @NotBlank(message = "El RUT es obligatorio")
    private String rut;

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "La especialidad es obligatoria")
    private String especialidad;
    
    @Column(name = "establecimiento_id")
    @NotNull(message = "El establecimiento es obligatorio")
    private Long establecimientoId;
    
    private Boolean activo;
}
package com.rednorte.sigle.citas_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "cancelaciones")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Cancelacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cita_id")
    private Cita cita;
    
    private String motivo;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "cancelado_por")
    private CanceladoPor canceladoPor;
    
    @Column(name = "fecha_cancelacion")
    private LocalDateTime fechaCancelacion;
    
    private Boolean reasignado;
}

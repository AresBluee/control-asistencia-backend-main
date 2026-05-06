package com.agro.control_asistencia_backend.scheduling.model.entity;

import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "work_schedules")
public class WorkSchedule {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name; // Ej: Turno Mañana, Turno Noche

    @Column(nullable = false)
    private LocalTime startTime; // Hora de entrada esperada (Ej: 07:00:00)

    @Column(nullable = false)
    private LocalTime endTime; // Hora de salida esperada (Ej: 15:00:00)
    
    // Tolerancia para la entrada/salida (Ej: 15 minutos)
    @Column(nullable = false) 
    private Integer toleranceMinutes = 15; 
}

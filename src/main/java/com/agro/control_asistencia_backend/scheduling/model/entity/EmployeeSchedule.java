package com.agro.control_asistencia_backend.scheduling.model.entity;

import java.time.LocalDate;

import com.agro.control_asistencia_backend.employee.model.entity.Employee;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "employee_schedules")
public class EmployeeSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Empleado al que se asigna el horario
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee; 

    // Turno asignado
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "schedule_id", nullable = false)
    private WorkSchedule workSchedule;

    // Periodo de validez de este horario
    @Column(nullable = false)
    private LocalDate validFrom;
    
    private LocalDate validTo; // Puede ser nulo si es indefinido
    
    // CRÍTICO: Indica los días de la semana que se aplica (Ej: "LUN, MAR, MIE, JUE, VIE")
    @Column(length = 50)
    private String workingDays; 
}

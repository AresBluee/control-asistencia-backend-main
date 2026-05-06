package com.agro.control_asistencia_backend.scheduling.model.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ScheduleResponseDTO {

    private Long id; // ID de la asignación (EmployeeSchedule)

    // Datos del Empleado
    private Long employeeId;
    private String employeeCode;
    private String employeeName; // <-- Añadir para el saludo o referencia
    
    // Datos de Asignación y Vigencia
    private LocalDate validFrom;
    private LocalDate validTo;
    private String workingDays;

    // Datos del Turno (WorkSchedule)
    private Long scheduleId;
    private String scheduleName;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer toleranceMinutes;
}

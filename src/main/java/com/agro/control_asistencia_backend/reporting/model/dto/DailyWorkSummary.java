package com.agro.control_asistencia_backend.reporting.model.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DailyWorkSummary {

    private Long employeeId;
    private String employeeCode;
    private LocalDate date;
    
    // El primer registro 'IN' del día
    private LocalTime checkInTime; 
    
    // El último registro 'OUT' del día
    private LocalTime checkOutTime; 
    
    // Duración total trabajada (Ej: "8h 30m")
    private String totalDuration; 
    
    // Duración total en minutos (útil para el frontend y cálculos de nómina)
    private Long totalMinutes; 
    
    // Indica si el día tuvo un ciclo completo de IN y OUT
    private boolean isComplete; 

    private Long overtimeMinutes;

    private long regularMinutes;
}

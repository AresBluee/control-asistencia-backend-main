package com.agro.control_asistencia_backend.reporting.model.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DailyAttendanceReportDTO {

    private LocalDate date;
    
    // Minutos trabajados dentro del horario regular
    private Long regularMinutes; 
    
    // Minutos trabajados que exceden el horario programado
    private Long totalOvertimeMinutes;

    
}

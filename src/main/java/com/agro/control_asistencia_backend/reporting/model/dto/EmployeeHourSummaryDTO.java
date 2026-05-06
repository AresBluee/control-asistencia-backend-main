package com.agro.control_asistencia_backend.reporting.model.dto;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EmployeeHourSummaryDTO {

    private Long employeeId;
    private BigDecimal fixedSalary; // Salario fijo del empleado
    private Long totalOvertimeMinutes; // Horas Extra acumuladas en el período
    private String totalOvertimeDuration;
}

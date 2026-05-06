package com.agro.control_asistencia_backend.document.model.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class PayslipResponseDTO {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private String employeeCode;
    private LocalDate periodStartDate;
    private LocalDate periodEndDate;
    private BigDecimal grossSalary;
    private BigDecimal bonuses;
    private BigDecimal deductions;
    private BigDecimal netSalary;
    private LocalDate generationDate;
    private String filePath;
}

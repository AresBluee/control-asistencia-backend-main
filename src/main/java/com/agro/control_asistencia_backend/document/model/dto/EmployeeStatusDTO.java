package com.agro.control_asistencia_backend.document.model.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import lombok.Data;

@Data
public class EmployeeStatusDTO {

    private Long employeeId;
    private String employeeCode;
    private String fullName;
    private String position;
    
    // Estado del día (ej: 'ASISTIO', 'NO_REGISTRADO', 'SALIO')
    private String status; 
    
    // El último marcaje registrado
    private LocalTime lastMarkTime; 
    private LocalDate reportDate;   

    private String biometricHash;
}

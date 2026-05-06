package com.agro.control_asistencia_backend.employee.model.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeResponseDTO {

    private Long id;
    private String employeeCode;
    private String fullName;
    private String position;
    
    // Información de contacto
    private String email;
    private String phoneNumber;
    private String address;
    
    // Información de Usuario y Rol para la tabla
    private Long userId;
    private String username;
    private String roleName;
    private boolean isEnabled;
    
    // Información de contratación
    private LocalDate hireDate;
    
    // Información salarial
    private java.math.BigDecimal fixedSalary;
    private java.math.BigDecimal hourlyRate;
}

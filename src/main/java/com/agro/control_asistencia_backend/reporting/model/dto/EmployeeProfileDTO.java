package com.agro.control_asistencia_backend.reporting.model.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.agro.control_asistencia_backend.employee.model.entity.Employee;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class EmployeeProfileDTO {

    private Long id;
    private String employeeCode;
    private String firstName;
    private String lastName;
    private String username;

    private String position;

    private String email;
    private String phoneNumber;
    private String address;
    
    // Información de contratación
    private LocalDate hireDate;
    
    // Estado del usuario
    private boolean isEnabled;

    private BigDecimal fixedSalary;
    private BigDecimal hourlyRate;
    private Long totalOvertimeMinutes;
    private String totalOvertimeDuration;

    public EmployeeProfileDTO(Employee employee, EmployeeHourSummaryDTO summary) {
        // Mapeo de la Entidad Employee
        this.id = employee.getId();
        this.employeeCode = employee.getEmployeeCode();
        this.firstName = employee.getFirstName();
        this.lastName = employee.getLastName();
        this.position = employee.getPosition().getName();
        this.fixedSalary = employee.getFixedSalary();
        this.hourlyRate = employee.getHourlyRate();
        this.hireDate = employee.getHireDate();
        this.email = employee.getEmail();
        this.phoneNumber = employee.getPhoneNumber();
        this.address = employee.getAddress();
        this.username = employee.getUser().getUsername(); // Asume que la relación está cargada
        this.isEnabled = employee.getUser().isEnabled();

        // Mapeo del Resumen de Horas Extra
        this.totalOvertimeMinutes = summary.getTotalOvertimeMinutes();
        this.totalOvertimeDuration = summary.getTotalOvertimeDuration();
    }
}

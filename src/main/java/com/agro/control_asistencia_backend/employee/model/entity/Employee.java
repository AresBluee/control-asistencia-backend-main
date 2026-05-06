package com.agro.control_asistencia_backend.employee.model.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "employee", uniqueConstraints = {
        // 1. CRÍTICO: employee_code debe ser único
        @UniqueConstraint(columnNames = "employee_code"),
        // 2. CRÍTICO: dni debe ser único
        @UniqueConstraint(columnNames = "dni"),
        // 3. CRÍTICO: biometric_hash debe ser único (Espacio eliminado)
        @UniqueConstraint(columnNames = "biometric_hash"),
        // 4. CRÍTICO: email debe ser único (ya está en @Column, pero es buena práctica
        // declararlo aquí)
        @UniqueConstraint(columnNames = "email")
})
@Data
@NoArgsConstructor
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Datos de Identificación
    @Column(name = "employee_code", nullable = false, length = 8)
    private String employeeCode;

    @Column(nullable = false, length = 50)
    private String firstName;

    @Column(nullable = false, length = 50)
    private String lastName;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "position_id", nullable = false)
    private WorkPosition position;

    @Column(name = "phone_number", length = 15)
    private String phoneNumber;

    @Column(name = "address")
    private String address;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "dni", nullable = false, length = 8, unique = true)
    private String dni;

    // Datos de Seguridad / Biometría
    @Column(name = "biometric_hash", nullable = false, length = 64)
    private String biometricHash;

    // Relaciones
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    // Datos de Nómina
    @Column(name = "fixed_salary")
    private BigDecimal fixedSalary;

    @Column(name = "hourly_rate")
    private BigDecimal hourlyRate;

    @Column(name = "hire_date", nullable = false)
    private LocalDate hireDate = LocalDate.now();
}
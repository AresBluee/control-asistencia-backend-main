package com.agro.control_asistencia_backend.employee.model.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class EmployeeRequestDTO {

    // --- Datos de Identificación y Contacto ---
    
    @NotBlank(message = "El DNI es obligatorio.")
    @Size(min = 8, max = 8, message = "El DNI debe tener 8 caracteres.")
    private String dni; // CRÍTICO: DNI para identificación

    @NotBlank(message = "El email es obligatorio para las notificaciones.")
    @Email(message = "Formato de email inválido.")
    private String email;

    @Size(max = 15)
    private String phoneNumber;
    
    // --- Datos del Empleado ---

    // El código de empleado se genera automáticamente en el backend

    @NotBlank(message = "El nombre es obligatorio.")
    @Size(max = 50)
    private String firstName;

    @NotBlank(message = "El apellido es obligatorio.")
    @Size(max = 50)
    private String lastName;

    @NotNull(message = "El ID del cargo es obligatorio.")
    private Long positionId;

    // Se ha eliminado el hash biométrico a petición.

    // --- Datos de la Cuenta de Usuario (Login) ---
    
    @NotBlank(message = "El nombre de usuario es obligatorio.")
    @Size(min = 4, max = 50)
    private String username;

    @NotBlank(message = "La contraseña es obligatoria para la cuenta de usuario.")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres.")
    private String password; 

    @NotBlank(message = "El rol es obligatorio.")
    private String roleName;
    
  
}
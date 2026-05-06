package com.agro.control_asistencia_backend.employee.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetDTO {

    @NotBlank(message = "El token de recuperación es obligatorio.")
    private String token; // El token único enviado por correo

    @NotBlank(message = "La nueva contraseña es obligatoria.")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres.")
    private String newPassword; 
    
    // Campo para la validación de coincidencia en el backend
    @NotBlank(message = "La confirmación de contraseña es obligatoria.")
    private String confirmPassword;
}

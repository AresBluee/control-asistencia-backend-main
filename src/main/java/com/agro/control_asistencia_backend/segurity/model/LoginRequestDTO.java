package com.agro.control_asistencia_backend.segurity.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LoginRequestDTO {


     @NotBlank(message = "Username cannot be empty")
    private String username;

    @NotBlank(message = "Password cannot be empty")
    private String password;
}

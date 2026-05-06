package com.agro.control_asistencia_backend.segurity.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor 
public class JwtResponseDTO {

    private String token;

    private String type = "Bearer";

    private Long id;
    private String username;
    //private String email; // Opcional, pero útil

    private String role;

      public JwtResponseDTO(String accessToken, Long id, String username, String role) {
        this.token = accessToken;
        this.id = id;
        this.username = username;
        this.role = role;
        // ¡AGREGA ESTA LÍNEA!
        this.type = "Bearer"; 
    }
}

package com.agro.control_asistencia_backend.shared.model;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ErrorResponse {

     private LocalDateTime timestamp;
    private int status;
    private String error; // Nombre de la excepción (Ej: "RuntimeException")
    private String message; // Mensaje de error para el usuario
    private String path; // La ruta que causó el error (Ej: "/api/employee")

    public ErrorResponse(int status, String error, String message, String path) {
        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
    }
}

package com.agro.control_asistencia_backend.shared.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.agro.control_asistencia_backend.shared.model.ErrorResponse;

import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class GlobalExceptionHandler {
     // ---------------------------------------------------------------------
    // MANEJO DE ERRORES DE LÓGICA DE NEGOCIO (Ej: Usuario ya existe)
    // ---------------------------------------------------------------------
    // Captura las excepciones genéricas que lanzamos en los Services (new RuntimeException("Error: ..."))
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex, HttpServletRequest request) {
        
        // La mayoría de nuestros errores de validación simple (ej. hash duplicado)
        // se manejan con un HTTP 400 Bad Request
        HttpStatus status = HttpStatus.BAD_REQUEST; 

        ErrorResponse errorResponse = new ErrorResponse(
            status.value(),
            ex.getClass().getSimpleName(),
            ex.getMessage(),
            request.getRequestURI()
        );

        return new ResponseEntity<>(errorResponse, status);
    }

    // ---------------------------------------------------------------------
    // MANEJO DE ERRORES DE VALIDACIÓN DE DTOs (@Valid)
    // ---------------------------------------------------------------------

    // Captura los errores cuando el DTO de entrada no pasa las anotaciones @NotBlank, @Size
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex, HttpServletRequest request) {
        
        // Construye un mensaje limpio basado en el primer error de validación
        String errorMessage = ex.getBindingResult().getFieldError().getDefaultMessage();
        
        HttpStatus status = HttpStatus.BAD_REQUEST;

        ErrorResponse errorResponse = new ErrorResponse(
            status.value(),
            "ValidationException",
            "Validation Error: " + errorMessage,
            request.getRequestURI()
        );

        return new ResponseEntity<>(errorResponse, status);
    }
    
    // ---------------------------------------------------------------------
    // MANEJO DE ERRORES GENERALES O NO PREVISTOS (HTTP 500)
    // ---------------------------------------------------------------------
    // Se podrían añadir otros, como handleDataAccessException, pero por ahora esto basta.
}

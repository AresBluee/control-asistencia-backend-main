package com.agro.control_asistencia_backend.document.model.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CertificateCreationDTO {
    @NotNull(message = "El ID del empleado es obligatorio.")
    private Long employeeId; 
    
    @NotBlank(message = "El título del certificado es obligatorio.")
    private String title; // Título del documento
    
    @NotBlank(message = "El tipo de documento es obligatorio (Ej: CERTIFICADO).")
    private String documentType; 

    @NotNull
    private LocalDate issueDate; // Fecha de Emisión
    
    private LocalDate expirationDate; // Opcional
    
    @NotBlank(message = "La ruta del archivo es obligatoria.")
    private String storagePath;
}

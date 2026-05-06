package com.agro.control_asistencia_backend.document.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FileUploadDTO {

    @NotNull(message = "El ID del empleado es obligatorio.")
    private Long employeeId; 

    @NotBlank(message = "El tipo de documento es obligatorio (Ej: Boleta de Pago).")
    private String documentType; 
}

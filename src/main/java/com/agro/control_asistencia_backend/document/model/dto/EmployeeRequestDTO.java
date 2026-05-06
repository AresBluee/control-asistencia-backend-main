package com.agro.control_asistencia_backend.document.model.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EmployeeRequestDTO {

    // ID del tipo de solicitud (ej: 1 para Permiso, 2 para Vacaciones)
    @NotNull(message = "El ID del tipo de solicitud es obligatorio.")
    private Long requestTypeId; 

    // Detalle o razón de la solicitud
    @NotBlank(message = "El detalle de la solicitud no puede estar vacío.")
    private String details;

    // Fecha de inicio (puede ser nulo si no aplica, como en una solicitud de boleta)
    private LocalDateTime startDate; 

    // Fecha de fin (puede ser nulo si no aplica)
    private LocalDateTime endDate; 
}

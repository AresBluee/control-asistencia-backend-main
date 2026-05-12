package com.agro.control_asistencia_backend.document.model.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RequestTypeDTO {

    private Long id;

    @NotBlank(message = "El nombre del tipo de solicitud no puede estar vacío")
    private String name;

    @NotNull(message = "La prioridad base es obligatoria")
    @Min(value = 1, message = "La prioridad mínima es 1")
    @Max(value = 4, message = "La prioridad máxima es 4")
    private Integer basePriority;

    @NotNull(message = "Los días SLA son obligatorios")
    @Min(value = 1, message = "El SLA mínimo es 1 día")
    private Integer slaDays;

    @NotNull(message = "Debe especificar si requiere firma")
    private Boolean requiresSignature;

    @NotNull(message = "Debe especificar si requiere documento adjunto")
    private Boolean requiresAttachment;
}

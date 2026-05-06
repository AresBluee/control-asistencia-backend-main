package com.agro.control_asistencia_backend.document.model.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RequestCreateDTO {

    @NotNull(message = "El tipo de solicitud es obligatorio.")
    private Long requestTypeId;

    @NotBlank(message = "Los detalles de la solicitud no pueden estar vacíos.")
    @Size(max = 255, message = "Los detalles no pueden exceder los 255 caracteres.")
    private String details;

    @NotNull(message = "La fecha de inicio es obligatoria.")
    @FutureOrPresent(message = "La fecha de inicio no puede ser en el pasado.")
    private LocalDate startDate;

    private LocalDate endDate; // Puede ser nulo si es un permiso de un solo día.
}
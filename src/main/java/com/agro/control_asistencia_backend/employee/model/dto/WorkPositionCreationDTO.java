package com.agro.control_asistencia_backend.employee.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkPositionCreationDTO {

    @NotBlank(message = "El nombre del cargo es obligatorio.")
    private String name;
}

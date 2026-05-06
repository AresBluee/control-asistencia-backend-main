package com.agro.control_asistencia_backend.scheduling.model.dto;

import java.time.LocalTime;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class WorkScheduleDTO {

    @NotBlank(message = "El nombre del turno es obligatorio.")
    private String name;

    @NotNull(message = "La hora de inicio es obligatoria.")
    private LocalTime startTime;

    @NotNull(message = "La hora de fin es obligatoria.")
    private LocalTime endTime;

    @Min(value = 0, message = "La tolerancia no puede ser negativa.")
    private Integer toleranceMinutes; // Minutos de tolerancia

}

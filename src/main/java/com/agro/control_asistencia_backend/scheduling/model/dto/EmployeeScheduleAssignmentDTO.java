package com.agro.control_asistencia_backend.scheduling.model.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EmployeeScheduleAssignmentDTO {

    @NotNull(message = "El ID del empleado es obligatorio.")
    private Long employeeId;

    @NotNull(message = "El ID del turno es obligatorio.")
    private Long scheduleId;

    @NotNull(message = "La fecha de inicio de vigencia es obligatoria.")
    private LocalDate validFrom;

    // Puede ser nulo para un horario indefinido
    private LocalDate validTo;

    @NotBlank(message = "Los días laborables son obligatorios (Ej: LUN,MAR,MIE).")
    private String workingDays;

}

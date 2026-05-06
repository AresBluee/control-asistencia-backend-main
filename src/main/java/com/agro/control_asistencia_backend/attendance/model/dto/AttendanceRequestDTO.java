package com.agro.control_asistencia_backend.attendance.model.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AttendanceRequestDTO {

    @NotBlank(message = "Biometric Hash is required.")
    private String biometricHash;

    @NotNull(message = "Device timestamp is required.")
    private LocalDateTime deviceTimestamp;

    
    @NotNull(message = "Latitude is required.")
    private Double latitude;

    @NotNull(message = "Longitude is required.")
    private Double longitude;

}

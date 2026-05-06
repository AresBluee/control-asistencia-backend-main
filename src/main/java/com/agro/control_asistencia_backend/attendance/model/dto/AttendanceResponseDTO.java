package com.agro.control_asistencia_backend.attendance.model.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AttendanceResponseDTO {

     private Long id;
    private String employeeCode; 
    private String recordType; 
    private LocalDateTime deviceTimestamp; 
    private LocalDateTime syncTimestamp;
    private Double latitude;
    private Double longitude;
}

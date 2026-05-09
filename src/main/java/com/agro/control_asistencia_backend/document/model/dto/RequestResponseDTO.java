package com.agro.control_asistencia_backend.document.model.dto;

import java.time.LocalDate;

import lombok.Builder;
import lombok.Data;

@Data
@Builder

public class RequestResponseDTO {

    private Long id;
    private Long employeeId; // ID del empleado
    private String employeeName; // Nombre para mostrar en el frontend
    private String requestType; // Nombre del tipo de solicitud
    private String details;
    private LocalDate requestedDate;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status; // PENDING, IN_REVIEW, AWAITING_SIGNATURE, APPROVED, REJECTED, COMPLETED
    private String managerComment; // Comentario del manager/RRHH
    private String managerName; // Nombre del jefe que aprobó/firmó

    // Nuevos campos
    private Integer calculatedPriority;
    private Long remainingSlaHours;
    private Boolean isSigned;
    private String documentPath;
    private Boolean requiresSignature;
}

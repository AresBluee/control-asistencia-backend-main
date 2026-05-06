package com.agro.control_asistencia_backend.document.model.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DocumentResponseDTO {

    private Long id;
    private String fileName;
    private String documentType;
    private String employeeCode;
    private LocalDateTime uploadDate;
    private String employeeName;
    private String downloadUrl;
    private String uploadedBy; 

}

package com.agro.control_asistencia_backend.employee.model.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ManagerContactDTO {

    private Long id;
    private String fullName;
    private String position;
    private String roleName;
}

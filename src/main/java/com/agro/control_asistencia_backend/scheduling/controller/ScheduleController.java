package com.agro.control_asistencia_backend.scheduling.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.agro.control_asistencia_backend.scheduling.model.dto.EmployeeScheduleAssignmentDTO;
import com.agro.control_asistencia_backend.scheduling.model.dto.ScheduleResponseDTO;
import com.agro.control_asistencia_backend.scheduling.model.dto.WorkScheduleDTO;
import com.agro.control_asistencia_backend.scheduling.model.entity.EmployeeSchedule;
import com.agro.control_asistencia_backend.scheduling.model.entity.WorkSchedule;
import com.agro.control_asistencia_backend.scheduling.service.ScheduleService;
import com.agro.control_asistencia_backend.segurity.service.UserDetailsImpl;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/schedules")

public class ScheduleController {

    private final ScheduleService scheduleService;

    @Autowired
    public ScheduleController(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    /**
     * Endpoint: POST /api/schedules/turn (Crea una plantilla de turno)
     */
    @PostMapping("/turn")

    public ResponseEntity<WorkSchedule> createWorkSchedule(@Valid @RequestBody WorkScheduleDTO dto) {
        WorkSchedule schedule = scheduleService.createWorkSchedule(dto);
        return new ResponseEntity<>(schedule, HttpStatus.CREATED);
    }

    /**
     * Endpoint: POST /api/schedules/assign (Asigna un turno a un empleado)
     */
    @PostMapping("/assign")

    public ResponseEntity<ScheduleResponseDTO> assignSchedule(@Valid @RequestBody EmployeeScheduleAssignmentDTO dto) {
        ScheduleResponseDTO assignment = scheduleService.assignScheduleToEmployee(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(assignment);
    }

   
    @GetMapping 
    
    public ResponseEntity<List<WorkSchedule>> getAllWorkSchedules() {
        List<WorkSchedule> schedules = scheduleService.getAllWorkSchedules();
        return ResponseEntity.ok(schedules);
    }

    @GetMapping("/me")
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN', 'ROLE_RRHH')")
    public ResponseEntity<ScheduleResponseDTO> getMySchedule(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam LocalDate date) {
        
        // 1. Obtener el ID del empleado
        Long userId = userDetails.getId(); 

        // 2. Llamar al servicio para obtener la asignación de horario válida
        // NOTA: Asumimos que el servicio devuelve la asignación activa para esa fecha
        Optional<ScheduleResponseDTO> scheduleOpt = scheduleService.getEmployeeScheduleByUserId(userId, date);
        if (scheduleOpt.isEmpty()) {
            // 💡 No encontrado: Devuelve una respuesta 404
            return ResponseEntity.notFound().build(); 
        }
        
        // 3. Devolver la Entidad EmployeeSchedule (Asignación)
        // CRÍTICO: Idealmente, esto devolvería un DTO, pero devolvemos la Entidad para simplificar.
        return ResponseEntity.ok(scheduleOpt.get());
    }
}

package com.agro.control_asistencia_backend.attendance.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.agro.control_asistencia_backend.attendance.model.dto.AttendanceRequestDTO;
import com.agro.control_asistencia_backend.attendance.model.dto.AttendanceResponseDTO;
import com.agro.control_asistencia_backend.attendance.model.entity.AttendanceHistoryDTO;
import com.agro.control_asistencia_backend.attendance.model.entity.AttendanceRecord;
import com.agro.control_asistencia_backend.attendance.repository.AttendanceRepository;
import com.agro.control_asistencia_backend.attendance.service.AttendanceAdminService;
import com.agro.control_asistencia_backend.attendance.service.AttendanceService;
import com.agro.control_asistencia_backend.document.model.dto.EmployeeStatusDTO;
import com.agro.control_asistencia_backend.employee.repository.EmployeeRepository;
import com.agro.control_asistencia_backend.segurity.service.UserDetailsImpl;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    @Autowired private EmployeeRepository employeeRepository;
    private final AttendanceService attendanceService;
    @Autowired private AttendanceRepository attendanceRepository;
    private final AttendanceAdminService adminService;

    @Autowired
    public AttendanceController(AttendanceService attendanceService, AttendanceAdminService adminService) {
        this.attendanceService = attendanceService;
        this.adminService = adminService;
    }

    @PostMapping("/register")
    public ResponseEntity<AttendanceResponseDTO> registerAttendance(
            @Valid @RequestBody AttendanceRequestDTO requestDTO) {
        AttendanceResponseDTO record = attendanceService.processAttendanceRecord(requestDTO);
        return new ResponseEntity<>(record, HttpStatus.CREATED);

    }

    @GetMapping("/status/daily")

    public ResponseEntity<List<EmployeeStatusDTO>> getDailyStatus(@RequestParam LocalDate date) {
        List<EmployeeStatusDTO> statusList = adminService.getDailyAttendanceStatus(date);
        return ResponseEntity.ok(statusList);
    }

    @GetMapping("/user")

    public ResponseEntity<List<AttendanceRecord>> getUserAttendance(Authentication authentication) {
        // 💡 El nombre de usuario (código de empleado) se obtiene del token JWT
        String employeeCode = authentication.getName();
        List<AttendanceRecord> userAttendance = attendanceService.getAttendanceByEmployeeCode(employeeCode);
        return ResponseEntity.ok(userAttendance);
    }
    @GetMapping("/me")
    
    public ResponseEntity<List<AttendanceHistoryDTO>> getMyAttendanceHistory(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        
        // 1. Obtiene el ID del usuario logueado (CRÍTICO)
        Long userId = userDetails.getId(); 
        
        // 2. Llama al servicio para obtener el historial (usando el ID)
        List<AttendanceHistoryDTO> userAttendance = attendanceService.getAttendanceByUserId(userId); 
        
        // 3. Devuelve la lista
        return ResponseEntity.ok(userAttendance);
    }

    
}

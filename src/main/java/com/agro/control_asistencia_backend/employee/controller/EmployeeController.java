package com.agro.control_asistencia_backend.employee.controller;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.agro.control_asistencia_backend.employee.model.dto.EmployeeRequestDTO;
import com.agro.control_asistencia_backend.employee.model.dto.EmployeeResponseDTO;
import com.agro.control_asistencia_backend.employee.model.dto.EmployeeProfileUpdateDTO;
import com.agro.control_asistencia_backend.employee.model.dto.ManagerContactDTO;
import com.agro.control_asistencia_backend.employee.model.dto.PasswordResetDTO;
import com.agro.control_asistencia_backend.employee.model.dto.WorkPositionCreationDTO;
import com.agro.control_asistencia_backend.employee.model.entity.Employee;
import com.agro.control_asistencia_backend.employee.model.entity.WorkPosition;
import com.agro.control_asistencia_backend.employee.service.EmployeeService;
import com.agro.control_asistencia_backend.employee.service.WorkPositionService;
import com.agro.control_asistencia_backend.reporting.model.dto.EmployeeHourSummaryDTO;
import com.agro.control_asistencia_backend.reporting.model.dto.EmployeeProfileDTO;
import com.agro.control_asistencia_backend.reporting.service.ReportingService;
import com.agro.control_asistencia_backend.segurity.controller.MessageResponse;
import com.agro.control_asistencia_backend.segurity.service.UserDetailsImpl;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/employee")
public class EmployeeController {

    // 1. Inyección de servicios FINAL
    private final EmployeeService employeeService;
    private final ReportingService reportingService;
    private final WorkPositionService workPositionService;

    @Autowired
    public EmployeeController(EmployeeService employeeService, ReportingService reportingService,
            WorkPositionService workPositionService) {
        this.employeeService = employeeService;
        this.reportingService = reportingService;
        this.workPositionService = workPositionService;
    }

    // -------------------------------------------------------------------------
    // ENDPOINTS DE CREACIÓN Y LISTADO GENERAL (ADMIN / RRHH)
    // -------------------------------------------------------------------------

    @PostMapping

    public ResponseEntity<EmployeeResponseDTO> createEmployee(@Valid @RequestBody EmployeeRequestDTO employeeDTO) {
        EmployeeResponseDTO newEmployee = employeeService.createEmployee(employeeDTO);
        return new ResponseEntity<>(newEmployee, HttpStatus.CREATED);
    }

    @GetMapping

    public ResponseEntity<List<EmployeeResponseDTO>> getAllEmployees() {

        List<EmployeeResponseDTO> employees = employeeService.getAllEmployees();
        return ResponseEntity.ok(employees);
    }

    // -------------------------------------------------------------------------
    // ENDPOINT DE PERFIL DEL EMPLEADO (MÓDULO DE NÓMINA Y DATOS PERSONALES)
    // Se elimina el método duplicado y se mantiene el DTO
    // -------------------------------------------------------------------------

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<EmployeeProfileDTO> getMyProfile(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        Long userId = userDetails.getId();

        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.withDayOfMonth(1);

        Employee employee = employeeService.getEmployeeByUserId(userId);

        // Llamada al ReportingService para obtener las Horas Extra
        EmployeeHourSummaryDTO summary = reportingService.getEmployeeHourSummary(employee.getId(), startDate, endDate);

        // Combina los datos de Employee y el Resumen en el DTO final
        EmployeeProfileDTO profileDTO = new EmployeeProfileDTO(employee, summary);

        return ResponseEntity.ok(profileDTO);
    }

    // -------------------------------------------------------------------------
    // ENDPOINT DE ACTUALIZACIÓN (PUT)
    // -------------------------------------------------------------------------

    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<EmployeeProfileDTO> updateMyProfile(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody EmployeeProfileUpdateDTO profileUpdateDTO) {

        EmployeeProfileDTO updatedEmployee = employeeService.updateProfile(userDetails.getId(), profileUpdateDTO);

        return ResponseEntity.ok(updatedEmployee);
    }

    // -------------------------------------------------------------------------
    // ENDPOINT DE LISTADO DE MANAGERS (PARA SOLICITUDES)
    // -------------------------------------------------------------------------

    @GetMapping("/managers")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ManagerContactDTO>> getManagers(@RequestParam List<String> roles) {

        String[] roleArray = roles.toArray(new String[0]);

        List<ManagerContactDTO> managers = employeeService.getManagersByRoles(roleArray);
        return ResponseEntity.ok(managers);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> requestPasswordReset(@RequestBody String email) {
        employeeService.createPasswordResetToken(email);
        return ResponseEntity.ok(new MessageResponse("Instrucciones enviadas al correo."));
    }

    // 2. POST /api/auth/reset-password (FINALIZA EL PROCESO)
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody PasswordResetDTO resetDTO) {
        if (!resetDTO.getNewPassword().equals(resetDTO.getConfirmPassword())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Las contraseñas no coinciden."));
        }

        employeeService.resetPassword(resetDTO.getToken(), resetDTO.getNewPassword());
        return ResponseEntity.ok(new MessageResponse("Contraseña actualizada con éxito."));
    }

    @PostMapping("/positions")

    public ResponseEntity<WorkPosition> createPosition(@Valid @RequestBody WorkPositionCreationDTO creationDTO) {
        // Llama al servicio para crear y validar el cargo
        WorkPosition newPosition = workPositionService.createPosition(creationDTO.getName());
        return new ResponseEntity<>(newPosition, HttpStatus.CREATED);
    }

    @GetMapping("/positions")

    public ResponseEntity<List<WorkPosition>> getAllPositions() {
        // Usa el WorkPositionService para obtener la lista
        List<WorkPosition> positions = workPositionService.getAllPositions();
        return ResponseEntity.ok(positions);
    }

    // -------------------------------------------------------------------------
    // ENDPOINTS DE GESTIÓN DE ESTADO DE USUARIOS (ACTIVAR/DESACTIVAR)
    // -------------------------------------------------------------------------

    @PutMapping("/status/{userId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('RRHH')") // Solo gestión puede cambiar el estado
    public ResponseEntity<MessageResponse> toggleUserStatus(
            @PathVariable Long userId,
            @RequestParam boolean enable) { // Recibe el estado booleano

        employeeService.toggleUserAccountStatus(userId, enable);
        return ResponseEntity.ok(new MessageResponse("Estado de cuenta actualizado con éxito."));
    }

    @PostMapping("/{userId}/activate")

    public ResponseEntity<?> activateUser(@PathVariable Long userId) {
        return employeeService.activateUser(userId);
    }

    @PostMapping("/{userId}/deactivate")

    public ResponseEntity<?> deactivateUser(@PathVariable Long userId) {
        return employeeService.deactivateUser(userId);
    }

    @GetMapping("/{userId}/status")

    public ResponseEntity<Map<String, Object>> getUserStatus(@PathVariable Long userId) {
        boolean isActive = employeeService.isUserActive(userId);
        Map<String, Object> response = new HashMap<>();
        response.put("userId", userId);
        response.put("isActive", isActive);
        response.put("status", isActive ? "ACTIVO" : "SUSPENDIDO");
        return ResponseEntity.ok(response);
    }

}
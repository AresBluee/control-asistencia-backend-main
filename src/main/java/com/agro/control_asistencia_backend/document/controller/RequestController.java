package com.agro.control_asistencia_backend.document.controller;

import java.util.List;

import org.apache.catalina.connector.Request;
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
import org.springframework.web.bind.annotation.RestController;

import com.agro.control_asistencia_backend.document.model.dto.RequestCreateDTO;
import com.agro.control_asistencia_backend.document.model.dto.RequestResponseDTO;
import com.agro.control_asistencia_backend.document.repository.EmployeeRequestRepository;
import com.agro.control_asistencia_backend.document.service.RequestService;
import com.agro.control_asistencia_backend.segurity.service.UserDetailsImpl;

import jakarta.validation.Valid;
import lombok.Data;
import lombok.NoArgsConstructor;

@RestController
@RequestMapping("/api/requests")

public class RequestController {

    private final RequestService requestService;
    private final EmployeeRequestRepository requestRepository; // Mantenido para el método findAll()

    @Autowired
    public RequestController(RequestService requestService, EmployeeRequestRepository requestRepository) {
        this.requestService = requestService;
        this.requestRepository = requestRepository;
    }

    // -----------------------------------------------------------
    // 1. ENDPOINT: CREAR SOLICITUD (EMPLEADO)
    // POST /api/requests
    // -----------------------------------------------------------
    @PostMapping
    @PreAuthorize("hasRole('EMPLOYEE') or hasRole('ADMIN')") // El empleado es el que debe hacer esto
    public ResponseEntity<RequestResponseDTO> createEmployeeRequest(
            @Valid @RequestBody RequestCreateDTO requestDTO,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        Long userId = userDetails.getId();

        // El servicio crea y devuelve el DTO de respuesta limpio
        RequestResponseDTO createdRequest = requestService.createRequest(requestDTO, userId);

        return ResponseEntity.status(HttpStatus.CREATED).body(createdRequest);
    }

    // -----------------------------------------------------------
    // 2. ENDPOINT: VER TODAS LAS SOLICITUDES (ADMIN/RRHH)
    // GET /api/requests
    // -----------------------------------------------------------
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('RRHH')")
    public ResponseEntity<List<RequestResponseDTO>> getAllRequests() {
        // Llama al servicio para obtener todas las solicitudes (PENDIENTES y otras)
        List<RequestResponseDTO> allRequests = requestService.getAllRequests();
        return ResponseEntity.ok(allRequests);
    }

    // -----------------------------------------------------------
    // 3. ENDPOINT: VER MIS SOLICITUDES (EMPLEADO)
    // GET /api/requests/me
    // -----------------------------------------------------------
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()") // Permite a cualquier usuario logueado (incluido EMPLOYEE) ver sus datos
    public ResponseEntity<List<RequestResponseDTO>> getMyRequests(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        // El servicio debe buscar solo las solicitudes asociadas al ID de usuario
        List<RequestResponseDTO> myRequests = requestService.getMyRequests(userDetails.getId());

        return ResponseEntity.ok(myRequests);
    }
    
    // -----------------------------------------------------------
    // 4. ENDPOINT: APROBAR/RECHAZAR SOLICITUD (ADMIN/RRHH)
    // PUT /api/requests/{requestId}/status
    // -----------------------------------------------------------
    @PutMapping("/{requestId}/status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('RRHH')")
    public ResponseEntity<RequestResponseDTO> updateRequestStatus(
            @PathVariable Long requestId,
            @RequestBody StatusUpdateDTO statusDTO) {

        // El servicio actualiza el estado y devuelve el DTO con los detalles
        RequestResponseDTO updatedRequest = requestService.updateRequestStatus(
                requestId, statusDTO.getStatus(), statusDTO.getComment());

        return ResponseEntity.ok(updatedRequest);
    }

    // -----------------------------------------------------------
    // CLASE INTERNA: DTO de Actualización de Estado (PUT Body)
    // -----------------------------------------------------------
    @Data
    @NoArgsConstructor
    // CRÍTICO: Debe ser 'static' para que Jackson pueda deserializarlo
    static class StatusUpdateDTO { 
        private String status;
        private String comment;
    }
}

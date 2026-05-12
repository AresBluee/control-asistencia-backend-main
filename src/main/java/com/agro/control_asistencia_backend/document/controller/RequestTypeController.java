package com.agro.control_asistencia_backend.document.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.agro.control_asistencia_backend.document.model.dto.RequestTypeDTO;
import com.agro.control_asistencia_backend.document.service.RequestTypeService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/request-types")
public class RequestTypeController {

    private final RequestTypeService requestTypeService;

    @Autowired
    public RequestTypeController(RequestTypeService requestTypeService) {
        this.requestTypeService = requestTypeService;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()") // Todos pueden ver los tipos para crear solicitudes
    public ResponseEntity<List<RequestTypeDTO>> getAllRequestTypes() {
        return ResponseEntity.ok(requestTypeService.getAllRequestTypes());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('RRHH')")
    public ResponseEntity<RequestTypeDTO> createRequestType(@Valid @RequestBody RequestTypeDTO dto) {
        return ResponseEntity.ok(requestTypeService.createRequestType(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('RRHH')")
    public ResponseEntity<RequestTypeDTO> updateRequestType(@PathVariable Long id, @Valid @RequestBody RequestTypeDTO dto) {
        return ResponseEntity.ok(requestTypeService.updateRequestType(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('RRHH')")
    public ResponseEntity<Void> deleteRequestType(@PathVariable Long id) {
        requestTypeService.deleteRequestType(id);
        return ResponseEntity.noContent().build();
    }
}

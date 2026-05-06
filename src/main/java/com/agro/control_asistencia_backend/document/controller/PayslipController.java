package com.agro.control_asistencia_backend.document.controller;

import com.agro.control_asistencia_backend.document.model.dto.PayslipResponseDTO;
import com.agro.control_asistencia_backend.document.model.entity.Payslip;
import com.agro.control_asistencia_backend.document.service.PayslipService;
import com.agro.control_asistencia_backend.segurity.service.UserDetailsImpl;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/payslips")
public class PayslipController {

    private final PayslipService payslipService;

    @Autowired
    public PayslipController(PayslipService payslipService) {
        this.payslipService = payslipService;
    }

    @Data
    static class PayslipGenerationRequest {
        private Long employeeId;
        private LocalDate startDate;
        private LocalDate endDate;
    }

    @PostMapping("/generate")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_RRHH')")
    public ResponseEntity<PayslipResponseDTO> generatePayslip(@RequestBody PayslipGenerationRequest request) {
        PayslipResponseDTO payslip = payslipService.generatePayslip(request.getEmployeeId(), request.getStartDate(), request.getEndDate());
        return ResponseEntity.ok(payslip);
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_RRHH')")
    public ResponseEntity<List<PayslipResponseDTO>> getAllPayslips() {
        List<PayslipResponseDTO> payslips = payslipService.getAllPayslips();
        return ResponseEntity.ok(payslips);
    }

    @GetMapping("/me")
    @PreAuthorize("hasAnyAuthority('ROLE_EMPLOYEE', 'ROLE_ADMIN', 'ROLE_RRHH')")
    public ResponseEntity<List<PayslipResponseDTO>> getMyPayslips(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        List<PayslipResponseDTO> payslips = payslipService.getPayslipsByUserId(userDetails.getId());
        return ResponseEntity.ok(payslips);
    }
}

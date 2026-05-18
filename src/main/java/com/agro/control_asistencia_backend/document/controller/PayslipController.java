package com.agro.control_asistencia_backend.document.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.agro.control_asistencia_backend.document.model.dto.PayslipResponseDTO;
import com.agro.control_asistencia_backend.document.model.entity.Payslip;
import com.agro.control_asistencia_backend.document.service.PayslipService;
import com.agro.control_asistencia_backend.document.service.DocumentService;
import com.agro.control_asistencia_backend.segurity.service.UserDetailsImpl;

import jakarta.persistence.EntityNotFoundException;
import lombok.Data;

@RestController
@RequestMapping("/api/payslips")
public class PayslipController {

    private final PayslipService payslipService;
    private final DocumentService documentService;

    @Autowired
    public PayslipController(PayslipService payslipService, DocumentService documentService) {
        this.payslipService = payslipService;
        this.documentService = documentService;
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

    /**
     * Descarga el PDF de una boleta de pago por su ID.
     * El archivo se genera y almacena físicamente al generar la boleta.
     */
    @GetMapping("/{id}/download")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_RRHH')")
    public ResponseEntity<UrlResource> downloadPayslipPdf(@PathVariable Long id) throws IOException {
        Payslip payslip = payslipService.getPayslipEntityById(id);
        String filePathStr = payslip.getFilePath();

        if (filePathStr == null || filePathStr.isBlank()) {
            throw new EntityNotFoundException("La boleta no tiene un archivo PDF asociado. Genere la boleta nuevamente.");
        }

        Path filePath = Paths.get(filePathStr).normalize();
        File pdfFile = filePath.toFile();

        if (!pdfFile.exists() || !pdfFile.isFile()) {
            throw new EntityNotFoundException("El archivo PDF de la boleta no se encontró en el servidor: " + filePathStr);
        }

        UrlResource resource = new UrlResource(filePath.toUri());

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + pdfFile.getName() + "\"")
                .body(resource);
    }
}

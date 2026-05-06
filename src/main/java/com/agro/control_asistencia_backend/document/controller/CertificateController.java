package com.agro.control_asistencia_backend.document.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.agro.control_asistencia_backend.document.model.dto.CertificateCreationDTO;
import com.agro.control_asistencia_backend.document.model.dto.DocumentResponseDTO;
import com.agro.control_asistencia_backend.document.service.DocumentService;
import com.agro.control_asistencia_backend.segurity.service.UserDetailsImpl;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/certificates")
public class CertificateController {

    @Autowired
            private DocumentService documentService; // Inyectamos el servicio de Documentos

            // -------------------------------------------------------------------------
            // 1. ENDPOINT DE ADMINISTRACIÓN (REGISTRO DE CERTIFICADO)
            // -------------------------------------------------------------------------
            @PostMapping // POST /api/certificates
            public ResponseEntity<DocumentResponseDTO> registerCertificate(
                    @Valid @RequestBody CertificateCreationDTO creationDTO) {

                DocumentResponseDTO newCertificate = documentService.createCertificateRecord(creationDTO);
                return new ResponseEntity<>(newCertificate, HttpStatus.CREATED);
            }

            // -------------------------------------------------------------------------
            // 2. ENDPOINT DEL EMPLEADO (VER SUS CERTIFICADOS)
            // -------------------------------------------------------------------------
            @GetMapping("/me") // GET /api/certificates/me
            public ResponseEntity<List<DocumentResponseDTO>> getMyCertificates(
                    @AuthenticationPrincipal UserDetailsImpl userDetails) {

                // Asumimos que getCertificatesByUserId realiza el filtro por ID de usuario
                List<DocumentResponseDTO> certificates = documentService.getCertificatesByUserId(userDetails.getId());

                return ResponseEntity.ok(certificates);
            }
}

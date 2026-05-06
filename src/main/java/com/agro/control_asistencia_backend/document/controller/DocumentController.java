package com.agro.control_asistencia_backend.document.controller;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.agro.control_asistencia_backend.document.model.dto.CertificateCreationDTO;
import com.agro.control_asistencia_backend.document.model.dto.DocumentResponseDTO;
import com.agro.control_asistencia_backend.document.model.dto.FileUploadDTO;
import com.agro.control_asistencia_backend.document.model.entity.Document;
import com.agro.control_asistencia_backend.document.model.entity.Payslip;
import com.agro.control_asistencia_backend.document.service.DocumentService;
import com.agro.control_asistencia_backend.document.service.PayslipService;
import com.agro.control_asistencia_backend.segurity.service.UserDetailsImpl;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

    @RestController
    @RequestMapping("/api/documents")
    public class DocumentController {

        private final DocumentService documentService;
        private final PayslipService payslipService;
    
        @Autowired
        public DocumentController(DocumentService documentService, PayslipService payslipService) {
            this.documentService = documentService;
            this.payslipService = payslipService;
        }
    
        // ---------------------------------------------------------------------
        // DTOs INTERNOS (Mantener)
        // ---------------------------------------------------------------------
        @Data @NotNull
        static class GenerationRequestDTO {
            private Long employeeId;
            private Long templateId;
        }
        
        // ---------------------------------------------------------------------
        // 1. CARGA y GENERACIÓN (ADMIN/RRHH)
        // ---------------------------------------------------------------------
    
        @PostMapping("/upload")
        @PreAuthorize("hasAnyAuthority(\'ROLE_ADMIN\', \'ROLE_RRHH\')") // \uD83D\uDD12 Seguridad
        public ResponseEntity<DocumentResponseDTO> uploadDocument(
                @RequestPart("metadata") @Valid FileUploadDTO metadata,
                @RequestPart("file") @NotNull MultipartFile file,
                @AuthenticationPrincipal UserDetailsImpl uploader) throws IOException {
    
            DocumentResponseDTO response = documentService.uploadDocument(
                    metadata.getEmployeeId(), metadata.getDocumentType(), file, uploader);
            return ResponseEntity.ok(response);
        }
    
        @PostMapping("/generate")
        @PreAuthorize("hasAnyAuthority(\'ROLE_ADMIN\', \'ROLE_RRHH\')") // \uD83D\uDD12 Seguridad
        public ResponseEntity<DocumentResponseDTO> generateDocument(
                @RequestBody GenerationRequestDTO request,
                @AuthenticationPrincipal UserDetailsImpl user) throws IOException {
    
            DocumentResponseDTO response = documentService.generateDocument(
                    request.getEmployeeId(), request.getTemplateId(), user);
            return ResponseEntity.ok(response);
        }
        
        // ---------------------------------------------------------------------
        // 2. DESCARGA (Universal: ADMIN/RRHH o Empleado Dueño)
        // ---------------------------------------------------------------------
    
        @GetMapping("/{id}/download")
        @PreAuthorize("hasAnyAuthority(\'ROLE_ADMIN\', \'ROLE_RRHH\') or @documentAuthorization.canView(#id, authentication.name)")
        public ResponseEntity<Resource> downloadDocument(@PathVariable Long id) throws MalformedURLException {
    
            // ... (Lógica de descarga se mantiene igual) ...
    
            Document document = documentService.getDocumentById(id);
            Path filePath = Paths.get(document.getStoragePath()).normalize();
            Resource resource = new UrlResource(filePath.toUri());
    
            // ... (Verificaciones de existencia) ...
    
            String contentType = document.getContentType();
            if (contentType == null) contentType = "application/octet-stream";
    
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + document.getFileName() + "\"")
                    .body(resource);
        }
    
    
        // ---------------------------------------------------------------------
        // 3. LISTADOS DE AUDITORÍA Y PERSONAL
        // ---------------------------------------------------------------------
    
        @GetMapping("/all")
        @PreAuthorize("hasAnyAuthority(\'ROLE_ADMIN\', \'ROLE_RRHH\')") // \uD83D\uDD12 Seguridad: Auditoría
        public ResponseEntity<List<DocumentResponseDTO>> getAllDocuments() {
            // Devuelve todos los documentos cargados por la administración
            List<DocumentResponseDTO> documents = documentService.getAllDocuments();
            return ResponseEntity.ok(documents);
        }
    
    
        @GetMapping("/user")
        @PreAuthorize("hasAnyAuthority(\'ROLE_EMPLOYEE\', \'ROLE_ADMIN\', \'ROLE_RRHH\')") // \uD83D\uDD12 Seguridad: Documentos personales
        public ResponseEntity<List<Document>> getUserDocuments(@AuthenticationPrincipal UserDetailsImpl userDetails) {
            List<Document> userDocs = documentService.getDocumentsByUserId(userDetails.getId());
            return ResponseEntity.ok(userDocs);
        }
     
    }

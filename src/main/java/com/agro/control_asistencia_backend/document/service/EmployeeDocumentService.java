package com.agro.control_asistencia_backend.document.service;

import com.agro.control_asistencia_backend.document.model.entity.EmployeeDocument;
import com.agro.control_asistencia_backend.document.model.entity.DocumentType;
import com.agro.control_asistencia_backend.document.model.enums.DocumentStatus;
import com.agro.control_asistencia_backend.document.repository.EmployeeDocumentRepository;
import com.agro.control_asistencia_backend.employee.model.entity.Employee;
import com.agro.control_asistencia_backend.notification.service.EmailService;
// NotificationService removed for simplicity
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class EmployeeDocumentService {

    private final EmployeeDocumentRepository employeeDocumentRepository;
    private final DocumentGenerationService documentGenerationService;
    private final EmailService emailService;


    public EmployeeDocumentService(EmployeeDocumentRepository employeeDocumentRepository,
                                  DocumentGenerationService documentGenerationService,
                                  EmailService emailService) {
        this.employeeDocumentRepository = employeeDocumentRepository;
        this.documentGenerationService = documentGenerationService;
        this.emailService = emailService;
    }

    /**
     * Create a new document request (status PENDING)
     */
    @Transactional
    public EmployeeDocument createDocumentRequest(Employee employee, DocumentType type) {
        EmployeeDocument doc = new EmployeeDocument();
        doc.setEmployee(employee);
        doc.setDocumentType(type);
        doc.setStatus(DocumentStatus.PENDING);
        doc.setCreatedAt(LocalDateTime.now());
        doc.setUpdatedAt(LocalDateTime.now());
        // simple unique code
        doc.setDocumentCode("EMPDOC-" + System.currentTimeMillis());
        EmployeeDocument saved = employeeDocumentRepository.save(doc);
        // notify employee
        String subject = "Solicitud de documento creada";
        String body = "Su solicitud de documento '" + type.getName() + "' ha sido creada y está pendiente de revisión por RRHH.";
        emailService.sendEmail(employee.getEmail(), subject, body);

        return saved;
    }

    @Transactional
    public EmployeeDocument sendToRrhh(Long documentId) throws IOException {
        EmployeeDocument doc = employeeDocumentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Documento no encontrado"));
        doc.setStatus(DocumentStatus.IN_REVIEW_RRHH);
        doc.setUpdatedAt(LocalDateTime.now());
        // generate PDF placeholder
        Path pdfPath = documentGenerationService.generatePdf(doc);
        doc.setDocumentPath(pdfPath.toString());
        EmployeeDocument saved = employeeDocumentRepository.save(doc);
        // Notify RRHH (simple email)
        emailService.sendEmail("rrhh@agrocyt.com", "Documento listo para revisión", "El documento " + doc.getDocumentCode() + " está listo para revisión.");
        return saved;
    }

    @Transactional
    public EmployeeDocument addObservation(Long documentId, String note) {
        EmployeeDocument doc = employeeDocumentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Documento no encontrado"));
        doc.getObservations().add(note);
        doc.setUpdatedAt(LocalDateTime.now());
        EmployeeDocument saved = employeeDocumentRepository.save(doc);
        // Notify employee of new observation
        emailService.sendEmail(doc.getEmployee().getEmail(), "Observación en su documento", note);
        return saved;
    }

    @Transactional
    public EmployeeDocument rejectDocument(Long documentId, String reason) {
        EmployeeDocument doc = employeeDocumentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Documento no encontrado"));
        doc.setStatus(DocumentStatus.REJECTED);
        doc.getObservations().add("Rechazo: " + reason);
        doc.setUpdatedAt(LocalDateTime.now());
        EmployeeDocument saved = employeeDocumentRepository.save(doc);
        // Notify employee
        String subject = "Documento rechazado";
        String body = "Su documento " + doc.getDocumentCode() + " ha sido rechazado. Motivo: " + reason;
        emailService.sendEmail(doc.getEmployee().getEmail(), subject, body);
        return saved;
    }

    @Transactional
    public EmployeeDocument sendToAdmin(Long documentId) {
        EmployeeDocument doc = employeeDocumentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Documento no encontrado"));
        if (!doc.getDocumentType().isRequiresAdminSignature()) {
            throw new IllegalArgumentException("Este tipo de documento no requiere firma de administrador");
        }
        doc.setStatus(DocumentStatus.AWAITING_SIGNATURE);
        doc.setUpdatedAt(LocalDateTime.now());
        EmployeeDocument saved = employeeDocumentRepository.save(doc);
        // Notify admin (simple email)
        emailService.sendEmail("admin@agrocyt.com", "Documento requiere firma", "El documento " + doc.getDocumentCode() + " está pendiente de su firma.");
        return saved;
    }

    @Transactional
    public EmployeeDocument signDocument(Long documentId, MultipartFile signedFile) throws IOException {
        EmployeeDocument doc = employeeDocumentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Documento no encontrado"));
        // Store the signed file in the same uploads folder
        Path uploadDir = Path.of("uploads/documents");
        Files.createDirectories(uploadDir);
        String fileName = doc.getDocumentCode() + "-signed.pdf";
        Path destination = uploadDir.resolve(fileName);
        Files.copy(signedFile.getInputStream(), destination);
        doc.setDocumentPath(destination.toString());
        doc.setStatus(DocumentStatus.COMPLETED);
        doc.setUpdatedAt(LocalDateTime.now());
        EmployeeDocument saved = employeeDocumentRepository.save(doc);
        // Notify employee
        String subject = "Documento completado";
        String body = "Su documento " + doc.getDocumentCode() + " ha sido firmado y está disponible para descarga.";
        emailService.sendEmail(doc.getEmployee().getEmail(), subject, body);
        return saved;
    }

    @Transactional(readOnly = true)
    public List<EmployeeDocument> getPendingByStatus(DocumentStatus status) {
        return employeeDocumentRepository.findByStatus(status);
    }

    @Transactional(readOnly = true)
    public List<EmployeeDocument> getPriorityQueue() {
        // For simplicity, order by document type priority and createdAt (older first)
        return employeeDocumentRepository.findAll().stream()
                .sorted((d1, d2) -> {
                    int p1 = d1.getDocumentType().getBasePriority();
                    int p2 = d2.getDocumentType().getBasePriority();
                    if (p1 != p2) return Integer.compare(p2, p1); // higher priority first
                    return d1.getCreatedAt().compareTo(d2.getCreatedAt());
                })
                .toList();
    }
}

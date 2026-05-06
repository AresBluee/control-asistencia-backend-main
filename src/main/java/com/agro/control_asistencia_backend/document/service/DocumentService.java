package com.agro.control_asistencia_backend.document.service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.agro.control_asistencia_backend.document.model.dto.CertificateCreationDTO;
import com.agro.control_asistencia_backend.document.model.dto.DocumentResponseDTO;
import com.agro.control_asistencia_backend.document.model.entity.Document;
import com.agro.control_asistencia_backend.document.model.entity.DocumentTemplate;
import com.agro.control_asistencia_backend.document.model.entity.Payslip;
import com.agro.control_asistencia_backend.document.repository.DocumentRepository;
import com.agro.control_asistencia_backend.document.repository.DocumentTemplateRepository;
import com.agro.control_asistencia_backend.employee.model.entity.Employee;
import com.agro.control_asistencia_backend.employee.repository.EmployeeRepository;
import com.agro.control_asistencia_backend.segurity.service.UserDetailsImpl;

@Service
public class DocumentService {

        // Ruta donde se guardarán los archivos (definida en application.properties)
        @Value("${file.upload-dir}")
        private String uploadDir;

        private final DocumentRepository documentRepository;
        private final EmployeeRepository employeeRepository;
        private final DocumentTemplateRepository templateRepository;

        public DocumentService(EmployeeRepository employeeRepository,
                        DocumentTemplateRepository templateRepository,
                        DocumentRepository documentRepository) {
                this.employeeRepository = employeeRepository;
                this.templateRepository = templateRepository;
                this.documentRepository = documentRepository;
        }

        // ---------------------------------------------------------------------
        // Lógica para Subir Archivos (Upload)
        // ---------------------------------------------------------------------

        @Transactional
        public DocumentResponseDTO uploadDocument(Long employeeId, String documentType,
                        MultipartFile file, UserDetailsImpl uploader) throws IOException {

                // 1. Validar Empleado de destino
                Employee employee = employeeRepository.findById(employeeId)
                                .orElseThrow(() -> new RuntimeException(
                                                "Empleado no encontrado con ID: " + employeeId));

                // 2. Definir la ruta de almacenamiento
                // Generamos un nombre único para evitar colisiones
                String uniqueFileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
                Path copyLocation = Paths.get(uploadDir + File.separator + uniqueFileName);

                // Aseguramos que el directorio exista
                Files.createDirectories(copyLocation.getParent());

                // 3. Guardar el archivo en el sistema de archivos (simulación)
                Files.copy(file.getInputStream(), copyLocation);

                // 4. Guardar Metadatos en la DB
                Document document = new Document();
                document.setEmployee(employee);
                document.setFileName(file.getOriginalFilename());
                document.setDocumentType(documentType);
                document.setContentType(file.getContentType());
                document.setStoragePath(copyLocation.toString()); // Guardamos la ruta física
                document.setUploadedBy(uploader.getUsername());

                Document savedDoc = documentRepository.save(document);

                // 5. Devolver DTO de Respuesta
                return mapToDTO(savedDoc);
        }

        // ---------------------------------------------------------------------
        // Lógica para Descargar Archivos (Download)
        // ---------------------------------------------------------------------

        public Document getDocumentById(Long documentId) {
                return documentRepository.findById(documentId)
                                .orElseThrow(() -> new RuntimeException(
                                                "Documento no encontrado con ID: " + documentId));
        }

        // ---------------------------------------------------------------------
        // Auxiliares
        // ---------------------------------------------------------------------
        @Transactional
        public DocumentResponseDTO generateDocument(Long employeeId, Long templateId, UserDetailsImpl user)
                        throws IOException {

                // 1. Obtener Empleado y Plantilla
                Employee employee = employeeRepository.findById(employeeId)
                                .orElseThrow(() -> new RuntimeException("Empleado no encontrado."));

                DocumentTemplate template = templateRepository.findById(templateId)
                                .orElseThrow(() -> new RuntimeException("Plantilla no encontrada."));

                // 2. Definir la lógica de reemplazo de contenido (Simulación de la generación)
                String content = "DOCUMENTO GENERADO A PARTIR DE PLANTILLA: " + template.getName() + "\n\n";
                content += "A quien corresponda, se certifica que nuestro empleado:\n";
                content += "Nombre: " + employee.getFirstName() + " " + employee.getLastName() + "\n";
                content += "Código: " + employee.getEmployeeCode() + "\n";
                content += "Cargo: " + employee.getPosition() + "\n";
                content += "Fecha de generación: " + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE);

                // 3. Simular el guardado del archivo generado
                String fileName = employee.getEmployeeCode() + "_" + template.getName().replaceAll("\\s+", "_") + "_"
                                + System.currentTimeMillis() + ".txt";
                Path copyLocation = Paths.get(uploadDir + File.separator + fileName);

                // Escribir el contenido al archivo de texto simulado
                try (PrintWriter out = new PrintWriter(new FileWriter(copyLocation.toFile()))) {
                        out.print(content);
                }

                // 4. Guardar Metadatos en la DB (igual que en uploadDocument)
                Document document = new Document();
                document.setEmployee(employee);
                document.setFileName(fileName);
                document.setDocumentType(template.getName());
                document.setContentType("text/plain"); // O el tipo de archivo real (application/pdf)
                document.setStoragePath(copyLocation.toString());
                document.setUploadedBy(user.getUsername());

                Document savedDoc = documentRepository.save(document);

                // 5. Devolver DTO de Respuesta
                return mapToDTO(savedDoc);
        }

        public String generatePayslipPdf(Payslip payslip) {

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMM");
                String period = payslip.getPeriodStartDate().format(formatter);

                String fileName = String.format("%d_%s_Boleta.pdf",
                                payslip.getEmployee().getId(),
                                period);

                String documentPath = "/payslips/" + fileName;
                return documentPath;
        }

        @Transactional(readOnly = true)
        public List<DocumentResponseDTO> getAllDocuments() {
                List<Document> documents = documentRepository.findAll();
                return documents.stream()
                                .map(this::mapToDTO)
                                .collect(Collectors.toList());
        }

        @Transactional(readOnly = true)
        public List<Document> getDocumentsByEmployeeCode(String employeeCode) {
                // 1. Obtener el objeto Employee
                Employee employee = employeeRepository.findByEmployeeCode(employeeCode)
                                .orElseThrow(() -> new RuntimeException("Empleado no encontrado."));

                // 2. Usar el repositorio para filtrar
                return documentRepository.findByEmployee(employee);
        }

        private DocumentResponseDTO mapToDTO(Document doc) {

                Employee employee = doc.getEmployee();

                String empCode = (employee != null) ? employee.getEmployeeCode() : "N/A";
                String empName = (employee != null) ? employee.getFirstName() + " " + employee.getLastName()
                                : "No Asignado";
                return DocumentResponseDTO.builder()
                                .id(doc.getId())
                                .fileName(doc.getFileName())
                                .documentType(doc.getDocumentType())
                                .employeeCode(empCode)
                                .employeeName(empName)
                                .uploadedBy(doc.getUploadedBy())
                                .uploadDate(doc.getUploadDate())
                                .downloadUrl("/api/documents/" + doc.getId() + "/download")
                                .build();
        }

        @Transactional
        public DocumentResponseDTO createCertificateRecord(CertificateCreationDTO dto) {

                Employee employee = employeeRepository.findById(dto.getEmployeeId())
                                .orElseThrow(() -> new RuntimeException("Empleado no encontrado."));

                Document doc = new Document();
                doc.setEmployee(employee);
                doc.setFileName(dto.getTitle() + "_" + dto.getIssueDate() + ".pdf"); // Generar un nombre de archivo
                doc.setDocumentType(dto.getDocumentType());
                doc.setContentType("application/pdf");
                doc.setStoragePath(dto.getStoragePath()); // Usar la ruta del DTO
                doc.setUploadedBy("SYSTEM/RRHH"); // Definir quién lo subió
                // ... otros setters

                Document savedDoc = documentRepository.save(doc);
                return mapToDTO(savedDoc); // Devolver el DTO limpio
        }

        @Transactional(readOnly = true)

        public List<DocumentResponseDTO> getCertificatesByUserId(Long userId) {
                Employee employee = employeeRepository.findByUserId(userId)
                                .orElseThrow(() -> new RuntimeException("Perfil no encontrado."));

                // Filtrar los documentos por empleado donde el documentType es 'CERTIFICADO'
                return documentRepository.findByEmployee(employee).stream()
                                .filter(d -> d.getDocumentType().toUpperCase().contains("CERTIFICADO")
                                                || d.getDocumentType().toUpperCase().contains("DIPLOMA"))
                                .map(this::mapToDTO)
                                .collect(Collectors.toList());
        }

        @Transactional(readOnly = true)
        public List<Document> getDocumentsByUserId(Long userId) {
                Employee employee = employeeRepository.findByUserId(userId)
                                .orElseThrow(() -> new RuntimeException("Perfil no encontrado."));
                return documentRepository.findByEmployee(employee);
        }
        
}

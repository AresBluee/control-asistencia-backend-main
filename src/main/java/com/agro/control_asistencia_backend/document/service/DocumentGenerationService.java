package com.agro.control_asistencia_backend.document.service;

import com.agro.control_asistencia_backend.document.model.entity.EmployeeDocument;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class DocumentGenerationService {

    private static final String UPLOAD_DIR = "uploads/documents";

    public Path generatePdf(EmployeeDocument employeeDocument) throws IOException {
        // Ensure directory exists
        java.nio.file.Files.createDirectories(Paths.get(UPLOAD_DIR));
        String fileName = employeeDocument.getDocumentCode() + ".pdf";
        Path filePath = Paths.get(UPLOAD_DIR, fileName);

        Document pdfDoc = new Document();
        try (FileOutputStream fos = new FileOutputStream(filePath.toFile())) {
            PdfWriter.getInstance(pdfDoc, fos);
            pdfDoc.open();
            // Simple placeholder content – can be extended with templates
            pdfDoc.add(new Paragraph("Documento: " + employeeDocument.getDocumentType().getName()));
            pdfDoc.add(new Paragraph("Empleado: " + employeeDocument.getEmployee().getFirstName() + " " + employeeDocument.getEmployee().getLastName()));
            pdfDoc.add(new Paragraph("Código: " + employeeDocument.getDocumentCode()));
            pdfDoc.add(new Paragraph("Fecha: " + java.time.LocalDate.now().toString()));
            pdfDoc.close();
        } catch (DocumentException e) {
            throw new IOException("Error generating PDF", e);
        }
        return filePath;
    }
}

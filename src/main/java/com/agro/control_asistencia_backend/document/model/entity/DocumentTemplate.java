package com.agro.control_asistencia_backend.document.model.entity;
import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "document_templates")
public class DocumentTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Nombre de la plantilla (Ej: "Carta de Trabajo - Crédito")
    @Column(nullable = false, unique = true)
    private String name; 
    
    // Tipo de archivo de la plantilla (Ej: DOCX, HTML, o PDF rellenable)
    @Column(nullable = false)
    private String fileType; 

    // Ruta en el disco donde se almacena el archivo de plantilla
    @Column(nullable = false)
    private String storagePath; 
    
    // Lista de variables esperadas en la plantilla (Ej: ${employeeName}, ${employeePosition})
    @Column(columnDefinition = "TEXT") 
    private String requiredFields; 

    private LocalDateTime uploadDate = LocalDateTime.now();
}

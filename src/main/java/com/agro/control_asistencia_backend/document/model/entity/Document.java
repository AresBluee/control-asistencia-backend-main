package com.agro.control_asistencia_backend.document.model.entity;
import java.time.LocalDateTime;
import com.agro.control_asistencia_backend.employee.model.entity.Employee;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "document")
public class Document {

     @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Nombre original del archivo
    @Column(nullable = false)
    private String fileName; 
    
    // Tipo de contenido (Ej: application/pdf)
    @Column(nullable = false)
    private String contentType; 
    
    // CRÍTICO: Ruta o clave donde se almacena el archivo (Ej: ruta en el disco, o clave S3)
    @Column(nullable = false)
    private String storagePath; 
    
    // Relación: ¿A qué empleado pertenece este documento?
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private Employee employee; 

    // Quién cargó el archivo (útil para auditoría)
    @Column(nullable = false)
    private String uploadedBy; 
    
    private LocalDateTime uploadDate = LocalDateTime.now();
    private String documentType; // Ej: 'Boleta de Pago', 'Contrato', 'Permiso'
}

package com.agro.control_asistencia_backend.document.model.entity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "request_types")
public class RequestType {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name; 

    @Column(name = "base_priority", nullable = false)
    private Integer basePriority = 1; // 1: Baja, 2: Media, 3: Alta, 4: Crítica

    @Column(name = "sla_days", nullable = false)
    private Integer slaDays = 7; // Días máximos de atención

    @Column(name = "requires_signature", nullable = false)
    private Boolean requiresSignature = false; // Si requiere firma del jefe

    @Column(name = "requires_attachment", nullable = false)
    private Boolean requiresAttachment = false; // Si requiere subir documento/prueba
}

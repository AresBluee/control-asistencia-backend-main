package com.agro.control_asistencia_backend.document.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "document_type")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private int basePriority; // 1 (low) - 4 (critical)
    private int slaDays;
    private boolean requiresRrhhApproval;
    private boolean requiresAdminSignature;

    // optional fields for future extensions
    private String description;
    private String templatePath;
}

package com.agro.control_asistencia_backend.document.model.entity;

import com.agro.control_asistencia_backend.employee.model.entity.Employee;
import com.agro.control_asistencia_backend.document.model.enums.DocumentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "employee_document",
       uniqueConstraints = {@UniqueConstraint(columnNames = "document_code")})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "document_code", nullable = false, unique = true)
    private String documentCode; // generated like EMPDOC-0001

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_type_id", nullable = false)
    private DocumentType documentType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentStatus status;

    private String documentPath; // filesystem path to generated / signed PDF

    @ElementCollection
    @CollectionTable(name = "document_observations", joinColumns = @JoinColumn(name = "document_id"))
    @Column(name = "observation")
    private List<String> observations = new ArrayList<>();

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

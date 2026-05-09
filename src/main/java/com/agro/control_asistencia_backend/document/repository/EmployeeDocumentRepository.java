package com.agro.control_asistencia_backend.document.repository;

import com.agro.control_asistencia_backend.document.model.entity.EmployeeDocument;
import com.agro.control_asistencia_backend.document.model.enums.DocumentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface EmployeeDocumentRepository extends JpaRepository<EmployeeDocument, Long> {
    Optional<EmployeeDocument> findByDocumentCode(String documentCode);
    List<EmployeeDocument> findByStatus(DocumentStatus status);
    List<EmployeeDocument> findByStatusIn(DocumentStatus... statuses);
}

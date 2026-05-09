package com.agro.control_asistencia_backend.document.repository;

import com.agro.control_asistencia_backend.document.model.entity.DocumentType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface DocumentTypeRepository extends JpaRepository<DocumentType, Long> {
    Optional<DocumentType> findByName(String name);
}

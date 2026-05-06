package com.agro.control_asistencia_backend.document.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.agro.control_asistencia_backend.document.model.entity.DocumentTemplate;

public interface DocumentTemplateRepository extends JpaRepository<DocumentTemplate, Long> {

}

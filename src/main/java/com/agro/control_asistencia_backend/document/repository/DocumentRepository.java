package com.agro.control_asistencia_backend.document.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.agro.control_asistencia_backend.document.model.entity.Document;
import com.agro.control_asistencia_backend.employee.model.entity.Employee;

public interface DocumentRepository extends JpaRepository<Document, Long>{

        List<Document> findByEmployee(Employee employee);

        Optional<Document> findByStoragePath(String storagePath);

}

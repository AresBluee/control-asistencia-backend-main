package com.agro.control_asistencia_backend.employee.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.agro.control_asistencia_backend.employee.model.entity.WorkPosition;

public interface WorkPositionRepository extends JpaRepository<WorkPosition, Long>{

    Optional<WorkPosition> findByName(String name); 
}

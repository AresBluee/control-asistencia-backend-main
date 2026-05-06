package com.agro.control_asistencia_backend.scheduling.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.agro.control_asistencia_backend.scheduling.model.entity.WorkSchedule;

public interface WorkScheduleRepository extends JpaRepository<WorkSchedule, Long>{
    Optional<WorkSchedule> findByName(String name);
}

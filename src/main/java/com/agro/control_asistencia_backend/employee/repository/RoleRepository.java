package com.agro.control_asistencia_backend.employee.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.agro.control_asistencia_backend.employee.model.entity.ERole;
import com.agro.control_asistencia_backend.employee.model.entity.Role;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(ERole name);
}

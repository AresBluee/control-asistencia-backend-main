package com.agro.control_asistencia_backend.employee.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.agro.control_asistencia_backend.employee.model.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Boolean existsByUsername(String unsername);

}

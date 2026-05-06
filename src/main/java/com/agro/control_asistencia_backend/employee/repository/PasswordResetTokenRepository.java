package com.agro.control_asistencia_backend.employee.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.agro.control_asistencia_backend.employee.model.entity.PasswordResetToken;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, String>{
    Optional<PasswordResetToken> findByToken(String token);
}

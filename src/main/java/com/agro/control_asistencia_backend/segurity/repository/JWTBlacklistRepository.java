package com.agro.control_asistencia_backend.segurity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.agro.control_asistencia_backend.segurity.model.Entity.JWTBlacklist;;

@Repository
public interface JWTBlacklistRepository extends JpaRepository<JWTBlacklist, Long>{
    boolean existsByToken(String token); 
}
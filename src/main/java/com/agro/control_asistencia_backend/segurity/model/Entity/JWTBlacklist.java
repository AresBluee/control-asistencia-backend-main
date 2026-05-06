package com.agro.control_asistencia_backend.segurity.model.Entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "jwt_blacklist")
public class JWTBlacklist {

     @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Almacenar el hash del token o el token completo (mejor el hash por tamaño)
    @Column(unique = true, nullable = false, length = 512)
    private String token; 

    @Column(nullable = false)
    private Long userId; 
    // CRÍTICO: Fecha de expiración para limpiar la tabla
    @Column(nullable = false)
    private Instant expiryDate;
}

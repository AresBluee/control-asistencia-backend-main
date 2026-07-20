package com.agro.control_asistencia_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ControlAsistenciaBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(ControlAsistenciaBackendApplication.class, args);
	}

	@org.springframework.context.annotation.Bean
	public org.springframework.boot.ApplicationRunner dropConstraint(org.springframework.jdbc.core.JdbcTemplate jdbcTemplate) {
		return args -> {
			try {
				// El nombre real de la restricción generada por Hibernate
				jdbcTemplate.execute("ALTER TABLE employee DROP CONSTRAINT IF EXISTS ukfopic1oh5oln2khj8eat6ino0;");
				// Por si acaso, también borramos la otra
				jdbcTemplate.execute("ALTER TABLE employee DROP CONSTRAINT IF EXISTS employee_email_key;");
				System.out.println("✅ Restricciones de unicidad de email eliminadas de la base de datos.");
			} catch (Exception e) {
				System.out.println("⚠️ No se pudo eliminar la restricción (tal vez ya no existe).");
			}
		};
	}
}

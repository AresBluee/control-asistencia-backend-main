package com.agro.control_asistencia_backend.document.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestTypeSchemaInitializer implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    public RequestTypeSchemaInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        boolean requiresAttachmentExists = columnExists("request_types", "requires_attachment");

        jdbcTemplate.execute("ALTER TABLE request_types ADD COLUMN IF NOT EXISTS base_priority INTEGER NOT NULL DEFAULT 1");
        jdbcTemplate.execute("ALTER TABLE request_types ADD COLUMN IF NOT EXISTS sla_days INTEGER NOT NULL DEFAULT 7");
        jdbcTemplate.execute("ALTER TABLE request_types ADD COLUMN IF NOT EXISTS requires_signature BOOLEAN NOT NULL DEFAULT FALSE");
        jdbcTemplate.execute("ALTER TABLE request_types ADD COLUMN IF NOT EXISTS requires_attachment BOOLEAN NOT NULL DEFAULT FALSE");

        if (!requiresAttachmentExists) {
            jdbcTemplate.update(
                    "UPDATE request_types SET requires_attachment = TRUE WHERE name = ?",
                    "Solicitud de Permiso M\u00e9dico");
        }
    }

    private boolean columnExists(String tableName, String columnName) {
        Integer count = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM information_schema.columns
                WHERE table_name = ? AND column_name = ?
                """,
                Integer.class,
                tableName,
                columnName);

        return count != null && count > 0;
    }
}

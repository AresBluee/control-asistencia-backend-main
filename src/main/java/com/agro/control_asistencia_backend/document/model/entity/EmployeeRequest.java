package com.agro.control_asistencia_backend.document.model.entity;
import java.time.LocalDate;
import java.time.LocalDateTime;
import com.agro.control_asistencia_backend.employee.model.entity.Employee;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "employee_requests")
public class EmployeeRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.EAGER) // Cargamos el tipo de solicitud inmediatamente
    @JoinColumn(name = "request_type_id", nullable = false)
    private RequestType requestType;

    // Campos de la solicitud
    private String details;
    private LocalDate startDate; // Fecha de inicio del permiso/vacación
    private LocalDate endDate; // Fecha de fin del permiso/vacación
private LocalDateTime requestedDate = LocalDateTime.now();
    // Estado de la gestión: PENDING, APPROVED, REJECTED
    @Column(length = 20, nullable = false)
    private String status = "PENDING";

    // Comentario del manager/RRHH al responder la solicitud
    // Comentario del manager/RRHH al responder la solicitud
    private String managerComment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id") // Puede ser nulo si aún no se asigna un manager
    private Employee manager;
}

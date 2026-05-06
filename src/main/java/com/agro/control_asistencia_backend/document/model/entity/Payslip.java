package com.agro.control_asistencia_backend.document.model.entity;
import java.math.BigDecimal;
import java.time.LocalDate;
import com.agro.control_asistencia_backend.employee.model.entity.Employee;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor

@Table(name = "payslips")
public class Payslip {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    private LocalDate periodStartDate;
    private LocalDate periodEndDate;
    private BigDecimal grossSalary;
    private BigDecimal bonuses;
    private BigDecimal deductions;
    private BigDecimal netSalary;
    private LocalDate generationDate;
    private String filePath;
}

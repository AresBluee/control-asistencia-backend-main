package com.agro.control_asistencia_backend.document.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.agro.control_asistencia_backend.document.model.entity.Payslip;
import com.agro.control_asistencia_backend.employee.model.entity.Employee;

@Repository
public interface PayslipRepository extends JpaRepository<Payslip, Long> {

    List<Payslip> findByEmployeeIdAndPeriodStartDateBetween(Long employeeId, LocalDate start, LocalDate end);

    List<Payslip> findByEmployee(Employee employee);

}

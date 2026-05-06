package com.agro.control_asistencia_backend.employee.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.agro.control_asistencia_backend.employee.model.entity.Employee;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Optional<Employee> findByBiometricHash(String biometricHash);

    Optional<Employee> findByEmployeeCode(String employeeCode);

    Optional<Employee> findByUserId(Long userId);

    Optional<Employee> findByEmail(String email);
}

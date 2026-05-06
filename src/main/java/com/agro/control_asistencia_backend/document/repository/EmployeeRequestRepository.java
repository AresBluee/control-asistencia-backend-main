package com.agro.control_asistencia_backend.document.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.agro.control_asistencia_backend.document.model.entity.EmployeeRequest;
import com.agro.control_asistencia_backend.employee.model.entity.Employee;

public interface EmployeeRequestRepository extends JpaRepository<EmployeeRequest, Long>{

        List<EmployeeRequest> findByEmployee(Employee employee);

        List<EmployeeRequest> findByEmployeeAndStatus(Employee employee, String status); 


}

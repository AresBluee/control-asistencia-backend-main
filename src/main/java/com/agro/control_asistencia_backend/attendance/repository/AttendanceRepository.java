package com.agro.control_asistencia_backend.attendance.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.agro.control_asistencia_backend.attendance.model.entity.AttendanceRecord;
import com.agro.control_asistencia_backend.employee.model.entity.Employee;

@Repository
public interface AttendanceRepository extends JpaRepository<AttendanceRecord, Long> {

    Optional<AttendanceRecord> findTopByEmployeeOrderByDeviceTimestampDesc(Employee employee);

    List<AttendanceRecord> findByEmployeeAndDeviceTimestampBetween(Employee employee, LocalDateTime startDate,
            LocalDateTime endDate);

    List<AttendanceRecord> findAllByEmployeeAndDeviceTimestampBetweenOrderByDeviceTimestampAsc(
            Employee employee,
            LocalDateTime startDate,
            LocalDateTime endDate);

            List<AttendanceRecord> findByEmployee(Employee employee);

List<AttendanceRecord> findByEmployeeOrderByDeviceTimestampDesc(Employee employee);
}

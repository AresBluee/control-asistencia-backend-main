package com.agro.control_asistencia_backend.attendance.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agro.control_asistencia_backend.attendance.model.entity.AttendanceRecord;
import com.agro.control_asistencia_backend.attendance.repository.AttendanceRepository;
import com.agro.control_asistencia_backend.document.model.dto.EmployeeStatusDTO;
import com.agro.control_asistencia_backend.employee.model.entity.Employee;
import com.agro.control_asistencia_backend.employee.repository.EmployeeRepository;

@Service
@Transactional(readOnly = true) 
public class AttendanceAdminService {

    private final EmployeeRepository employeeRepository;
    private final AttendanceRepository attendanceRepository;

    @Autowired
    public AttendanceAdminService(EmployeeRepository employeeRepository, 
                                  AttendanceRepository attendanceRepository) {
        this.employeeRepository = employeeRepository;
        this.attendanceRepository = attendanceRepository;
    }

    
    public List<EmployeeStatusDTO> getDailyAttendanceStatus(LocalDate date) {
        // 1. Obtener la lista de todos los empleados
        List<Employee> employees = employeeRepository.findAll();

        // 2. Procesar cada empleado para determinar su estado del día
        return employees.stream().map(employee -> {
            
            // Buscar el último registro del empleado
            Optional<AttendanceRecord> lastRecord = attendanceRepository
                .findTopByEmployeeOrderByDeviceTimestampDesc(employee); 

            EmployeeStatusDTO dto = new EmployeeStatusDTO();
            dto.setEmployeeId(employee.getId());
            dto.setEmployeeCode(employee.getEmployeeCode());
            dto.setFullName(employee.getFirstName() + " " + employee.getLastName());
            dto.setPosition(employee.getPosition().getName());            dto.setBiometricHash(employee.getBiometricHash());
            dto.setReportDate(date);
            
            // LÓGICA CLAVE PARA DETERMINAR EL ESTADO
            if (lastRecord.isEmpty() || lastRecord.get().getDeviceTimestamp().toLocalDate().isBefore(date)) {
                
                // Si no hay registros o el último registro es de un día anterior al de reporte.
                dto.setStatus("NO_REGISTRADO");
                dto.setLastMarkTime(null);
                
            } else {
                
                String type = lastRecord.get().getRecordType(); // "IN" o "OUT"
                dto.setLastMarkTime(lastRecord.get().getDeviceTimestamp().toLocalTime());
                
                if ("IN".equals(type)) {
                    dto.setStatus("ASISTIO"); // Entró pero NO ha salido
                } else {
                    dto.setStatus("SALIO"); // Entró y ya salió
                }
            }
            return dto;
        }).collect(Collectors.toList());
    }
}

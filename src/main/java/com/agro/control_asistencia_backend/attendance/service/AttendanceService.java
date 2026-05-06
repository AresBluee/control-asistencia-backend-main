package com.agro.control_asistencia_backend.attendance.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agro.control_asistencia_backend.attendance.model.dto.AttendanceRequestDTO;
import com.agro.control_asistencia_backend.attendance.model.dto.AttendanceResponseDTO;
import com.agro.control_asistencia_backend.attendance.model.entity.AttendanceHistoryDTO;
import com.agro.control_asistencia_backend.attendance.model.entity.AttendanceRecord;
import com.agro.control_asistencia_backend.attendance.repository.AttendanceRepository;
import com.agro.control_asistencia_backend.document.model.dto.EmployeeStatusDTO;
import com.agro.control_asistencia_backend.employee.model.entity.Employee;
import com.agro.control_asistencia_backend.employee.repository.EmployeeRepository;

@Service
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;

    @Autowired
    public AttendanceService(AttendanceRepository attendanceRepository, EmployeeRepository employeeRepository) {
        this.attendanceRepository = attendanceRepository;
        this.employeeRepository = employeeRepository;
    }

    // Método para manejar un único registro de asistencia (incluyendo los que
    // llegan tarde)
    @Transactional
    public AttendanceResponseDTO processAttendanceRecord(AttendanceRequestDTO requestDTO) {

        Optional<Employee> employeeOpt = employeeRepository.findByBiometricHash(requestDTO.getBiometricHash());

        if (employeeOpt.isEmpty()) {
            throw new RuntimeException("Biometric ID not recognized. Employee not found.");
        }
        Employee employee = employeeOpt.get();

        if (!isLocationValid(requestDTO.getLatitude(), requestDTO.getLongitude())) {

            System.err.println("ALERT: Attendance marked outside permitted area for " + employee.getEmployeeCode());
        }

        String recordType = determineRecordType(employee);

        AttendanceRecord newRecord = new AttendanceRecord();
        newRecord.setEmployee(employee);
        newRecord.setDeviceTimestamp(requestDTO.getDeviceTimestamp());
        newRecord.setSyncTimestamp(LocalDateTime.now()); // Hora en que llegó al servidor
        newRecord.setRecordType(recordType);
        newRecord.setLatitude(requestDTO.getLatitude());
        newRecord.setLongitude(requestDTO.getLongitude());
        AttendanceRecord savedRecord = attendanceRepository.save(newRecord);

        return AttendanceResponseDTO.builder()
                .id(savedRecord.getId())
                .employeeCode(employee.getEmployeeCode())
                .recordType(savedRecord.getRecordType())
                .deviceTimestamp(savedRecord.getDeviceTimestamp())
                .syncTimestamp(savedRecord.getSyncTimestamp())
                .latitude(savedRecord.getLatitude())
                .longitude(savedRecord.getLongitude())
                .build();
    }

    private String determineRecordType(Employee employee) {
        Optional<AttendanceRecord> lastRecord = attendanceRepository
                .findTopByEmployeeOrderByDeviceTimestampDesc(employee);

        if (lastRecord.isEmpty() || lastRecord.get().getRecordType().equals("OUT")) {
            return "IN";
        } else {
            return "OUT";
        }
    }

    private boolean isLocationValid(Double lat, Double lon) {
        return true;
    }

    @Transactional(readOnly = true)
    public List<EmployeeStatusDTO> getDailyAttendanceStatus(LocalDate date) {
        List<Employee> employees = employeeRepository.findAll();

        return employees.stream().map(employee -> {
            Optional<AttendanceRecord> lastRecord = attendanceRepository
                    .findTopByEmployeeOrderByDeviceTimestampDesc(employee);

            EmployeeStatusDTO dto = new EmployeeStatusDTO();
            dto.setEmployeeId(employee.getId());
            dto.setEmployeeCode(employee.getEmployeeCode());
            dto.setFullName(employee.getFirstName() + " " + employee.getLastName());
            dto.setPosition(employee.getPosition().getName());
            dto.setBiometricHash(employee.getBiometricHash());
            dto.setReportDate(date);

            if (lastRecord.isEmpty() || lastRecord.get().getDeviceTimestamp().toLocalDate().isBefore(date)) {
                dto.setStatus("NO_REGISTRADO");
                dto.setLastMarkTime(null);
            } else {
                String type = lastRecord.get().getRecordType();
                dto.setLastMarkTime(lastRecord.get().getDeviceTimestamp().toLocalTime());

                if ("IN".equals(type)) {
                    dto.setStatus("ASISTIO");
                } else {
                    dto.setStatus("SALIO");
                }
            }
            return dto;
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AttendanceRecord> getAttendanceByEmployeeCode(String employeeCode) {

        // 1. Encontrar al empleado usando su código
        Employee employee = employeeRepository.findByEmployeeCode(employeeCode)
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado para historial."));

        // 2. Usar el repositorio para obtener todos los registros de asistencia de ese
        // empleado
        return attendanceRepository.findByEmployee(employee);
    }

    @Transactional(readOnly = true)
    public List<AttendanceHistoryDTO> getAttendanceByUserId(Long userId) {

        // 1. Encontrar el Empleado por su ID de Usuario (findByUserId debe existir en
        // EmployeeRepository)
        Employee employee = employeeRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Perfil de Empleado no encontrado para el ID: " + userId));
        List<AttendanceRecord> records = attendanceRepository.findByEmployee(employee);
        return records.stream()
                .map(this::mapToHistoryDTO)
                .collect(Collectors.toList());
    }

    private AttendanceHistoryDTO mapToHistoryDTO(AttendanceRecord record) {
        // ⚠️ CRÍTICO: El getEmployee() debe ocurrir DENTRO del método transaccional de
        // llamada
        // o la relación debe ser EAGER. Asumimos que está dentro de una transacción.
        Employee employee = record.getEmployee();

        return AttendanceHistoryDTO.builder()
                .id(record.getId())
                .employeeCode(employee.getEmployeeCode())
                .recordType(record.getRecordType())
                .deviceTimestamp(record.getDeviceTimestamp())
                .syncTimestamp(record.getSyncTimestamp())
                .position(employee.getPosition().getName()) // Obtener el nombre legible
                .build();
    }
}

package com.agro.control_asistencia_backend.reporting.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.agro.control_asistencia_backend.attendance.model.entity.AttendanceRecord;
import com.agro.control_asistencia_backend.attendance.repository.AttendanceRepository;
import com.agro.control_asistencia_backend.employee.model.entity.Employee;
import com.agro.control_asistencia_backend.employee.repository.EmployeeRepository;
import com.agro.control_asistencia_backend.reporting.model.dto.DailyWorkSummary;
import com.agro.control_asistencia_backend.reporting.model.dto.EmployeeHourSummaryDTO;
import com.agro.control_asistencia_backend.scheduling.service.ScheduleService;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

@Service
@Transactional(readOnly = true)
public class ReportingService {

    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;
    private final ScheduleService scheduleService;

    @Autowired
    public ReportingService(AttendanceRepository attendanceRepository,
            EmployeeRepository employeeRepository,
            ScheduleService scheduleService) {
        this.attendanceRepository = attendanceRepository;
        this.employeeRepository = employeeRepository;
        this.scheduleService = scheduleService;
    }

    /**
     * Calcula el resumen de horas trabajadas para un empleado en un rango de
     * fechas.
     * Este es el método principal llamado por el Controller.
     */
    public List<DailyWorkSummary> getEmployeeWorkSummary(Long employeeId, LocalDate startDate, LocalDate endDate) {

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found for reporting."));

        // 1. Obtener todos los registros en el rango de fechas
        List<AttendanceRecord> records = attendanceRepository
                .findAllByEmployeeAndDeviceTimestampBetweenOrderByDeviceTimestampAsc(
                        employee,
                        startDate.atStartOfDay(),
                        endDate.atTime(LocalTime.MAX));

        // 2. Agrupar registros por fecha
        Map<LocalDate, List<AttendanceRecord>> recordsByDate = records.stream()
                .collect(Collectors.groupingBy(
                        record -> record.getDeviceTimestamp().toLocalDate()));

        // 3. Procesar cada día y calcular la duración (Llamada al método auxiliar)
        return recordsByDate.entrySet().stream()
                .map(entry -> calculateDailySummary(employee, entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(DailyWorkSummary::getDate))
                .collect(Collectors.toList());
    }

    /**
     * MÉTODO DE PUENTE (NUEVO) PARA LLAMADAS EXTERNAS (ej. PayslipService)
     * Resuelve el error de la cantidad incorrecta de argumentos.
     */
    public DailyWorkSummary getDailySummaryByEmployeeAndDate(Long employeeId, LocalDate date) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found for reporting."));

        List<AttendanceRecord> dailyRecords = attendanceRepository
                .findAllByEmployeeAndDeviceTimestampBetweenOrderByDeviceTimestampAsc(
                        employee,
                        date.atStartOfDay(),
                        date.atTime(LocalTime.MAX));

        return calculateDailySummary(employee, date, dailyRecords);
    }

    /**
     * Lógica para calcular la duración total de la jornada y las HORAS EXTRA.
     */
    private DailyWorkSummary calculateDailySummary(Employee employee, LocalDate date,
            List<AttendanceRecord> dailyRecords) {

        long totalMinutes = 0;
        long overtimeMinutes = 0;

        dailyRecords.sort(Comparator.comparing(AttendanceRecord::getDeviceTimestamp));

        LocalDateTime checkIn = null;
        LocalDateTime checkOut = null;

        // 1. Iterar para encontrar pares IN/OUT (Calcula las horas brutas)
        for (AttendanceRecord record : dailyRecords) {
            if ("IN".equals(record.getRecordType())) {
                checkIn = record.getDeviceTimestamp();
            } else if ("OUT".equals(record.getRecordType()) && checkIn != null) {
                checkOut = record.getDeviceTimestamp();
                totalMinutes += ChronoUnit.MINUTES.between(checkIn, checkOut);
                checkIn = null; // Reiniciar para el siguiente ciclo
            }
        }

        // 2. LÓGICA DE HORAS EXTRA (Compara la salida real vs. la programada)
        if (checkOut != null) {

            LocalTime scheduledEndTime = scheduleService.getScheduledEndTime(employee.getId(), date);

            if (scheduledEndTime != null) {
                LocalDateTime scheduledOutDateTime = LocalDateTime.of(date, scheduledEndTime);

                // Si la salida real fue después de la programada
                if (checkOut.isAfter(scheduledOutDateTime)) {

                    Duration extraDuration = Duration.between(scheduledOutDateTime, checkOut);
                    long totalExtraMinutes = extraDuration.toMinutes();

                    // Aplicar la regla: "si es que pasa una hora..." (60 minutos de tolerancia)
                    if (totalExtraMinutes > 60) {
                        long toleranceMinutes = 60;

                        // Tiempo extra pagable: total que excedió - 60 minutos de tolerancia
                        overtimeMinutes = totalExtraMinutes - toleranceMinutes;

                        // Recalcular totalMinutes (restar el tiempo que se va a pagar como extra)
                        totalMinutes -= overtimeMinutes;
                    }
                }
            }
        }

        // 3. Devolver DTO de Reporte con Horas Extra
        boolean isComplete = dailyRecords.stream()
                .anyMatch(r -> r.getRecordType().equals("OUT")) && checkIn == null;

        LocalTime firstIn = dailyRecords.stream()
                .filter(r -> "IN".equals(r.getRecordType()))
                .findFirst()
                .map(r -> r.getDeviceTimestamp().toLocalTime()).orElse(null);

        LocalTime lastOut = dailyRecords.stream()
                .filter(r -> "OUT".equals(r.getRecordType()))
                .reduce((a, b) -> b)
                .map(r -> r.getDeviceTimestamp().toLocalTime()).orElse(null);

        return DailyWorkSummary.builder()
                .employeeId(employee.getId())
                .employeeCode(employee.getEmployeeCode())
                .date(date)
                .checkInTime(firstIn)
                .checkOutTime(lastOut)
                .totalDuration(formatDuration(totalMinutes))
                .totalMinutes(totalMinutes)
                .overtimeMinutes(overtimeMinutes)
                .isComplete(isComplete)
                .build();
    }

    private String formatDuration(long totalMinutes) {
        if (totalMinutes < 0)
            totalMinutes = 0;
        long hours = totalMinutes / 60;
        long minutes = totalMinutes % 60;
        return String.format("%dh %dm", hours, minutes);
    }

    @Transactional(readOnly = true)
    public EmployeeHourSummaryDTO getEmployeeHourSummary(Long employeeId, LocalDate startDate, LocalDate endDate) {

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado."));

        long totalOvertimeMins = 0;

        // Itera sobre el rango de fechas y acumula horas extra
        for (LocalDate date = startDate; date.isBefore(endDate.plusDays(1)); date = date.plusDays(1)) {
            // Usar el método de puente que creamos
            DailyWorkSummary summary = getDailySummaryByEmployeeAndDate(employeeId, date);
            totalOvertimeMins += summary.getOvertimeMinutes();
        }

        // Formatear las horas extra totales
        long hours = totalOvertimeMins / 60;
        long minutes = totalOvertimeMins % 60;
        String formattedDuration = String.format("%dh %dm", hours, minutes);

        return EmployeeHourSummaryDTO.builder()
                .employeeId(employeeId)
                .fixedSalary(employee.getFixedSalary())
                .totalOvertimeMinutes(totalOvertimeMins)
                .totalOvertimeDuration(formattedDuration)
                .build();
    }

    public Resource generateGlobalAttendanceFile(LocalDate start, LocalDate end) {
    // 1. Obtener datos: Llamar a repositorios para obtener TODAS las asistencias en el rango.
    
    // 2. Lógica de generación de archivo (Simulado): Crear un archivo temporal
    try {
        String content = "Fecha,Codigo,Nombre,Entrada,Salida,Duracion\n" + 
                         "2025-09-01,AGR001,Maria Lopez,07:00,15:00,8h\n" + 
                         "2025-09-01,AGR002,Juan Perez,NO REGISTRO,--,--";
        
        // Crear un archivo temporal en memoria o en el disco
        Path tempFile = Files.createTempFile("global_report", ".csv");
        Files.write(tempFile, content.getBytes());
        
        
        return new UrlResource(tempFile.toUri());

    } catch (IOException e) {
        throw new RuntimeException("Error al generar archivo global: " + e.getMessage());
    }
}
}

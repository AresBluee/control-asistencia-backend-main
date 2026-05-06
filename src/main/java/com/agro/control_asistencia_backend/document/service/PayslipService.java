package com.agro.control_asistencia_backend.document.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.agro.control_asistencia_backend.document.model.dto.PayslipResponseDTO;
import com.agro.control_asistencia_backend.document.model.entity.Payslip;
import com.agro.control_asistencia_backend.document.repository.PayslipRepository;
import com.agro.control_asistencia_backend.employee.model.entity.Employee;
import com.agro.control_asistencia_backend.employee.repository.EmployeeRepository;
import com.agro.control_asistencia_backend.reporting.model.dto.DailyWorkSummary;
import com.agro.control_asistencia_backend.reporting.service.ReportingService;

@Service
public class PayslipService {

    @Autowired
    private EmployeeRepository employeeRepo;
    @Autowired
    private ReportingService reportingService;
    @Autowired
    private PayslipRepository payslipRepo;
    @Autowired
    private DocumentService documentService;

   
    
    public List<Payslip> getPayslipsByEmployee(Long employeeId) {
        Employee employee = employeeRepo.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado."));
        return payslipRepo.findByEmployee(employee);
    }

    private PayslipResponseDTO mapToPayslipResponseDTO(Payslip payslip) {
        Employee employee = payslip.getEmployee();
        return PayslipResponseDTO.builder()
                .id(payslip.getId())
                .employeeId(employee.getId())
                .employeeName(employee.getFirstName() + " " + employee.getLastName())
                .employeeCode(employee.getEmployeeCode())
                .periodStartDate(payslip.getPeriodStartDate())
                .periodEndDate(payslip.getPeriodEndDate())
                .grossSalary(payslip.getGrossSalary())
                .bonuses(payslip.getBonuses())
                .deductions(payslip.getDeductions())
                .netSalary(payslip.getNetSalary())
                .generationDate(payslip.getGenerationDate())
                .filePath(payslip.getFilePath())
                .build();
    }

    /**
     * Método principal para generar una Boleta de Pago para un período.
     * Este método es el que debes usar.
     */
    @Transactional
    public PayslipResponseDTO generatePayslip(Long employeeId, LocalDate start, LocalDate end) {

        Employee employee = employeeRepo.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado para generar boleta."));

        // 1. Acumular Horas de Asistencia
        long totalRegularMins = 0;
        long totalOvertimeMins = 0;

        // Itera sobre el rango de fechas
        for (LocalDate date = start; date.isBefore(end.plusDays(1)); date = date.plusDays(1)) {
            // Usa el nuevo método de ReportingService
            DailyWorkSummary summary = reportingService.getDailySummaryByEmployeeAndDate(employeeId, date);

            totalRegularMins += summary.getTotalMinutes();
            totalOvertimeMins += summary.getOvertimeMinutes();
        }

        // 2. Calcular Pago Monetario
        // Tarifa por minuto: (HourlyRate / 60)
        BigDecimal ratePerMinute = employee.getHourlyRate().divide(BigDecimal.valueOf(60), 4, RoundingMode.HALF_UP);

        // Tarifa por minuto para la hora extra (ej: 50% extra)
        BigDecimal overtimeRatePerMinute = ratePerMinute.multiply(BigDecimal.valueOf(1.5));

        // Cálculo del pago
        BigDecimal regularPay = ratePerMinute.multiply(BigDecimal.valueOf(totalRegularMins)).setScale(2,
                RoundingMode.HALF_UP);
        BigDecimal overtimePay = overtimeRatePerMinute.multiply(BigDecimal.valueOf(totalOvertimeMins)).setScale(2,
                RoundingMode.HALF_UP);

        // Sueldo Fijo + Pago por Horas Trabajadas (Normales + Extra)
        BigDecimal grossSalary = employee.getFixedSalary().add(regularPay).add(overtimePay).setScale(2, RoundingMode.HALF_UP);
        // Aquí podrías añadir lógica para deducciones y bonificaciones si las tuvieras
        BigDecimal deductions = BigDecimal.ZERO; // Ejemplo, si no se calculan aquí
        BigDecimal bonuses = BigDecimal.ZERO; // Ejemplo, si no se calculan aquí

        BigDecimal netSalary = grossSalary.add(bonuses).subtract(deductions).setScale(2, RoundingMode.HALF_UP);

        // 3. Crear y SETEAR la Entidad Payslip
        Payslip payslip = new Payslip();
        payslip.setEmployee(employee);
        payslip.setPeriodStartDate(start);
        payslip.setPeriodEndDate(end);

        // Setear los valores calculados:
        payslip.setGrossSalary(grossSalary);
        payslip.setBonuses(bonuses);
        payslip.setDeductions(deductions);
        payslip.setNetSalary(netSalary);
        payslip.setGenerationDate(LocalDate.now());

        // 4. Generar y Almacenar el PDF (Simulado)
        String filePath = documentService.generatePayslipPdf(payslip);
        payslip.setFilePath(filePath);

        Payslip savedPayslip = payslipRepo.save(payslip);
        return mapToPayslipResponseDTO(savedPayslip);
    }

    @Transactional(readOnly = true)
    public List<PayslipResponseDTO> getAllPayslips() {
        return payslipRepo.findAll().stream().map(this::mapToPayslipResponseDTO).collect(java.util.stream.Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Payslip> getPayslipsByEmployeeCode(String employeeCode) {
    // 1. Obtener el objeto Employee
    Employee employee = employeeRepo.findByEmployeeCode(employeeCode)
        .orElseThrow(() -> new RuntimeException("Empleado no encontrado."));
        
    // 2. Usar el repositorio de Payslips para filtrar
    return payslipRepo.findByEmployee(employee);
}

    @Transactional(readOnly = true)
    public List<PayslipResponseDTO> getPayslipsByUserId(Long userId) {
        Employee employee = employeeRepo.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Perfil no encontrado."));
        return payslipRepo.findByEmployee(employee).stream().map(this::mapToPayslipResponseDTO).collect(java.util.stream.Collectors.toList());
    }
}

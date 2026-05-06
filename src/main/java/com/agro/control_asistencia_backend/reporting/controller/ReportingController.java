package com.agro.control_asistencia_backend.reporting.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


import com.agro.control_asistencia_backend.employee.service.EmployeeService;
import com.agro.control_asistencia_backend.reporting.model.dto.DailyWorkSummary;


import com.agro.control_asistencia_backend.reporting.service.ReportingService;
import com.agro.control_asistencia_backend.segurity.service.UserDetailsImpl;
import org.springframework.core.io.Resource;


@RestController
@RequestMapping("/api/reports")
public class ReportingController {

    private final ReportingService reportingService;
    private final EmployeeService employeeService;

    @Autowired
    public ReportingController(ReportingService reportingService, EmployeeService employeeService) {
        this.reportingService = reportingService;
        this.employeeService = employeeService;
    }

    // Endpoint: GET
    // /api/reports/employee/{employeeId}?start=2024-01-01&end=2024-01-31
    // Permite a ADMIN y RRHH solicitar el reporte de un empleado.

    @GetMapping("/employee/{employeeId}")

    public ResponseEntity<List<DailyWorkSummary>> getEmployeeReport(
            @PathVariable Long employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {

        List<DailyWorkSummary> summary = reportingService.getEmployeeWorkSummary(employeeId, start, end);
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/me")
 // Asegura que el trabajador pueda verlo
    public ResponseEntity<List<DailyWorkSummary>> getMyReport(
        
            @AuthenticationPrincipal UserDetailsImpl userDetails, // Obtiene el ID del usuario logueado
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {

        Long employeeId = userDetails.getId();

        List<DailyWorkSummary> summary = reportingService.getEmployeeWorkSummary(employeeId, start, end);
        return ResponseEntity.ok(summary);  
    }

    @GetMapping("/global/attendance")
    
    public ResponseEntity<Resource> downloadGlobalAttendanceReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {

        // 1. Llama al servicio para generar el archivo consolidado
        Resource fileResource = reportingService.generateGlobalAttendanceFile(start, end); // Método a crear

        // 2. Configuración de la respuesta para forzar la descarga de un archivo
        // CSV/Excel
        String filename = "reporte_asistencia_global_" + start + "_a_" + end + ".csv";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv")) // O application/vnd.ms-excel
                .body(fileResource);
    }

}

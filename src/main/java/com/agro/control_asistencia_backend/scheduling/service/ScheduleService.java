package com.agro.control_asistencia_backend.scheduling.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agro.control_asistencia_backend.employee.model.entity.Employee;
import com.agro.control_asistencia_backend.employee.repository.EmployeeRepository;
import com.agro.control_asistencia_backend.scheduling.model.dto.EmployeeScheduleAssignmentDTO;
import com.agro.control_asistencia_backend.scheduling.model.dto.ScheduleResponseDTO;
import com.agro.control_asistencia_backend.scheduling.model.dto.WorkScheduleDTO;
import com.agro.control_asistencia_backend.scheduling.model.entity.EmployeeSchedule;
import com.agro.control_asistencia_backend.scheduling.model.entity.WorkSchedule;
import com.agro.control_asistencia_backend.scheduling.repository.EmployeeScheduleRepository;
import com.agro.control_asistencia_backend.scheduling.repository.WorkScheduleRepository;

@Service
public class ScheduleService {

    private final WorkScheduleRepository workScheduleRepository;
    private final EmployeeScheduleRepository employeeScheduleRepository;
    private final EmployeeRepository employeeRepository;

    @Autowired
    public ScheduleService(WorkScheduleRepository workScheduleRepository,
            EmployeeScheduleRepository employeeScheduleRepository,
            EmployeeRepository employeeRepository) {
        this.workScheduleRepository = workScheduleRepository;
        this.employeeScheduleRepository = employeeScheduleRepository;
        this.employeeRepository = employeeRepository;
    }

    // ---------------------------------------------------------------------
    // Lógica para crear turnos (WorkSchedule)
    // ---------------------------------------------------------------------

    @Transactional
    public WorkSchedule createWorkSchedule(WorkScheduleDTO dto) {
        WorkSchedule schedule = new WorkSchedule();
        schedule.setName(dto.getName());
        schedule.setStartTime(dto.getStartTime());
        schedule.setEndTime(dto.getEndTime());
        schedule.setToleranceMinutes(dto.getToleranceMinutes());

        return workScheduleRepository.save(schedule);
    }

    // ---------------------------------------------------------------------
    // Lógica para asignar turnos a empleados (EmployeeSchedule)
    // ---------------------------------------------------------------------

    @Transactional
    public ScheduleResponseDTO assignScheduleToEmployee(EmployeeScheduleAssignmentDTO dto) {

        // 1. Validar existencia del Empleado
        Employee employee = employeeRepository.findById(dto.getEmployeeId())
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado con ID: " + dto.getEmployeeId()));

        // 2. Validar existencia del Turno
        WorkSchedule schedule = workScheduleRepository.findById(dto.getScheduleId())
                .orElseThrow(
                        () -> new RuntimeException("Turno de trabajo no encontrado con ID: " + dto.getScheduleId()));

        EmployeeSchedule assignment = new EmployeeSchedule();
        assignment.setEmployee(employee);
        assignment.setWorkSchedule(schedule);
        assignment.setValidFrom(dto.getValidFrom());
        assignment.setValidTo(dto.getValidTo());
        assignment.setWorkingDays(dto.getWorkingDays());

        EmployeeSchedule savedAssignment = employeeScheduleRepository.save(assignment);

        return ScheduleResponseDTO.builder()
                .id(assignment.getId())
                .employeeId(employee.getId())
                .employeeCode(employee.getEmployeeCode())
                .scheduleName(schedule.getName())
                .startTime(schedule.getStartTime())
                .endTime(schedule.getEndTime())
                .validFrom(savedAssignment.getValidFrom())
                .validTo(savedAssignment.getValidTo())
                .workingDays(savedAssignment.getWorkingDays())
                .build();
    }

    private ScheduleResponseDTO mapToResponseDTO(EmployeeSchedule assignment) {
        WorkSchedule schedule = assignment.getWorkSchedule(); // Acceder a la relación dentro del @Transactional
        
        return ScheduleResponseDTO.builder()
                .id(assignment.getId())
                .validFrom(assignment.getValidFrom())
                .validTo(assignment.getValidTo())
                .workingDays(assignment.getWorkingDays())
                .scheduleId(schedule.getId())
                .scheduleName(schedule.getName())
                .startTime(schedule.getStartTime())
                .endTime(schedule.getEndTime())
                .toleranceMinutes(schedule.getToleranceMinutes())
                .build();
    }
    @Transactional(readOnly = true)
    public Optional<ScheduleResponseDTO> getEmployeeScheduleByUserId(Long userId, LocalDate date) {

        Employee employee = employeeRepository.findByUserId(userId)
            .orElseThrow(() -> new RuntimeException("Empleado no encontrado para la asignación de horario."));
        Optional<EmployeeSchedule> scheduleOpt = employeeScheduleRepository
                .findTopByEmployeeOrderByValidFromDesc(employee);

        if (scheduleOpt.isEmpty()) {
            return Optional.empty();
        }

        // 💡 CRÍTICO: Mapear la entidad al DTO
        return scheduleOpt.map(this::mapToResponseDTO);

    }
  

    @Transactional(readOnly = true)
    public LocalTime getScheduledEndTime(Long employeeId, LocalDate date) {

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado."));

        Optional<EmployeeSchedule> activeAssignment = employeeScheduleRepository
                .findTopByEmployeeOrderByValidFromDesc(employee);

        if (activeAssignment.isPresent() && activeAssignment.get().getWorkSchedule() != null) {

            return activeAssignment.get().getWorkSchedule().getEndTime();
        }

        return LocalTime.of(17, 0);
    }

    @Transactional(readOnly = true)
    public List<WorkSchedule> getAllWorkSchedules() {
        return workScheduleRepository.findAll();
    }
}

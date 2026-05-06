package com.agro.control_asistencia_backend.employee.config;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.agro.control_asistencia_backend.attendance.model.entity.AttendanceRecord;
import com.agro.control_asistencia_backend.attendance.repository.AttendanceRepository;
import com.agro.control_asistencia_backend.document.model.entity.EmployeeRequest;
import com.agro.control_asistencia_backend.document.model.entity.Payslip;
import com.agro.control_asistencia_backend.document.model.entity.RequestType;
import com.agro.control_asistencia_backend.document.repository.EmployeeRequestRepository;
import com.agro.control_asistencia_backend.document.repository.PayslipRepository;
import com.agro.control_asistencia_backend.document.repository.RequestTypeRepository;
import com.agro.control_asistencia_backend.employee.model.entity.ERole;
import com.agro.control_asistencia_backend.employee.model.entity.Employee;
import com.agro.control_asistencia_backend.employee.model.entity.Role;
import com.agro.control_asistencia_backend.employee.model.entity.User;
import com.agro.control_asistencia_backend.employee.model.entity.WorkPosition;
import com.agro.control_asistencia_backend.employee.repository.EmployeeRepository;
import com.agro.control_asistencia_backend.employee.repository.RoleRepository;
import com.agro.control_asistencia_backend.employee.repository.UserRepository;
import com.agro.control_asistencia_backend.employee.repository.WorkPositionRepository;
import com.agro.control_asistencia_backend.scheduling.model.entity.EmployeeSchedule;
import com.agro.control_asistencia_backend.scheduling.model.entity.WorkSchedule;
import com.agro.control_asistencia_backend.scheduling.repository.EmployeeScheduleRepository;
import com.agro.control_asistencia_backend.scheduling.repository.WorkScheduleRepository;

@Component
public class DataLoader implements CommandLineRunner {

        private final RoleRepository roleRepository;
        private final UserRepository userRepository;
        private final EmployeeRepository employeeRepository;
        private final WorkPositionRepository positionRepository;
        private final AttendanceRepository attendanceRepository;
        private final WorkScheduleRepository scheduleRepository;
        private final EmployeeScheduleRepository employeeScheduleRepository;
        private final EmployeeRequestRepository employeeRequestRepository;
        private final RequestTypeRepository requestTypeRepository;
        private final PayslipRepository payslipRepository;
        private final PasswordEncoder passwordEncoder;

        @Autowired
        public DataLoader(RoleRepository roleRepository, UserRepository userRepository,
                        EmployeeRepository employeeRepository, WorkPositionRepository positionRepository,
                        AttendanceRepository attendanceRepository, WorkScheduleRepository scheduleRepository,
                        EmployeeScheduleRepository employeeScheduleRepository,
                        EmployeeRequestRepository employeeRequestRepository,
                        RequestTypeRepository requestTypeRepository, PayslipRepository payslipRepository,
                        PasswordEncoder passwordEncoder) {
                this.roleRepository = roleRepository;
                this.userRepository = userRepository;
                this.employeeRepository = employeeRepository;
                this.positionRepository = positionRepository;
                this.attendanceRepository = attendanceRepository;
                this.scheduleRepository = scheduleRepository;
                this.employeeScheduleRepository = employeeScheduleRepository;
                this.employeeRequestRepository = employeeRequestRepository;
                this.requestTypeRepository = requestTypeRepository;
                this.payslipRepository = payslipRepository;
                this.passwordEncoder = passwordEncoder;
        }

        @Override
        @Transactional
        public void run(String... args) throws Exception {
                if (roleRepository.count() == 0) {
                        loadRolesAndPositionsAndSchedules();
                }
                if (userRepository.count() < 2) {
                        loadUsersAndEmployeeData();
                        loadAttendanceAndRequestsAndPayslips();
                }
        }

        private void loadRolesAndPositionsAndSchedules() {
                for (ERole roleName : ERole.values()) {
                        if (roleRepository.findByName(roleName).isEmpty()) {
                                Role role = new Role();
                                role.setName(roleName);
                                roleRepository.save(role);
                        }
                }
                createPosition("Gerente General");
                createPosition("Subgerente de Operaciones");
                createPosition("Especialista RRHH");
                createPosition("Supervisor de Campo");
                createPosition("Supervisor de Mantenimiento");
                createPosition("Cosechador");
                createPosition("Operador de Maquinaria");
                createPosition("Técnico de Mantenimiento");
                createPosition("Asistente Administrativo");
                createPosition("Contador");

                createRequestType("Permiso Personal");
                createRequestType("Solicitud de Vacaciones");
                createRequestType("Solicitud de Constancia de Trabajo");
                createRequestType("Solicitud de Permiso Médico");
                createRequestType("Solicitud de Cambio de Turno");
                createRequestType("Solicitud de Capacitación");

                createWorkSchedule("Turno Cosecha Mañana", LocalTime.of(7, 0), LocalTime.of(16, 0));
                createWorkSchedule("Turno Cosecha Tarde", LocalTime.of(15, 0), LocalTime.of(23, 0));
                createWorkSchedule("Turno Administrativo", LocalTime.of(8, 0), LocalTime.of(17, 0));

                System.out.println("-> Roles, Posiciones, Tipos de Solicitud y Horarios de Trabajo cargados.");
        }

        private Employee createUserAndEmployee(String username, String password, String code, String first, String last,
                        String dni, String email, String positionName, double fixed, double hourly, ERole roleEnum,
                        WorkPosition position) {
                if (userRepository.findByUsername(username).isPresent())
                        return null;

                Role role = roleRepository.findByName(roleEnum)
                                .orElseThrow(() -> new RuntimeException("Rol " + roleEnum.name() + " no encontrado."));
                WorkPosition posEntity = positionRepository.findByName(positionName)
                                .orElseThrow(() -> new RuntimeException("Cargo no encontrado: " + positionName));

                User user = new User();
                user.setUsername(username);
                user.setPassword(passwordEncoder.encode(password));
                user.setRole(role);
                user = userRepository.save(user);

                Employee employee = new Employee();
                employee.setEmployeeCode(code);
                employee.setFirstName(first);
                employee.setLastName(last);
                employee.setDni(dni);
                employee.setEmail(email);
                employee.setPhoneNumber("9" + dni.substring(2));
                employee.setAddress("Calle Falsa 123, Ica");
                employee.setBiometricHash("HASH-" + code + "-UNIQUE-ID-78901234567890123456");
                employee.setFixedSalary(new BigDecimal(fixed));
                employee.setHourlyRate(new BigDecimal(hourly));
                employee.setHireDate(LocalDate.now());

                employee.setUser(user);
                employee.setPosition(posEntity);

                return employeeRepository.save(employee);
        }

        private void loadUsersAndEmployeeData() {
                Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                                .orElseThrow(() -> new RuntimeException("Rol ADMIN no encontrado."));
                Role empleadoRole = roleRepository.findByName(ERole.ROLE_EMPLOYEE)
                                .orElseThrow(() -> new RuntimeException("Rol Empleado no encontrado."));
                Role rrhhRole = roleRepository.findByName(ERole.ROLE_RRHH)
                                .orElseThrow(() -> new RuntimeException("Rol RRHH no encontrado."));
                Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                                .orElseThrow(() -> new RuntimeException("Rol USER no encontrado."));
                Role supervisorRole = roleRepository.findByName(ERole.ROLE_SUPERVISOR)
                                .orElseThrow(() -> new RuntimeException("Rol SUPERVISOR no encontrado."));

                WorkPosition gerencia = positionRepository.findByName("Gerente General")
                                .orElseThrow(() -> new RuntimeException("Cargo 'Gerente General' no encontrado."));
                WorkPosition subgerencia = positionRepository.findByName("Subgerente de Operaciones")
                                .orElseThrow(() -> new RuntimeException(
                                                "Cargo 'Subgerente de Operaciones' no encontrado."));
                WorkPosition rrhhPos = positionRepository.findByName("Especialista RRHH")
                                .orElseThrow(() -> new RuntimeException("Cargo 'Especialista RRHH' no encontrado."));
                WorkPosition supervisorCampo = positionRepository.findByName("Supervisor de Campo")
                                .orElseThrow(() -> new RuntimeException("Cargo 'Supervisor de Campo' no encontrado."));
                WorkPosition supervisorMantenimiento = positionRepository.findByName("Supervisor de Mantenimiento")
                                .orElseThrow(() -> new RuntimeException(
                                                "Cargo 'Supervisor de Mantenimiento' no encontrado."));
                WorkPosition cosechador = positionRepository.findByName("Cosechador")
                                .orElseThrow(() -> new RuntimeException("Cargo 'Cosechador' no encontrado."));
                WorkPosition operadorMaquinaria = positionRepository.findByName("Operador de Maquinaria")
                                .orElseThrow(() -> new RuntimeException(
                                                "Cargo 'Operador de Maquinaria' no encontrado."));
                WorkPosition tecnicoMantenimiento = positionRepository.findByName("Técnico de Mantenimiento")
                                .orElseThrow(() -> new RuntimeException(
                                                "Cargo 'Técnico de Mantenimiento' no encontrado."));
                WorkPosition asistenteAdmin = positionRepository.findByName("Asistente Administrativo")
                                .orElseThrow(() -> new RuntimeException(
                                                "Cargo 'Asistente Administrativo' no encontrado."));
                WorkPosition contador = positionRepository.findByName("Contador")
                                .orElseThrow(() -> new RuntimeException("Cargo 'Contador' no encontrado."));

                // ADMINISTRADORES
                createUserAndEmployee("admin", "admin123", "ADM-001", "Carlos", "Mendoza", "12345678",
                                "carlos.mendoza@agrocyt.com", "Gerente General", 6000.00, 60.00, adminRole.getName(),
                                gerencia);
                createUserAndEmployee("subadmin", "subadmin123", "ADM-002", "Roberto", "Silva", "87654321",
                                "roberto.silva@agrocyt.com", "Subgerente de Operaciones", 4500.00, 45.00,
                                adminRole.getName(),
                                subgerencia);

                // ESPECIALISTA RRHH
                Employee rrhhEmployee = createUserAndEmployee("rrhh", "rrhh123", "RRHH-001", "Ana", "Gómez", "88776655",
                                "ana.gomez@agrocyt.com", "Especialista RRHH", 3500.00, 35.00, rrhhRole.getName(),
                                rrhhPos);

                // SUPERVISORES
                Employee supervisor1 = createUserAndEmployee("supervisor1", "sup123", "SUP-001", "Miguel", "Torres",
                                "11223344",
                                "miguel.torres@agrocyt.com", "Supervisor de Campo", 2800.00, 28.00,
                                supervisorRole.getName(),
                                supervisorCampo);
                Employee supervisor2 = createUserAndEmployee("supervisor2", "sup123", "SUP-002", "Elena", "Vargas",
                                "55667788",
                                "elena.vargas@agrocyt.com", "Supervisor de Mantenimiento", 2800.00, 28.00,
                                supervisorRole.getName(),
                                supervisorMantenimiento);

                // TRABAJADORES (USER)
                Employee maria = createUserAndEmployee("maria", "pass123", "EMP-001", "María", "López", "45454545",
                                "maria.lopez@agrocyt.com", "Cosechador", 1200.00, 15.00, empleadoRole.getName(),
                                cosechador);
                Employee juan = createUserAndEmployee("juan", "pass123", "EMP-002", "Juan", "Pérez", "32323232",
                                "juan.perez@agrocyt.com", "Cosechador", 1200.00, 15.00, empleadoRole.getName(),
                                cosechador);
                Employee pedro = createUserAndEmployee("pedro", "pass123", "EMP-003", "Pedro", "Ramírez", "99887766",
                                "pedro.ramirez@agrocyt.com", "Operador de Maquinaria", 1500.00, 18.00,
                                empleadoRole.getName(),
                                operadorMaquinaria);
                Employee lucia = createUserAndEmployee("lucia", "pass123", "EMP-004", "Lucía", "Fernández", "55443322",
                                "lucia.fernandez@agrocyt.com", "Técnico de Mantenimiento", 1400.00, 17.00,
                                empleadoRole.getName(),
                                tecnicoMantenimiento);
                Employee carmen = createUserAndEmployee("carmen", "pass123", "EMP-005", "Carmen", "Ruiz", "77665544",
                                "carmen.ruiz@agrocyt.com", "Asistente Administrativo", 1300.00, 16.00,
                                empleadoRole.getName(),
                                asistenteAdmin);
                Employee diego = createUserAndEmployee("diego", "pass123", "EMP-006", "Diego", "Morales", "33445566",
                                "diego.morales@agrocyt.com", "Contador", 2000.00, 25.00, empleadoRole.getName(),
                                contador);

                System.out.println("-> Usuarios y Perfiles de Empleado creados.");
        }

        private void loadAttendanceAndRequestsAndPayslips() {
                Employee maria = employeeRepository.findByEmployeeCode("EMP-001")
                                .orElseThrow(() -> new RuntimeException("Empleado 'maria' no encontrado."));
                Employee juan = employeeRepository.findByEmployeeCode("EMP-002")
                                .orElseThrow(() -> new RuntimeException("Empleado 'juan' no encontrado."));
                Employee carmen = employeeRepository.findByEmployeeCode("EMP-005")
                                .orElseThrow(() -> new RuntimeException("Empleado 'carmen' no encontrado."));
                Employee supervisor1 = employeeRepository.findByEmployeeCode("SUP-001")
                                .orElseThrow(() -> new RuntimeException("Empleado 'supervisor1' no encontrado."));

                WorkSchedule turnoManana = scheduleRepository.findByName("Turno Cosecha Mañana")
                                .orElseThrow(() -> new RuntimeException(
                                                "Horario 'Turno Cosecha Mañana' no encontrado."));
                RequestType permisoType = requestTypeRepository.findByName("Permiso Personal")
                                .orElseThrow(() -> new RuntimeException(
                                                "Tipo de solicitud 'Permiso Personal' no encontrado."));
                RequestType medicoType = requestTypeRepository.findByName("Solicitud de Permiso Médico").orElseThrow(
                                () -> new RuntimeException(
                                                "Tipo de solicitud 'Solicitud de Permiso Médico' no encontrado."));
                RequestType vacacionesType = requestTypeRepository.findByName("Solicitud de Vacaciones")
                                .orElseThrow(() -> new RuntimeException(
                                                "Tipo de solicitud 'Solicitud de Vacaciones' no encontrado."));

                // ASIGNAR HORARIOS
                assignScheduleToEmployee(maria, turnoManana, "LUN,MAR,MIE,JUE,VIE");
                assignScheduleToEmployee(juan, turnoManana, "LUN,MAR,MIE,JUE,VIE");
                assignScheduleToEmployee(carmen,
                                scheduleRepository.findByName("Turno Administrativo")
                                                .orElseThrow(() -> new RuntimeException(
                                                                "Horario 'Turno Administrativo' no encontrado.")),
                                "LUN,MAR,MIE,JUE,VIE");

                LocalDate today = LocalDate.now();
                LocalDate yesterday = today.minusDays(1);

                // María - Registros IN/OUT (con horas extra)
                createAttendanceRecord(maria, "IN", LocalDateTime.of(today, LocalTime.of(7, 5, 0)));
                createAttendanceRecord(maria, "OUT", LocalDateTime.of(today, LocalTime.of(16, 10, 0)));
                createAttendanceRecord(maria, "IN", LocalDateTime.of(yesterday.minusDays(2), LocalTime.of(6, 58)));
                createAttendanceRecord(maria, "OUT", LocalDateTime.of(yesterday.minusDays(2), LocalTime.of(15, 3)));

                // Juan - Registro solo entrada (para estado ASISTIO en control diario)
                createAttendanceRecord(juan, "IN", LocalDateTime.of(today, LocalTime.of(7, 10, 0)));

                // Carmen - Solicitud de Permiso Pendiente
                createEmployeeRequest(carmen, medicoType, "Cirugía programada", "PENDING", LocalDate.now().plusDays(5),
                                LocalDate.now().plusDays(7), supervisor1);
                createEmployeeRequest(maria, vacacionesType, "Vacaciones anuales", "APPROVED",
                                LocalDate.now().plusMonths(1),
                                LocalDate.now().plusMonths(1).plusDays(15), supervisor1);
                createEmployeeRequest(juan, permisoType, "Asuntos personales", "REJECTED", LocalDate.now().minusDays(3),
                                LocalDate.now().minusDays(3), supervisor1);

                // Boletas de Pago de Prueba
                createPayslip(maria, LocalDate.of(today.getYear(), today.getMonth().minus(1), 1),
                                LocalDate.of(today.getYear(), today.getMonth().minus(1),
                                                LocalDate.of(today.getYear(), today.getMonth().minus(1), 1)
                                                                .lengthOfMonth()),
                                new BigDecimal("1200.00"), new BigDecimal("100.00"), new BigDecimal("50.00"),
                                new BigDecimal("1250.00"),
                                "path/to/maria_payslip_oct.pdf");
                createPayslip(juan, LocalDate.of(today.getYear(), today.getMonth().minus(1), 1),
                                LocalDate.of(today.getYear(), today.getMonth().minus(1),
                                                LocalDate.of(today.getYear(), today.getMonth().minus(1), 1)
                                                                .lengthOfMonth()),
                                new BigDecimal("1200.00"), new BigDecimal("50.00"), new BigDecimal("50.00"),
                                new BigDecimal("1200.00"),
                                "path/to/juan_payslip_oct.pdf");

                System.out.println("-> Datos completos cargados: Horarios, Asistencia, Solicitudes y Boletas de Pago.");
        }

        // =================================================================
        // MÉTODOS AUXILIARES
        // =================================================================

        private void createPosition(String name) {
                if (positionRepository.findByName(name).isEmpty()) {
                        WorkPosition pos = new WorkPosition();
                        pos.setName(name);
                        positionRepository.save(pos);
                }
        }

        private void createRequestType(String name) {
                if (requestTypeRepository.findByName(name).isEmpty()) {
                        RequestType type = new RequestType();
                        type.setName(name);
                        requestTypeRepository.save(type);
                }
        }

        private void createWorkSchedule(String name, LocalTime startTime, LocalTime endTime) {
                if (scheduleRepository.findByName(name).isEmpty()) {
                        WorkSchedule schedule = new WorkSchedule();
                        schedule.setName(name);
                        schedule.setStartTime(startTime);
                        schedule.setEndTime(endTime);
                        scheduleRepository.save(schedule);
                }
        }

        private void assignScheduleToEmployee(Employee employee, WorkSchedule schedule, String workingDays) {
                EmployeeSchedule empSchedule = new EmployeeSchedule();
                empSchedule.setEmployee(employee);
                empSchedule.setWorkSchedule(schedule);
                empSchedule.setValidFrom(LocalDate.now().minusMonths(1));
                empSchedule.setWorkingDays(workingDays);
                employeeScheduleRepository.save(empSchedule);
        }

        private void createAttendanceRecord(Employee employee, String recordType, LocalDateTime timestamp) {
                AttendanceRecord record = new AttendanceRecord();
                record.setEmployee(employee);
                record.setRecordType(recordType);
                record.setDeviceTimestamp(timestamp);
                attendanceRepository.save(record);
        }

        private void createEmployeeRequest(Employee employee, RequestType requestType, String details,
                        String status, LocalDate startDate, LocalDate endDate, Employee manager) {
                EmployeeRequest request = new EmployeeRequest();
                request.setEmployee(employee);
                request.setRequestType(requestType);
                request.setDetails(details);
                request.setRequestedDate(LocalDateTime.now().minusDays(1));
                request.setStartDate(startDate);
                request.setEndDate(endDate);
                request.setStatus(status);
                request.setManager(manager); // Asignar el manager
                employeeRequestRepository.save(request);
        }

        private void createPayslip(Employee employee, LocalDate periodStart, LocalDate periodEnd,
                        BigDecimal grossSalary, BigDecimal bonuses, BigDecimal deductions,
                        BigDecimal netSalary, String filePath) {
                Payslip payslip = new Payslip();
                payslip.setEmployee(employee);
                payslip.setPeriodStartDate(periodStart);
                payslip.setPeriodEndDate(periodEnd);
                payslip.setGrossSalary(grossSalary);
                payslip.setBonuses(bonuses);
                payslip.setDeductions(deductions);
                payslip.setNetSalary(netSalary);
                payslip.setGenerationDate(LocalDate.now());
                payslip.setFilePath(filePath);
                payslipRepository.save(payslip);
        }
}

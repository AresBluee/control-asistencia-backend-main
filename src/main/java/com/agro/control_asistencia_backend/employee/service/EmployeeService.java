package com.agro.control_asistencia_backend.employee.service;

import java.math.BigDecimal;

import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agro.control_asistencia_backend.employee.model.dto.EmployeeRequestDTO;
import com.agro.control_asistencia_backend.employee.model.dto.EmployeeResponseDTO;
import com.agro.control_asistencia_backend.employee.model.dto.EmployeeProfileUpdateDTO;
import com.agro.control_asistencia_backend.employee.model.dto.ManagerContactDTO;
import com.agro.control_asistencia_backend.employee.model.entity.ERole;
import com.agro.control_asistencia_backend.employee.model.entity.Employee;
import com.agro.control_asistencia_backend.employee.model.entity.PasswordResetToken;
import com.agro.control_asistencia_backend.employee.model.entity.Role;
import com.agro.control_asistencia_backend.employee.model.entity.User;
import com.agro.control_asistencia_backend.employee.model.entity.WorkPosition;
import com.agro.control_asistencia_backend.employee.repository.EmployeeRepository;
import com.agro.control_asistencia_backend.employee.repository.PasswordResetTokenRepository;
import com.agro.control_asistencia_backend.employee.repository.UserRepository;
import com.agro.control_asistencia_backend.notification.service.EmailService;
import com.agro.control_asistencia_backend.reporting.model.dto.EmployeeHourSummaryDTO;
import com.agro.control_asistencia_backend.reporting.model.dto.EmployeeProfileDTO;
import com.agro.control_asistencia_backend.reporting.service.ReportingService;
import com.agro.control_asistencia_backend.segurity.model.MessageResponse;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final ReportingService reportingService;
    private PasswordResetTokenRepository tokenRepository;
    private final WorkPositionService workPositionService;
    private static final int EXPIRATION_TIME_MINUTES = 5;

    @Autowired
    public EmployeeService(EmployeeRepository employeeRepository, UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            EmailService emailService, ReportingService reportingService,
            WorkPositionService workPositionService) {
        this.employeeRepository = employeeRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.reportingService = reportingService;
        this.workPositionService = workPositionService;
    }

    // -------------------------------------------------------------------------
    // Mapeo Auxiliar (Entidad a DTO de Listado/Respuesta)
    // -------------------------------------------------------------------------
    private EmployeeResponseDTO mapToResponseDTO(Employee employee) {
        String positionName = employee.getPosition().getName();
        return EmployeeResponseDTO.builder()
                .id(employee.getId())
                .employeeCode(employee.getEmployeeCode())
                .fullName(employee.getFirstName() + " " + employee.getLastName())
                .position(positionName)
                .email(employee.getEmail())
                .phoneNumber(employee.getPhoneNumber())
                .address(employee.getAddress())
                .userId(employee.getUser().getId())
                .username(employee.getUser().getUsername())
                .roleName(employee.getUser().getRole().name())
                .isEnabled(employee.getUser().isEnabled())
                .hireDate(employee.getHireDate())
                .fixedSalary(employee.getFixedSalary())
                .hourlyRate(employee.getHourlyRate())
                .build();
    }

    // -------------------------------------------------------------------------
    // 1. CREACIÓN DE EMPLEADO (Con Notificación por Email)
    // -------------------------------------------------------------------------

    @Transactional
    public EmployeeResponseDTO createEmployee(EmployeeRequestDTO requestDTO) {

        // 1. Validaciones (omitidas por brevedad)
        WorkPosition position = workPositionService.getPositionById(requestDTO.getPositionId());

        // 2. Buscar Rol y crear User
        ERole roleEnum = ERole.valueOf(requestDTO.getRoleName());

        User user = new User();
        user.setUsername(requestDTO.getUsername());
        user.setPassword(passwordEncoder.encode(requestDTO.getPassword()));
        user.setRole(roleEnum);
        user.setEnabled(true);
        user = userRepository.save(user);
        Employee employee = new Employee();
        String generatedCode;
        do {
            generatedCode = "EMP-" + String.format("%04d", (int)(Math.random() * 10000));
        } while (employeeRepository.findByEmployeeCode(generatedCode).isPresent());
        
        employee.setEmployeeCode(generatedCode);
        employee.setFirstName(requestDTO.getFirstName());
        employee.setLastName(requestDTO.getLastName());
        employee.setPosition(position);
        // biometricHash ha sido removido

        // 💡 CRÍTICO: Asignar DNI, Email, Teléfono y Address (Se asume que address no
        // viene en el DTO y es nulo)
        employee.setDni(requestDTO.getDni());
        employee.setHireDate(LocalDate.now());
        employee.setEmail(requestDTO.getEmail());
        employee.setPhoneNumber(requestDTO.getPhoneNumber());
        employee.setAddress("Pendiente de ingresar"); // Simulación inicial de Address

        // Simulación de salario (Asegúrate de que tus DTOs manejen estos campos si
        // vienen del frontend)
        employee.setFixedSalary(new BigDecimal("1200.00"));
        employee.setHourlyRate(new BigDecimal("10.00"));

        employee.setUser(user);
        Employee savedEmployee = employeeRepository.save(employee);

        // 4. LÓGICA DE NOTIFICACIÓN DE CUENTA
        String subject = "Bienvenido a AgroCYT: Tu Cuenta de Portal";
        String body = String.format("Hola %s,\n\nTu cuenta de portal ha sido creada con éxito.\n" +
                "Usuario: %s\nContraseña temporal: %s\n\nPor favor, ingresa al portal.",
                requestDTO.getFirstName(), requestDTO.getUsername(), requestDTO.getPassword());

        emailService.sendEmail(requestDTO.getEmail(), subject, body);

        // 5. Devolver el DTO para el frontend
        return mapToResponseDTO(savedEmployee);
    }

    // -------------------------------------------------------------------------
    // 2. LISTADO DE EMPLEADOS (Para la tabla de Admin)
    // -------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<EmployeeResponseDTO> getAllEmployees() {
        return employeeRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    // -------------------------------------------------------------------------
    // 3. OBTENCIÓN DE PERFIL POR ID DE USUARIO (GET /me)
    // -------------------------------------------------------------------------

    public Employee getEmployeeByUserId(Long userId) {
        return employeeRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Perfil no encontrado."));
    }

    // -------------------------------------------------------------------------
    // 4. ACTUALIZACIÓN DE PERFIL Y COMBINACIÓN DE NÓMINA (PUT /me)
    // -------------------------------------------------------------------------

    @Transactional
    public EmployeeProfileDTO updateProfile(Long userId, EmployeeProfileUpdateDTO updateDTO) {

        Employee existingEmployee = getEmployeeByUserId(userId);

        // 1. Aplicar las actualizaciones a campos editables
        if (updateDTO.getFirstName() != null) {
            existingEmployee.setFirstName(updateDTO.getFirstName());
        }
        if (updateDTO.getLastName() != null) {
            existingEmployee.setLastName(updateDTO.getLastName());
        }

        // 💡 CRÍTICO: Actualizar Email, Teléfono, DNI y Dirección si se editan
        if (updateDTO.getEmail() != null) {
            existingEmployee.setEmail(updateDTO.getEmail());
        }
        if (updateDTO.getPhoneNumber() != null) {
            existingEmployee.setPhoneNumber(updateDTO.getPhoneNumber());
        }
        if (updateDTO.getDni() != null) {
            existingEmployee.setDni(updateDTO.getDni());
        }
        if (updateDTO.getAddress() != null) {
            existingEmployee.setAddress(updateDTO.getAddress());
        }

        Employee updatedEmployee = employeeRepository.save(existingEmployee);

        // 2. Obtener resumen de horas para el DTO de perfil
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.withDayOfMonth(1);
        EmployeeHourSummaryDTO summary = reportingService.getEmployeeHourSummary(updatedEmployee.getId(), startDate,
                endDate);

        // 3. Devolver el DTO final de perfil (Combina Entidad + Nómina)
        return new EmployeeProfileDTO(updatedEmployee, summary);
    }

    // -------------------------------------------------------------------------
    // 5. OBTENCIÓN DE MANAGERS POR ROL (Para Solicitudes)
    // -------------------------------------------------------------------------

    private Date calculateExpiryDate() {
        // 1. Obtener una instancia del Calendario (Calendar)
        Calendar cal = Calendar.getInstance();

        // 2. Establecer el tiempo actual
        cal.setTime(new Date());

        // 3. Añadir los minutos de expiración
        cal.add(Calendar.MINUTE, EXPIRATION_TIME_MINUTES);

        // 4. Devolver la nueva fecha
        return cal.getTime();
    }

    @Transactional(readOnly = true)
    public List<ManagerContactDTO> getManagersByRoles(String... roleNames) {
        // Lógica para obtener managers (se mantiene igual)
        // ...
        return null; // Retorno temporal para evitar errores de compilación
    }

    @Transactional
    public String createPasswordResetToken(String userEmail) {

        // 1. Buscar el Employee por email (asumimos que findByEmail está en
        // EmployeeRepository)
        Employee employee = employeeRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ese email."));

        // 2. Obtener el User asociado al Employee
        User user = employee.getUser();

        // 3. Generar token único (UUID) y calcular expiración
        String token = UUID.randomUUID().toString();
        Date expiryDate = calculateExpiryDate(); // Método auxiliar que calcula la expiración

        // 4. Guardar token en la tabla PasswordResetToken
        PasswordResetToken myToken = new PasswordResetToken();
        myToken.setToken(token);
        myToken.setUser(user);
        myToken.setExpiryDate(expiryDate);
        tokenRepository.save(myToken);

        // 5. Lógica de Envío de Correo
        String resetUrl = "http://localhost:4200/reset-password?token=" + token; // URL del frontend
        String subject = "Recuperación de Contraseña - AgroCYT";
        String body = String.format(
                "Hola %s,\n\nHaz clic en el siguiente enlace para restablecer tu contraseña:\n%s\n\nEste enlace expira en %d minutos.",
                employee.getFirstName(), resetUrl, EXPIRATION_TIME_MINUTES);

        emailService.sendEmail(userEmail, subject, body);

        return token;
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Token inválido o expirado."));

        if (resetToken.getExpiryDate().before(new Date())) {
            tokenRepository.delete(resetToken);
            throw new RuntimeException("El token ha expirado. Solicite uno nuevo.");
        }

        // Cifrar y actualizar la contraseña
        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Eliminar el token usado por seguridad
        tokenRepository.delete(resetToken);
    }

    public User getUserByEmail(String userEmail) {
        // Asumimos que puedes añadir un findByEmail a EmployeeRepository:
        Employee employee = employeeRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ese email."));

        // Devolver el objeto User que está enlazado al Employee
        return employee.getUser();
    }

    // -------------------------------------------------------------------------
    // 6. GESTIÓN DE ESTADO DE USUARIOS (ACTIVAR/DESACTIVAR)
    // -------------------------------------------------------------------------

    @Transactional
    public ResponseEntity<?> activateUser(Long userId) {
        Employee employee = employeeRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado."));

        User user = employee.getUser();
        if (user == null) {
            return ResponseEntity.badRequest().body(new MessageResponse("Usuario no encontrado."));
        }

        user.setEnabled(true);
        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("Usuario activado exitosamente."));
    }

    @Transactional
    public ResponseEntity<?> deactivateUser(Long userId) {
        Employee employee = employeeRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado."));

        User user = employee.getUser();
        if (user == null) {
            return ResponseEntity.badRequest().body(new MessageResponse("Usuario no encontrado."));
        }

        user.setEnabled(false);
        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("Usuario desactivado exitosamente."));
    }

    @Transactional(readOnly = true)
    public boolean isUserActive(Long userId) {
        Employee employee = employeeRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado."));

        User user = employee.getUser();
        return user != null && user.isEnabled();
    }

    @Transactional
public void toggleUserAccountStatus(Long userId, boolean enable) {
    User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + userId));

    // CRÍTICO: No se puede suspender la cuenta del propio administrador
    if (user.getRole() == ERole.ROLE_ADMIN && !enable) {
         throw new RuntimeException("No se puede suspender la cuenta del Administrador principal.");
    }

    user.setEnabled(enable); // Asume que el setter se llama setIsEnabled
    userRepository.save(user);
    
    // Si la cuenta es suspendida, el sistema de seguridad lo bloqueará al intentar loguearse.
}
}

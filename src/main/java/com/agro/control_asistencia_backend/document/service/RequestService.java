package com.agro.control_asistencia_backend.document.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // 

import com.agro.control_asistencia_backend.document.model.dto.RequestCreateDTO;
import com.agro.control_asistencia_backend.document.model.dto.RequestResponseDTO;
import com.agro.control_asistencia_backend.document.model.entity.EmployeeRequest;
import com.agro.control_asistencia_backend.document.model.entity.RequestType;
import com.agro.control_asistencia_backend.document.repository.EmployeeRequestRepository;
import com.agro.control_asistencia_backend.document.repository.RequestTypeRepository;
import com.agro.control_asistencia_backend.employee.model.entity.Employee;
import com.agro.control_asistencia_backend.employee.repository.EmployeeRepository;



@Service
public class RequestService {

    private final EmployeeRequestRepository requestRepository;
    private final RequestTypeRepository requestTypeRepository;
    private final EmployeeRepository employeeRepository;

    // Asumiremos que tu entidad User tiene una relación OneToOne con Employee
    // o que Employee tiene un campo userId. Usaremos findByUserId.

    @Autowired
    public RequestService(EmployeeRequestRepository requestRepository,
            RequestTypeRepository requestTypeRepository,
            EmployeeRepository employeeRepository) {
        this.requestRepository = requestRepository;
        this.requestTypeRepository = requestTypeRepository;
        this.employeeRepository = employeeRepository;
    }

    /**
     * Crea una nueva solicitud de empleado.
     * 
     * @param requestDTO Datos de la solicitud.
     * @param userId     ID del usuario autenticado (extraído del Token JWT).
     * @return La solicitud creada.
     */
    @Transactional
    public RequestResponseDTO createRequest(RequestCreateDTO requestDTO, Long userId) {

      
        Optional<Employee> employeeOpt = employeeRepository.findByUserId(userId);

        if (employeeOpt.isEmpty()) {
            throw new RuntimeException("Error: Empleado no encontrado para el usuario autenticado.");
        }
        Employee employee = employeeOpt.get();

        // 2. Encontrar el Tipo de Solicitud
        Optional<RequestType> typeOpt = requestTypeRepository.findById(requestDTO.getRequestTypeId());

        if (typeOpt.isEmpty()) {
            throw new RuntimeException(
                    "Error: Tipo de solicitud con ID " + requestDTO.getRequestTypeId() + " no encontrado.");
        }
        RequestType requestType = typeOpt.get();

        // 3. Mapear y Guardar la Solicitud
        EmployeeRequest newRequest = new EmployeeRequest();
        newRequest.setEmployee(employee);
        newRequest.setRequestType(requestType);
        newRequest.setDetails(requestDTO.getDetails());
        newRequest.setStartDate(requestDTO.getStartDate());
        newRequest.setEndDate(requestDTO.getEndDate());

        EmployeeRequest savedRequest = requestRepository.save(newRequest); // Guardar

        return mapToRequestResponseDTO(savedRequest);
    }

    @Transactional
    public RequestResponseDTO updateRequestStatus(Long requestId, String status, String comment) {

        EmployeeRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada con ID: " + requestId));

        // Validar el estado entrante
        if (!status.equals("APPROVED") && !status.equals("REJECTED")) {
            throw new RuntimeException("Estado inválido. Use 'APPROVED' o 'REJECTED'.");
        }

        request.setStatus(status);
        request.setManagerComment(comment);

        EmployeeRequest savedRequest = requestRepository.save(request);

        // NOTA: Aquí iría la lógica para notificar al empleado sobre el cambio de
        // estado.

        // Mapear la entidad guardada al DTO de Respuesta
        return mapToRequestResponseDTO(savedRequest);
    }

    // Método auxiliar (debe ser implementado en tu servicio)
    private RequestResponseDTO mapToRequestResponseDTO(EmployeeRequest request) {
        // ... Lógica de mapeo a RequestResponseDTO (igual que en createRequest) ...
        Employee employee = request.getEmployee();
        return RequestResponseDTO.builder()
        .id(request.getId())
        .employeeId(employee.getId())
        .employeeName(employee.getFirstName() + " " + employee.getLastName())
        .requestType(request.getRequestType().getName())
        .details(request.getDetails())
        .requestedDate(request.getRequestedDate().toLocalDate()) 
        .startDate(request.getStartDate())
        .endDate(request.getEndDate())
        .status(request.getStatus())
        .managerComment(request.getManagerComment())
        .build();
    }

    
    /**
     * Obtiene todas las solicitudes del usuario autenticado (empleado).
     * @param userId ID del usuario autenticado (proviene del token JWT).
     * @return Lista de solicitudes mapeadas a DTOs.
     */
    public List<RequestResponseDTO> getMyRequests(Long userId) {
        
        // 1. Buscar el empleado a partir del userId (CRÍTICO para la seguridad)
        Employee employee = employeeRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado para el usuario autenticado."));
        
        // 2. Obtener las solicitudes de ese empleado
        List<EmployeeRequest> requests = requestRepository.findByEmployee(employee);
        
        // 3. Mapear la lista de entidades a DTOs de respuesta limpios
        return requests.stream()
                .map(this::mapToRequestResponseDTO) // Usamos el método de mapeo auxiliar
                .collect(Collectors.toList());
    }
    
    /**
     * Obtiene todas las solicitudes de la base de datos (para vista de Admin/RRHH).
     * Nota: Este método debe ser llamado por el RequestController GET /api/requests
     * @return Lista de todas las solicitudes mapeadas a DTOs.
     */
    @Transactional(readOnly = true) // Indicamos que esta operación es solo de lectura
    public List<RequestResponseDTO> getAllRequests() {
        
        // 1. Obtener todas las entidades de solicitud
        List<EmployeeRequest> requests = requestRepository.findAll();
        
        // 2. Mapear la lista de entidades a una lista de DTOs de respuesta limpios
        return requests.stream()
                .map(this::mapToRequestResponseDTO) // Usamos el método de mapeo auxiliar
                .collect(Collectors.toList());
    }
    
   

}

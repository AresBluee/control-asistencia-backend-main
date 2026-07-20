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
import com.agro.control_asistencia_backend.document.repository.DocumentRepository;
import com.agro.control_asistencia_backend.employee.model.entity.Employee;
import com.agro.control_asistencia_backend.employee.repository.EmployeeRepository;



@Service
public class RequestService {

    private final EmployeeRequestRepository requestRepository;
    private final RequestTypeRepository requestTypeRepository;
    private final EmployeeRepository employeeRepository;
    private final DocumentRepository documentRepository;
    private final DocumentService documentService;

    // Asumiremos que tu entidad User tiene una relación OneToOne con Employee
    // o que Employee tiene un campo userId. Usaremos findByUserId.

    @Autowired
    public RequestService(EmployeeRequestRepository requestRepository,
            RequestTypeRepository requestTypeRepository,
            EmployeeRepository employeeRepository,
            DocumentRepository documentRepository,
            DocumentService documentService) {
        this.requestRepository = requestRepository;
        this.requestTypeRepository = requestTypeRepository;
        this.employeeRepository = employeeRepository;
        this.documentRepository = documentRepository;
        this.documentService = documentService;
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

        if (requestDTO.getDocumentId() != null) {
            com.agro.control_asistencia_backend.document.model.entity.Document doc = documentRepository.findById(requestDTO.getDocumentId())
                    .orElseThrow(() -> new RuntimeException("Documento no encontrado con ID: " + requestDTO.getDocumentId()));
            newRequest.setDocumentPath(doc.getStoragePath());
        }

        EmployeeRequest savedRequest = requestRepository.save(newRequest); // Guardar

        return mapToRequestResponseDTO(savedRequest);
    }

    @Transactional
    public RequestResponseDTO updateRequestStatus(Long requestId, String status, String comment, Long managerId) {

        EmployeeRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada con ID: " + requestId));

        // Validar el estado entrante
        if (!status.equals("APPROVED") && !status.equals("REJECTED") && !status.equals("AWAITING_SIGNATURE")) {
            throw new RuntimeException("Estado inválido. Use 'APPROVED', 'REJECTED' o 'AWAITING_SIGNATURE'.");
        }

        if (managerId != null) {
            Employee manager = employeeRepository.findById(managerId)
                    .orElseThrow(() -> new RuntimeException("Manager no encontrado con ID: " + managerId));
            request.setManager(manager);
        }

        request.setStatus(status);
        request.setManagerComment(comment);

        EmployeeRequest savedRequest = requestRepository.save(request);

        // NOTA: Aquí iría la lógica para notificar al empleado sobre el cambio de
        // estado.

        // Mapear la entidad guardada al DTO de Respuesta
        return mapToRequestResponseDTO(savedRequest);
    }

    @Transactional
    public RequestResponseDTO signAndUploadRequest(Long requestId, org.springframework.web.multipart.MultipartFile file, com.agro.control_asistencia_backend.segurity.service.UserDetailsImpl user) throws java.io.IOException {
        EmployeeRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada con ID: " + requestId));

        // Validar que el usuario sea el manager de la solicitud o Admin
        Employee manager = request.getManager();
        if (manager == null || !manager.getUser().getId().equals(user.getId())) {
            if (user.getAuthorities().stream().noneMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                throw new RuntimeException("No tienes permiso para firmar esta solicitud.");
            }
        }

        // Subir el documento usando DocumentService
        com.agro.control_asistencia_backend.document.model.dto.DocumentResponseDTO docResponse = documentService.uploadDocument(
                request.getEmployee().getId(), 
                request.getRequestType().getName() + "_FIRMADO", 
                file, 
                user);

        // Actualizar la solicitud
        request.setIsSigned(true);
        request.setSignedAt(java.time.LocalDateTime.now());
        request.setStatus("COMPLETED");
        request.setDocumentPath(docResponse.getDownloadUrl());

        EmployeeRequest savedRequest = requestRepository.save(request);

        return mapToRequestResponseDTO(savedRequest);
    }

    // Método auxiliar (debe ser implementado en tu servicio)
    private RequestResponseDTO mapToRequestResponseDTO(EmployeeRequest request) {
        Employee employee = request.getEmployee();
        
        // Calcular prioridad dinámica y SLA
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        java.time.LocalDateTime reqDate = request.getRequestedDate() != null ? request.getRequestedDate() : (request.getStartDate() != null ? request.getStartDate().atStartOfDay() : now);
        java.time.LocalDateTime dueDate = reqDate.plusDays(request.getRequestType().getSlaDays() != null ? request.getRequestType().getSlaDays() : 7);
        long remainingSlaHours = java.time.Duration.between(now, dueDate).toHours();
        
        int calculatedPriority = request.getRequestType().getBasePriority() != null ? request.getRequestType().getBasePriority() : 1;
        if (!"COMPLETED".equals(request.getStatus()) && !"REJECTED".equals(request.getStatus())) {
            if (remainingSlaHours <= 24) {
                calculatedPriority = 4; // Escalar a Crítica (4)
            }
        }

        String managerName = request.getManager() != null ? 
                request.getManager().getFirstName() + " " + request.getManager().getLastName() : "No asignado";

        String docPath = request.getDocumentPath();
        if (docPath != null && (docPath.startsWith("uploads") || docPath.contains("\\") || docPath.contains("/uploads"))) {
            Optional<com.agro.control_asistencia_backend.document.model.entity.Document> docOpt = documentRepository.findByStoragePath(docPath);
            if (docOpt.isPresent()) {
                docPath = "/api/documents/" + docOpt.get().getId() + "/download";
            }
        }

        return RequestResponseDTO.builder()
                .managerName(managerName)
                .id(request.getId())
        .employeeId(employee.getId())
        .employeeName(employee.getFirstName() + " " + employee.getLastName())
        .requestType(request.getRequestType().getName())
        .details(request.getDetails())
        .requestedDate(reqDate.toLocalDate()) 
        .startDate(request.getStartDate())
        .endDate(request.getEndDate())
        .status(request.getStatus())
        .managerComment(request.getManagerComment())
        .calculatedPriority(calculatedPriority)
        .remainingSlaHours(remainingSlaHours)
        .isSigned(request.getIsSigned())
        .documentPath(docPath)
        .requiresSignature(request.getRequestType().getRequiresSignature())
        .build();
    }

    /**
     * Obtiene la cola de prioridad para RRHH, ordenada por urgencia.
     */
    @Transactional(readOnly = true)
    public List<RequestResponseDTO> getPriorityQueue() {
        List<EmployeeRequest> requests = requestRepository.findAll();
        
        return requests.stream()
                .filter(r -> !"COMPLETED".equals(r.getStatus()) && !"REJECTED".equals(r.getStatus()))
                .map(this::mapToRequestResponseDTO)
                .sorted((r1, r2) -> {
                    // Primero ordenamos por prioridad calculada (descendente)
                    int priorityCompare = r2.getCalculatedPriority().compareTo(r1.getCalculatedPriority());
                    if (priorityCompare != 0) return priorityCompare;
                    
                    // Si tienen la misma prioridad, ordenamos por tiempo restante SLA (ascendente)
                    return r1.getRemainingSlaHours().compareTo(r2.getRemainingSlaHours());
                })
                .collect(Collectors.toList());
    }

    
    /**
     * Obtiene todas las solicitudes del usuario autenticado (empleado).
     * @param userId ID del usuario autenticado (proviene del token JWT).
     * @return Lista de solicitudes mapeadas a DTOs.
     */
    @Transactional(readOnly = true)
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

package com.agro.control_asistencia_backend.document.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.agro.control_asistencia_backend.document.model.dto.RequestTypeDTO;
import com.agro.control_asistencia_backend.document.model.entity.RequestType;
import com.agro.control_asistencia_backend.document.repository.RequestTypeRepository;

@Service
public class RequestTypeService {

    private final RequestTypeRepository requestTypeRepository;

    @Autowired
    public RequestTypeService(RequestTypeRepository requestTypeRepository) {
        this.requestTypeRepository = requestTypeRepository;
    }

    public List<RequestTypeDTO> getAllRequestTypes() {
        return requestTypeRepository.findAll().stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public RequestTypeDTO createRequestType(RequestTypeDTO dto) {
        if (requestTypeRepository.findByName(dto.getName()).isPresent()) {
            throw new RuntimeException("Ya existe un tipo de solicitud con ese nombre.");
        }
        RequestType type = new RequestType();
        type.setName(dto.getName());
        type.setBasePriority(dto.getBasePriority());
        type.setSlaDays(dto.getSlaDays());
        type.setRequiresSignature(dto.getRequiresSignature());
        type.setRequiresAttachment(dto.getRequiresAttachment());

        RequestType saved = requestTypeRepository.save(type);
        return mapToDTO(saved);
    }

    public RequestTypeDTO updateRequestType(Long id, RequestTypeDTO dto) {
        RequestType type = requestTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tipo de solicitud no encontrado."));

        type.setName(dto.getName());
        type.setBasePriority(dto.getBasePriority());
        type.setSlaDays(dto.getSlaDays());
        type.setRequiresSignature(dto.getRequiresSignature());
        type.setRequiresAttachment(dto.getRequiresAttachment());

        RequestType saved = requestTypeRepository.save(type);
        return mapToDTO(saved);
    }

    public void deleteRequestType(Long id) {
        RequestType type = requestTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tipo de solicitud no encontrado."));
        requestTypeRepository.delete(type);
    }

    private RequestTypeDTO mapToDTO(RequestType entity) {
        RequestTypeDTO dto = new RequestTypeDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setBasePriority(entity.getBasePriority());
        dto.setSlaDays(entity.getSlaDays());
        dto.setRequiresSignature(entity.getRequiresSignature());
        dto.setRequiresAttachment(entity.getRequiresAttachment());
        return dto;
    }
}

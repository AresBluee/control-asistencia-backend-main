package com.agro.control_asistencia_backend.document.service;

import com.agro.control_asistencia_backend.document.model.entity.DocumentType;
import com.agro.control_asistencia_backend.document.repository.DocumentTypeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class DocumentTypeService {

    private final DocumentTypeRepository documentTypeRepository;

    public DocumentTypeService(DocumentTypeRepository documentTypeRepository) {
        this.documentTypeRepository = documentTypeRepository;
    }

    @Transactional(readOnly = true)
    public List<DocumentType> getAll() {
        return documentTypeRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<DocumentType> getById(Long id) {
        return documentTypeRepository.findById(id);
    }

    @Transactional
    public DocumentType create(DocumentType documentType) {
        // Simple validation
        if (documentType.getBasePriority() < 1 || documentType.getBasePriority() > 4) {
            throw new IllegalArgumentException("Base priority must be between 1 and 4");
        }
        if (documentType.getSlaDays() <= 0) {
            throw new IllegalArgumentException("SLA days must be greater than 0");
        }
        // Ensure unique name
        if (documentTypeRepository.findByName(documentType.getName()).isPresent()) {
            throw new IllegalArgumentException("Document type with this name already exists");
        }
        return documentTypeRepository.save(documentType);
    }

    @Transactional
    public DocumentType update(Long id, DocumentType updated) {
        DocumentType existing = documentTypeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Document type not found"));
        existing.setName(updated.getName());
        existing.setBasePriority(updated.getBasePriority());
        existing.setSlaDays(updated.getSlaDays());
        existing.setRequiresRrhhApproval(updated.isRequiresRrhhApproval());
        existing.setRequiresAdminSignature(updated.isRequiresAdminSignature());
        existing.setDescription(updated.getDescription());
        existing.setTemplatePath(updated.getTemplatePath());
        return documentTypeRepository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        if (!documentTypeRepository.existsById(id)) {
            throw new IllegalArgumentException("Document type not found");
        }
        documentTypeRepository.deleteById(id);
    }
}

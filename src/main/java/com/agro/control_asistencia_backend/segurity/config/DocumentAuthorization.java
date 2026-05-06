package com.agro.control_asistencia_backend.segurity.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.agro.control_asistencia_backend.document.model.entity.Document;
import com.agro.control_asistencia_backend.document.repository.DocumentRepository;
import com.agro.control_asistencia_backend.employee.model.entity.Employee;
import com.agro.control_asistencia_backend.employee.repository.EmployeeRepository;
import com.agro.control_asistencia_backend.employee.repository.UserRepository;

@Component("documentAuthorization") 
public class DocumentAuthorization {


    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    
    @Autowired
    public DocumentAuthorization(DocumentRepository documentRepository, UserRepository userRepository, EmployeeRepository employeeRepository) {
        this.documentRepository = documentRepository;
        this.userRepository = userRepository;
        this.employeeRepository = employeeRepository;
    }

    public boolean canView(Long documentId, String username) {
        
        Document doc = documentRepository.findById(documentId).orElse(null);
        if (doc == null) return false;
        
        return userRepository.findByUsername(username)
                .map(user -> {
                    Employee employee = employeeRepository.findByUserId(user.getId()).orElse(null);
                    return employee != null && doc.getEmployee() != null && doc.getEmployee().getId().equals(employee.getId());
                })
                .orElse(false);
    }
}

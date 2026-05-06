package com.agro.control_asistencia_backend.employee.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agro.control_asistencia_backend.employee.model.entity.WorkPosition;
import com.agro.control_asistencia_backend.employee.repository.WorkPositionRepository;

@Service
@Transactional
public class WorkPositionService {

    private final WorkPositionRepository workPositionRepository;

    @Autowired
    public WorkPositionService(WorkPositionRepository workPositionRepository) {
        this.workPositionRepository = workPositionRepository;
    }


    public List<WorkPosition> getAllPositions() {
        return workPositionRepository.findAll();
    }



    @Transactional
    public WorkPosition createPosition(String name) {
        if (workPositionRepository.findByName(name).isPresent()) {
            throw new RuntimeException("El cargo '" + name + "' ya existe.");
        }

        WorkPosition position = new WorkPosition();
        position.setName(name);
        return workPositionRepository.save(position);
    }
    
    /**
     * Obtiene un cargo por su ID (Necesario para el EmployeeService).
     */
    public WorkPosition getPositionById(Long id) {
        return workPositionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cargo no encontrado con ID: " + id));
    }
}

package com.agro.control_asistencia_backend.scheduling.repository;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.agro.control_asistencia_backend.employee.model.entity.Employee;
import com.agro.control_asistencia_backend.scheduling.model.entity.EmployeeSchedule;

public interface EmployeeScheduleRepository extends JpaRepository<EmployeeSchedule, Long> {

     Optional<EmployeeSchedule> findByEmployeeAndValidFromBeforeAndValidToAfter(
        Employee employee, LocalDate todayStart, LocalDate todayEnd
    );

    Optional<EmployeeSchedule> findTopByEmployeeOrderByValidFromDesc(Employee employee); // <--- ¡AÑADE ESTO!

}

package com.agro.control_asistencia_backend.attendance.model.entity;

import java.time.LocalDateTime;

import com.agro.control_asistencia_backend.employee.model.entity.Employee;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "attendance_records")
@Data
@NoArgsConstructor

public class AttendanceRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(nullable = false, length = 10)
    private String recordType;

    @Column(nullable = false)
    private LocalDateTime deviceTimestamp;

    @Column(nullable = false)
    private LocalDateTime syncTimestamp = LocalDateTime.now();

    private Double latitude;
    private Double longitude;

    private Boolean isOfflineRecord = false;

}

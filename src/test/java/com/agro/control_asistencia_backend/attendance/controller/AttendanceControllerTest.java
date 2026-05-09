package com.agro.control_asistencia_backend.attendance.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import com.agro.control_asistencia_backend.attendance.model.dto.AttendanceRequestDTO;
import com.agro.control_asistencia_backend.attendance.model.dto.AttendanceResponseDTO;
import com.agro.control_asistencia_backend.attendance.model.entity.AttendanceHistoryDTO;
import com.agro.control_asistencia_backend.attendance.model.entity.AttendanceRecord;
import com.agro.control_asistencia_backend.attendance.repository.AttendanceRepository;
import com.agro.control_asistencia_backend.attendance.service.AttendanceAdminService;
import com.agro.control_asistencia_backend.attendance.service.AttendanceService;
import com.agro.control_asistencia_backend.document.model.dto.EmployeeStatusDTO;
import com.agro.control_asistencia_backend.employee.repository.EmployeeRepository;
import com.agro.control_asistencia_backend.segurity.service.UserDetailsImpl;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(AttendanceController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AttendanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AttendanceService attendanceService;

    @MockBean
    private AttendanceAdminService attendanceAdminService;

    @MockBean
    private EmployeeRepository employeeRepository;

    @MockBean
    private AttendanceRepository attendanceRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testRegisterAttendance() throws Exception {
        AttendanceRequestDTO requestDTO = new AttendanceRequestDTO();
        requestDTO.setEmployeeCode("EMP001");
        requestDTO.setDeviceTimestamp(LocalDateTime.now());
        requestDTO.setLatitude(10.0);
        requestDTO.setLongitude(20.0);

        AttendanceResponseDTO responseDTO = AttendanceResponseDTO.builder()
                .employeeCode("EMP001")
                .build();

        when(attendanceService.processAttendanceRecord(any(AttendanceRequestDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/api/attendance/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.employeeCode").value("EMP001"));
    }

    @Test
    public void testGetDailyStatus() throws Exception {
        EmployeeStatusDTO statusDTO = new EmployeeStatusDTO();
        statusDTO.setEmployeeCode("EMP001");
        statusDTO.setStatus("PRESENT");

        List<EmployeeStatusDTO> statusList = Collections.singletonList(statusDTO);

        when(attendanceAdminService.getDailyAttendanceStatus(any(LocalDate.class))).thenReturn(statusList);

        mockMvc.perform(get("/api/attendance/status/daily")
                .param("date", LocalDate.now().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].employeeCode").value("EMP001"))
                .andExpect(jsonPath("$[0].status").value("PRESENT"));
    }

    @Test
    public void testGetUserAttendance() throws Exception {
        Authentication authentication = new UsernamePasswordAuthenticationToken("EMP001", null);

        AttendanceRecord record = new AttendanceRecord();
        // Set fields if necessary

        List<AttendanceRecord> records = Collections.singletonList(record);

        when(attendanceService.getAttendanceByEmployeeCode(anyString())).thenReturn(records);

        mockMvc.perform(get("/api/attendance/user")
                .principal(authentication))
                .andExpect(status().isOk());
    }

    @Test
    public void testGetMyAttendanceHistory() throws Exception {
        UserDetailsImpl userDetails = new UserDetailsImpl(1L, "testuser", "password", 
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")), true);
        
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        AttendanceHistoryDTO historyDTO = AttendanceHistoryDTO.builder().build();
        // Set fields if necessary

        List<AttendanceHistoryDTO> history = Collections.singletonList(historyDTO);

        when(attendanceService.getAttendanceByUserId(anyLong())).thenReturn(history);

        mockMvc.perform(get("/api/attendance/me")
                .principal(authentication))
                .andExpect(status().isOk());
    }
}

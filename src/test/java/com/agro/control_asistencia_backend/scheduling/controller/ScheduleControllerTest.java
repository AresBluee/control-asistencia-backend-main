package com.agro.control_asistencia_backend.scheduling.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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

import com.agro.control_asistencia_backend.scheduling.model.dto.EmployeeScheduleAssignmentDTO;
import com.agro.control_asistencia_backend.scheduling.model.dto.ScheduleResponseDTO;
import com.agro.control_asistencia_backend.scheduling.model.dto.WorkScheduleDTO;
import com.agro.control_asistencia_backend.scheduling.model.entity.WorkSchedule;
import com.agro.control_asistencia_backend.scheduling.service.ScheduleService;
import com.agro.control_asistencia_backend.segurity.service.UserDetailsImpl;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(ScheduleController.class)
@AutoConfigureMockMvc(addFilters = false)
public class ScheduleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ScheduleService scheduleService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testCreateWorkSchedule() throws Exception {
        WorkScheduleDTO dto = new WorkScheduleDTO();
        dto.setName("Turno Mañana");
        dto.setStartTime(LocalTime.of(8, 0));
        dto.setEndTime(LocalTime.of(16, 0));
        dto.setToleranceMinutes(15);

        WorkSchedule schedule = new WorkSchedule();
        schedule.setId(1L);
        schedule.setName("Turno Mañana");

        when(scheduleService.createWorkSchedule(any(WorkScheduleDTO.class))).thenReturn(schedule);

        mockMvc.perform(post("/api/schedules/turn")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Turno Mañana"));
    }

    @Test
    public void testAssignSchedule() throws Exception {
        EmployeeScheduleAssignmentDTO dto = new EmployeeScheduleAssignmentDTO();
        // Set fields if necessary

        ScheduleResponseDTO responseDTO = ScheduleResponseDTO.builder().build();
        // Set fields if necessary

        when(scheduleService.assignScheduleToEmployee(any(EmployeeScheduleAssignmentDTO.class)))
                .thenReturn(responseDTO);

        mockMvc.perform(post("/api/schedules/assign")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
    }

    @Test
    public void testGetAllWorkSchedules() throws Exception {
        WorkSchedule schedule = new WorkSchedule();
        schedule.setId(1L);
        schedule.setName("Turno Mañana");

        List<WorkSchedule> schedules = Collections.singletonList(schedule);

        when(scheduleService.getAllWorkSchedules()).thenReturn(schedules);

        mockMvc.perform(get("/api/schedules"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Turno Mañana"));
    }

    @Test
    public void testGetMySchedule() throws Exception {
        UserDetailsImpl userDetails = new UserDetailsImpl(1L, "testuser", "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")), true);

        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null,
                userDetails.getAuthorities());

        ScheduleResponseDTO responseDTO = ScheduleResponseDTO.builder().build();
        // Set fields if necessary

        when(scheduleService.getEmployeeScheduleByUserId(anyLong(), any(LocalDate.class)))
                .thenReturn(Optional.of(responseDTO));

        mockMvc.perform(get("/api/schedules/me")
                .param("date", LocalDate.now().toString())
                .principal(authentication))
                .andExpect(status().isOk());
    }
}

package com.agro.control_asistencia_backend.reporting.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import com.agro.control_asistencia_backend.employee.service.EmployeeService;
import com.agro.control_asistencia_backend.reporting.model.dto.DailyWorkSummary;
import com.agro.control_asistencia_backend.reporting.service.ReportingService;
import com.agro.control_asistencia_backend.segurity.service.UserDetailsImpl;

@WebMvcTest(ReportingController.class)
@AutoConfigureMockMvc(addFilters = false)
public class ReportingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReportingService reportingService;

    @MockBean
    private EmployeeService employeeService;

    @Test
    public void testGetEmployeeReport() throws Exception {
        DailyWorkSummary summary = DailyWorkSummary.builder()
                .build();

        List<DailyWorkSummary> summaries = Collections.singletonList(summary);

        when(reportingService.getEmployeeWorkSummary(anyLong(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(summaries);

        mockMvc.perform(get("/api/reports/employee/1")
                .param("start", LocalDate.now().toString())
                .param("end", LocalDate.now().toString()))
                .andExpect(status().isOk());
    }

    @Test
    public void testGetMyReport() throws Exception {
        UserDetailsImpl userDetails = new UserDetailsImpl(1L, "testuser", "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")), true);

        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null,
                userDetails.getAuthorities());

        DailyWorkSummary summary = DailyWorkSummary.builder()
                .build();

        List<DailyWorkSummary> summaries = Collections.singletonList(summary);

        when(reportingService.getEmployeeWorkSummary(anyLong(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(summaries);

        mockMvc.perform(get("/api/reports/me")
                .param("start", LocalDate.now().toString())
                .param("end", LocalDate.now().toString())
                .principal(authentication))
                .andExpect(status().isOk());
    }

    @Test
    public void testDownloadGlobalAttendanceReport() throws Exception {
        Resource resource = new ByteArrayResource("test data".getBytes());

        when(reportingService.generateGlobalAttendanceFile(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(resource);

        mockMvc.perform(get("/api/reports/global/attendance")
                .param("start", LocalDate.now().toString())
                .param("end", LocalDate.now().toString()))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition",
                        "attachment; filename=\"reporte_asistencia_global_" + LocalDate.now() + "_a_" + LocalDate.now()
                                + ".csv\""))
                .andExpect(header().string("Content-Type", "text/csv"));
    }
}

package com.agro.control_asistencia_backend.document.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import com.agro.control_asistencia_backend.document.model.dto.PayslipResponseDTO;
import com.agro.control_asistencia_backend.document.service.PayslipService;
import com.agro.control_asistencia_backend.segurity.service.UserDetailsImpl;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(PayslipController.class)
@AutoConfigureMockMvc(addFilters = false)
public class PayslipControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PayslipService payslipService;

    @MockBean
    private com.agro.control_asistencia_backend.segurity.jwt.JwtUtils jwtUtils;

    @MockBean
    private com.agro.control_asistencia_backend.segurity.service.UserDetailsServiceImpl userDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testGeneratePayslip() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("employeeId", 1L);
        request.put("startDate", LocalDate.now().toString());
        request.put("endDate", LocalDate.now().toString());

        PayslipResponseDTO responseDTO = PayslipResponseDTO.builder()
                .id(1L)
                .employeeName("John Doe")
                .build();

        when(payslipService.generatePayslip(anyLong(), any(LocalDate.class), any(LocalDate.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/api/payslips/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.employeeName").value("John Doe"));
    }

    @Test
    public void testGetAllPayslips() throws Exception {
        PayslipResponseDTO responseDTO = PayslipResponseDTO.builder()
                .id(1L)
                .employeeName("John Doe")
                .build();

        List<PayslipResponseDTO> payslips = Collections.singletonList(responseDTO);

        when(payslipService.getAllPayslips()).thenReturn(payslips);

        mockMvc.perform(get("/api/payslips/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    public void testGetMyPayslips() throws Exception {
        UserDetailsImpl userDetails = new UserDetailsImpl(1L, "testuser", "password", 
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_EMPLOYEE")), true);
        
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        PayslipResponseDTO responseDTO = PayslipResponseDTO.builder()
                .id(1L)
                .employeeName("John Doe")
                .build();

        List<PayslipResponseDTO> payslips = Collections.singletonList(responseDTO);

        when(payslipService.getPayslipsByUserId(anyLong())).thenReturn(payslips);

        mockMvc.perform(get("/api/payslips/me")
                .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));
    }
}

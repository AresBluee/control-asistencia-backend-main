package com.agro.control_asistencia_backend.document.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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

import com.agro.control_asistencia_backend.document.model.dto.RequestCreateDTO;
import com.agro.control_asistencia_backend.document.model.dto.RequestResponseDTO;
import com.agro.control_asistencia_backend.document.repository.EmployeeRequestRepository;
import com.agro.control_asistencia_backend.document.service.RequestService;
import com.agro.control_asistencia_backend.segurity.service.UserDetailsImpl;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(RequestController.class)
@AutoConfigureMockMvc(addFilters = false)
public class RequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RequestService requestService;

    @MockBean
    private EmployeeRequestRepository requestRepository;

    @MockBean
    private com.agro.control_asistencia_backend.segurity.jwt.JwtUtils jwtUtils;

    @MockBean
    private com.agro.control_asistencia_backend.segurity.service.UserDetailsServiceImpl userDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testCreateEmployeeRequest() throws Exception {
        RequestCreateDTO requestDTO = new RequestCreateDTO();
        requestDTO.setRequestTypeId(1L);
        requestDTO.setDetails("Solicitud de vacaciones");
        requestDTO.setStartDate(LocalDate.now().plusDays(1));

        RequestResponseDTO responseDTO = RequestResponseDTO.builder()
                .id(1L)
                .details("Solicitud de vacaciones")
                .build();

        UserDetailsImpl userDetails = new UserDetailsImpl(1L, "testuser", "password", 
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_EMPLOYEE")), true);
        
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        when(requestService.createRequest(any(RequestCreateDTO.class), anyLong())).thenReturn(responseDTO);

        mockMvc.perform(post("/api/requests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO))
                .principal(authentication))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.details").value("Solicitud de vacaciones"));
    }

    @Test
    public void testGetAllRequests() throws Exception {
        RequestResponseDTO responseDTO = RequestResponseDTO.builder()
                .id(1L)
                .details("Solicitud de vacaciones")
                .build();

        List<RequestResponseDTO> requests = Collections.singletonList(responseDTO);

        when(requestService.getAllRequests()).thenReturn(requests);

        mockMvc.perform(get("/api/requests"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    public void testGetMyRequests() throws Exception {
        UserDetailsImpl userDetails = new UserDetailsImpl(1L, "testuser", "password", 
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_EMPLOYEE")), true);
        
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        RequestResponseDTO responseDTO = RequestResponseDTO.builder()
                .id(1L)
                .details("Solicitud de vacaciones")
                .build();

        List<RequestResponseDTO> requests = Collections.singletonList(responseDTO);

        when(requestService.getMyRequests(anyLong())).thenReturn(requests);

        mockMvc.perform(get("/api/requests/me")
                .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    public void testUpdateRequestStatus() throws Exception {
        Map<String, String> statusUpdate = new HashMap<>();
        statusUpdate.put("status", "APPROVED");
        statusUpdate.put("comment", "Aprobado por el jefe");

        RequestResponseDTO responseDTO = RequestResponseDTO.builder()
                .id(1L)
                .details("Solicitud de vacaciones")
                .build();

        when(requestService.updateRequestStatus(anyLong(), anyString(), anyString(), any())).thenReturn(responseDTO);

        mockMvc.perform(put("/api/requests/1/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(statusUpdate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }
}

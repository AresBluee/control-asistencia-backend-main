package com.agro.control_asistencia_backend.employee.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;

import com.agro.control_asistencia_backend.employee.model.dto.EmployeeRequestDTO;
import com.agro.control_asistencia_backend.employee.model.dto.EmployeeResponseDTO;
import com.agro.control_asistencia_backend.employee.model.entity.Employee;
import com.agro.control_asistencia_backend.employee.model.entity.User;
import com.agro.control_asistencia_backend.employee.model.entity.Role;
import com.agro.control_asistencia_backend.employee.model.entity.ERole;
import com.agro.control_asistencia_backend.employee.service.EmployeeService;
import com.agro.control_asistencia_backend.employee.service.WorkPositionService;
import com.agro.control_asistencia_backend.reporting.service.ReportingService;
import com.agro.control_asistencia_backend.segurity.service.UserDetailsImpl;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(EmployeeController.class)
@AutoConfigureMockMvc(addFilters = false)
public class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmployeeService employeeService;

    @MockBean
    private ReportingService reportingService;

    @MockBean
    private WorkPositionService workPositionService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testCreateEmployee() throws Exception {
        EmployeeRequestDTO requestDTO = new EmployeeRequestDTO();
        requestDTO.setDni("12345678");
        requestDTO.setEmail("test@test.com");
        requestDTO.setFirstName("John");
        requestDTO.setLastName("Doe");
        requestDTO.setPositionId(1L);
        requestDTO.setUsername("johndoe");
        requestDTO.setPassword("password");
        requestDTO.setRoleName("ROLE_USER");

        EmployeeResponseDTO responseDTO = new EmployeeResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setFullName("John Doe");

        when(employeeService.createEmployee(any(EmployeeRequestDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/api/employee")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.fullName").value("John Doe"));
    }

    @Test
    public void testGetAllEmployees() throws Exception {
        EmployeeResponseDTO responseDTO = new EmployeeResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setFullName("John Doe");

        List<EmployeeResponseDTO> employees = Collections.singletonList(responseDTO);

        when(employeeService.getAllEmployees()).thenReturn(employees);

        mockMvc.perform(get("/api/employee"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].fullName").value("John Doe"));
    }

    @Test
    public void testGetMyProfile() throws Exception {
        UserDetailsImpl userDetails = new UserDetailsImpl(1L, "testuser", "password", 
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")), true);
        
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        Employee employee = new Employee();
        employee.setId(1L);
        employee.setFirstName("John");
        employee.setLastName("Doe");
        
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setRole(ERole.ROLE_USER);
        employee.setUser(user);

        when(employeeService.getEmployeeByUserId(anyLong())).thenReturn(employee);
        // Mocking reportingService.getEmployeeHourSummary is omitted for simplicity or can be mocked if needed.
        // Assuming EmployeeProfileDTO can be constructed with null summary or mock it if necessary.

        mockMvc.perform(get("/api/employee/me")
                .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employee.id").value(1L));
    }
}

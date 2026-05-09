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

import com.agro.control_asistencia_backend.document.model.dto.CertificateCreationDTO;
import com.agro.control_asistencia_backend.document.model.dto.DocumentResponseDTO;
import com.agro.control_asistencia_backend.document.service.DocumentService;
import com.agro.control_asistencia_backend.segurity.service.UserDetailsImpl;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(CertificateController.class)
@AutoConfigureMockMvc(addFilters = false)
public class CertificateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DocumentService documentService;

    @MockBean
    private com.agro.control_asistencia_backend.segurity.jwt.JwtUtils jwtUtils;

    @MockBean
    private com.agro.control_asistencia_backend.segurity.service.UserDetailsServiceImpl userDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testRegisterCertificate() throws Exception {
        CertificateCreationDTO creationDTO = new CertificateCreationDTO();
        creationDTO.setEmployeeId(1L);
        creationDTO.setTitle("Certificado de Trabajo");
        creationDTO.setDocumentType("CERTIFICADO");
        creationDTO.setIssueDate(LocalDate.now());
        creationDTO.setStoragePath("uploads/cert.pdf");

        DocumentResponseDTO responseDTO = DocumentResponseDTO.builder()
                .id(1L)
                .fileName("cert.pdf")
                .build();

        when(documentService.createCertificateRecord(any(CertificateCreationDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/api/certificates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(creationDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.fileName").value("cert.pdf"));
    }

    @Test
    public void testGetMyCertificates() throws Exception {
        UserDetailsImpl userDetails = new UserDetailsImpl(1L, "testuser", "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_EMPLOYEE")), true);

        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null,
                userDetails.getAuthorities());

        DocumentResponseDTO responseDTO = DocumentResponseDTO.builder()
                .id(1L)
                .build();

        List<DocumentResponseDTO> certificates = Collections.singletonList(responseDTO);

        when(documentService.getCertificatesByUserId(anyLong())).thenReturn(certificates);

        mockMvc.perform(get("/api/certificates/me")
                .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));
    }
}

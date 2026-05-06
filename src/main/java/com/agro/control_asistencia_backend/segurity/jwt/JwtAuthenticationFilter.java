package com.agro.control_asistencia_backend.segurity.jwt;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;


import com.agro.control_asistencia_backend.segurity.service.UserDetailsServiceImpl;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {


      @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        logger.info("JwtAuthenticationFilter executing for request: {}", request.getRequestURI());
        try {
            // 1. Extraer el Token del encabezado (Ej: "Authorization: Bearer <token>")
            String jwt = parseJwt(request);

            if (jwt != null && jwtUtils.validateJwtToken(jwt)) {
                // 2. Si el token es válido: Obtener el nombre de usuario
                String username = jwtUtils.getUserNameFromJwtToken(jwt);

                if (SecurityContextHolder.getContext().getAuthentication() == null) {
                    logger.info("SecurityContext is null, authenticating user: {}", username);
                    // 3. Cargar los detalles del usuario desde la DB (para roles)
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                    // 4. Crear el objeto de Autenticación de Spring Security
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities());

                    // 5. Establecer detalles de la petición (IP, etc.)
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // 6. Colocar la Autenticación en el Contexto de Spring Security
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } else {
                    logger.info("SecurityContext already has authentication for user: {}", SecurityContextHolder.getContext().getAuthentication().getName());
                }
            }
        } catch (Exception e) {
            // Manejo de errores de autenticación/token
            logger.error("Don't getting autentication: {}", e.getMessage());
        }

        // Continúa al siguiente filtro de la cadena (o al Controller)
        filterChain.doFilter(request, response);
    }

   
    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");

        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            // Retorna solo el token (quitando "Bearer ")
            return headerAuth.substring(7);
        }

        return null;
    }

}

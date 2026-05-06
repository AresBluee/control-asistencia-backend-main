package com.agro.control_asistencia_backend.segurity.jwt;
import java.security.Key;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.agro.control_asistencia_backend.segurity.model.Entity.JWTBlacklist;
import com.agro.control_asistencia_backend.segurity.repository.JWTBlacklistRepository;
import com.agro.control_asistencia_backend.segurity.service.UserDetailsImpl;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;

@Component
public class JwtUtils {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    @Value("${agro.app.jwtSecret}")
    private String jwtSecret;

    @Value("${agro.app.jwtExpirationMs}")
    private int jwtExpirationMs;

    private final JWTBlacklistRepository jwtBlacklistRepository;

    public JwtUtils(JWTBlacklistRepository jwtBlacklistRepository) {
        this.jwtBlacklistRepository = jwtBlacklistRepository;
    }

    public String generateJwtToken(Authentication authentication) {

        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userPrincipal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return Jwts.builder()
                .setSubject((userPrincipal.getUsername()))
                .claim("userId", userPrincipal.getId())
                .claim("roles", roles)
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }

    private Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    public String getUserNameFromJwtToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key()).build()
                .parseClaimsJws(token).getBody().getSubject();
    }

    public Long getUserIdFromJwtToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key()).build()
                .parseClaimsJws(token).getBody().get("userId", Long.class);
    }

    @SuppressWarnings("deprecation")
    public boolean validateJwtToken(String authToken) {
        if (jwtBlacklistRepository.existsByToken(authToken)) {
            System.out.println("Token revocado encontrado en lista negra.");
            return false;
        }
        try {
            // 2. Si no está en la lista negra, validar su firma y expiración
            Jwts.parserBuilder().setSigningKey(key()).build().parse(authToken);
            return true;
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }

    public void invalidateToken(String token, Long userId) {
        try {
            Date expiryUtilDate = Jwts.parserBuilder()
                    .setSigningKey(key())
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getExpiration();

            JWTBlacklist blacklistEntry = new JWTBlacklist(null, token, userId, expiryUtilDate.toInstant());

            jwtBlacklistRepository.save(blacklistEntry);
        } catch (ExpiredJwtException e) {
            logger.warn("Attempted to log out with an already expired token. No action needed.");
        } catch (Exception e) {
            logger.error("Could not invalidate token: {}", e.getMessage());
        }
    }

    public String getJwtFromHeaders(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");

        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }

        return null;
    }
}

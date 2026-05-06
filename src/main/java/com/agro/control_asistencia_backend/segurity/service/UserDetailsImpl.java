package com.agro.control_asistencia_backend.segurity.service;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.agro.control_asistencia_backend.employee.model.entity.User;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NoArgsConstructor
@Getter
@Setter
public class UserDetailsImpl implements UserDetails {

    private static final Logger logger = LoggerFactory.getLogger(UserDetailsImpl.class);

    private static final long serialVersionUID = 1L;

    private Long id;
    private String username;

    @JsonIgnore
    private String password;

    private boolean isEnabled;

    private Collection<? extends GrantedAuthority> authorities;

    public UserDetailsImpl(Long id, String username, String password,
            Collection<? extends GrantedAuthority> authorities, boolean isEnabled) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.authorities = authorities;
        this.isEnabled = isEnabled;
    }

    public static UserDetailsImpl build(User user) {

        String roleName = user.getRole().getName().name();
        logger.info("Building UserDetails for {} with role: {}", user.getUsername(), roleName);
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(roleName);

        return new UserDetailsImpl(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                Collections.singletonList(authority),
                user.isEnabled());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        UserDetailsImpl user = (UserDetailsImpl) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public boolean isEnabled() {
        return isEnabled; 
    }
}

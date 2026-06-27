package com.example.backend.security;

import java.util.List;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.example.backend.model.User;

public class CustomUserDetails implements UserDetails {
    private static final String ACTIVE_STATUS = "ACTIVE";
    private static final String SUSPENDED_STATUS = "SUSPENDED";
    private static final String DELETED_STATUS = "DELETED";

    private final User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }

    @Override
    public List<SimpleGrantedAuthority> getAuthorities() {
        return List.of(
                new SimpleGrantedAuthority("ROLE_" + user.getRole().getRoleName()));

    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return !hasStatus(DELETED_STATUS);
    }

    @Override
    public boolean isAccountNonLocked() {
        return !hasStatus(SUSPENDED_STATUS) && !hasStatus(DELETED_STATUS);
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return hasStatus(ACTIVE_STATUS);
    }

    private boolean hasStatus(String status) {
        return user.getStatus() != null && status.equalsIgnoreCase(user.getStatus());
    }
}

package com.example.backend.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import com.example.backend.model.Role;
import com.example.backend.model.User;
import com.example.backend.service.CustomUserDetailsService;

import jakarta.servlet.ServletException;

import java.io.IOException;

@ExtendWith(MockitoExtension.class)
class JwtFilterTests {
    @Mock
    private JwtService jwtService;
    @Mock
    private CustomUserDetailsService userDetailsService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void rejectsTokenForSuspendedAccount() throws ServletException, IOException {
        JwtFilter filter = new JwtFilter(jwtService, userDetailsService);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer old-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();
        User suspendedUser = user("assistant@manga.test", "SUSPENDED");

        when(jwtService.extractEmail("old-token")).thenReturn(suspendedUser.getEmail());
        when(userDetailsService.loadUserByUsername(suspendedUser.getEmail()))
                .thenReturn(new CustomUserDetails(suspendedUser));

        filter.doFilter(request, response, chain);

        assertEquals(401, response.getStatus());
        verify(userDetailsService).loadUserByUsername(suspendedUser.getEmail());
    }

    @Test
    void authenticatesActiveAccount() throws ServletException, IOException {
        JwtFilter filter = new JwtFilter(jwtService, userDetailsService);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer active-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();
        User activeUser = user("assistant@manga.test", "ACTIVE");

        when(jwtService.extractEmail("active-token")).thenReturn(activeUser.getEmail());
        when(userDetailsService.loadUserByUsername(activeUser.getEmail()))
                .thenReturn(new CustomUserDetails(activeUser));

        filter.doFilter(request, response, chain);

        assertEquals(200, response.getStatus());
        verify(jwtService).extractEmail("active-token");
    }

    @Test
    void skipsRequestWithoutBearerToken() throws ServletException, IOException {
        JwtFilter filter = new JwtFilter(jwtService, userDetailsService);
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertEquals(200, response.getStatus());
        verify(jwtService, never()).extractEmail("old-token");
    }

    private User user(String email, String status) {
        Role role = new Role();
        role.setRoleName("ASSISTANT");
        User user = new User();
        user.setEmail(email);
        user.setUsername(email);
        user.setPassword("encoded-password");
        user.setStatus(status);
        user.setRole(role);
        return user;
    }
}

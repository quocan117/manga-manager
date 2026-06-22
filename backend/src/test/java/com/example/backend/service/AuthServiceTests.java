package com.example.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.backend.dto.LoginRequest;
import com.example.backend.model.Role;
import com.example.backend.model.User;
import com.example.backend.repository.UserRepository;
import com.example.backend.security.JwtService;

@ExtendWith(MockitoExtension.class)
class AuthServiceTests {
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService service;

    @Test
    void loginReturnsTokenAndUserProfile() {
        Role role = new Role();
        role.setRoleName("MANGAKA");
        User user = new User();
        user.setUserId(1L);
        user.setUsername("Eiichiro Oda");
        user.setEmail("oda@manga.test");
        user.setPassword("encoded-password");
        user.setRole(role);
        LoginRequest request = new LoginRequest();
        request.setEmail(user.getEmail());
        request.setPassword("password");

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password", "encoded-password")).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("jwt-token");

        var response = service.login(request);

        assertEquals("jwt-token", response.getToken());
        assertEquals(1L, response.getUserId());
        assertEquals("Eiichiro Oda", response.getUsername());
        assertEquals("oda@manga.test", response.getEmail());
        assertEquals("MANGAKA", response.getRole());
    }
}

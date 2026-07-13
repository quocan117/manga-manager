package com.example.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import com.example.backend.dto.GoogleLoginRequest;
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
    @Mock
    private GoogleTokenVerifier googleTokenVerifier;

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
        user.setStatus("ACTIVE");
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

    @Test
    void loginRejectsSuspendedAccount() {
        Role role = new Role();
        role.setRoleName("MANGAKA");
        User user = new User();
        user.setEmail("oda@manga.test");
        user.setPassword("encoded-password");
        user.setStatus("SUSPENDED");
        user.setRole(role);
        LoginRequest request = new LoginRequest();
        request.setEmail(user.getEmail());
        request.setPassword("password");

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password", "encoded-password")).thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.login(request));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        verify(jwtService, never()).generateToken(user);
    }

    @Test
    void googleLoginReturnsTokenForExistingActiveUser() {
        User user = user("oda@manga.test", "ACTIVE");
        user.setUserId(1L);
        user.setUsername("Eiichiro Oda");
        GoogleLoginRequest request = googleRequest("google-id-token");

        when(googleTokenVerifier.verifyEmail("google-id-token")).thenReturn(user.getEmail());
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("jwt-token");

        var response = service.googleLogin(request);

        assertEquals("jwt-token", response.getToken());
        assertEquals(1L, response.getUserId());
        assertEquals("Eiichiro Oda", response.getUsername());
        assertEquals("oda@manga.test", response.getEmail());
        assertEquals("MANGAKA", response.getRole());
        verify(passwordEncoder, never()).matches(org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void googleLoginRejectsEmailThatWasNotCreated() {
        GoogleLoginRequest request = googleRequest("google-id-token");

        when(googleTokenVerifier.verifyEmail("google-id-token")).thenReturn("new-user@gmail.com");
        when(userRepository.findByEmail("new-user@gmail.com")).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.googleLogin(request));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Tài khoản Google này chưa được tạo trong hệ thống", exception.getReason());
        verify(jwtService, never()).generateToken(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void googleLoginRejectsInactiveAccount() {
        User user = user("oda@manga.test", "SUSPENDED");
        GoogleLoginRequest request = googleRequest("google-id-token");

        when(googleTokenVerifier.verifyEmail("google-id-token")).thenReturn(user.getEmail());
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.googleLogin(request));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        assertEquals("Account is not active", exception.getReason());
        verify(jwtService, never()).generateToken(user);
    }

    private GoogleLoginRequest googleRequest(String idToken) {
        GoogleLoginRequest request = new GoogleLoginRequest();
        request.setIdToken(idToken);
        return request;
    }

    private User user(String email, String status) {
        Role role = new Role();
        role.setRoleName("MANGAKA");
        User user = new User();
        user.setEmail(email);
        user.setStatus(status);
        user.setRole(role);
        return user;
    }
}

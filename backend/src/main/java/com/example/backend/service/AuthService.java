package com.example.backend.service;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.backend.dto.LoginRequest;
import com.example.backend.dto.LoginResponse;
import com.example.backend.dto.RegisterRequest;
import com.example.backend.model.User;
import com.example.backend.repository.UserRepository;
import com.example.backend.security.JwtService;

@Service
public class AuthService {
    private static final String ACTIVE_STATUS = "ACTIVE";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public void register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setStatus("ACTIVE");
        user.setCreatedAt(LocalDateTime.now());

        userRepository.save(user);

    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or Password"));

        boolean matched = passwordEncoder.matches(
                request.getPassword(), user.getPassword());
        if (!matched) {
            throw new RuntimeException("Incorrect Email or Password");
        }

        if("DELETE".equalsIgnoreCase(user.getStatus())){
            throw new RunTimeException("Account does not exist");
        }

        if (!isActive(user)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User account is not active");
        }

        String token = jwtService.generateToken(user);
        return new LoginResponse(
                token,
                user.getUserId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole().getRoleName());

    }

    private boolean isActive(User user) {
        return user.getStatus() != null && ACTIVE_STATUS.equalsIgnoreCase(user.getStatus());
    }
}

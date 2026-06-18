package com.example.backend.service;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.example.backend.dto.RegistrationRequestDTO;
import com.example.backend.model.RegistrationRequest;
import com.example.backend.repository.RegistrationRequestRepository;
import com.example.backend.repository.UserRepository;

@Service
public class RegistrationRequestService {
    private static final Set<String> ALLOWED_ROLES = Set.of(
            "MANGAKA", "ASSISTANT", "TANTOU_EDITOR");

    private final RegistrationRequestRepository repository;
    private final UserRepository userRepository;

    public RegistrationRequestService(
            RegistrationRequestRepository repository,
            UserRepository userRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
    }

    public RegistrationRequest create(RegistrationRequestDTO dto) {
        if (repository.existsByEmail(dto.getEmail()) || userRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email already registered.");
        }

        String requestedRole = normalizeRequestedRole(dto.getRequestedRole());

        RegistrationRequest request = new RegistrationRequest();
        request.setFullName(dto.getFullName());
        request.setEmail(dto.getEmail());
        request.setPortfolioUrl(dto.getPortfolioUrl());
        request.setIntroduction(dto.getIntroduction());
        request.setPhoneNumber(dto.getPhoneNumber());
        request.setRequestedRole(requestedRole);
        request.setStatus("PENDING");

        request.setCreatedAt(LocalDateTime.now());

        return repository.save(request);

    }

    private String normalizeRequestedRole(String requestedRole) {
        if (requestedRole == null || requestedRole.isBlank()) {
            return "MANGAKA";
        }

        String normalizedRole = requestedRole.trim()
                .toUpperCase(Locale.ROOT)
                .replace('-', '_')
                .replace(' ', '_');

        if (!ALLOWED_ROLES.contains(normalizedRole)) {
            throw new IllegalArgumentException(
                    "Requested role must be MANGAKA, ASSISTANT, or TANTOU_EDITOR.");
        }

        return normalizedRole;
    }
}

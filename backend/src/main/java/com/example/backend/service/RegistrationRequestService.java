package com.example.backend.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.example.backend.dto.RegistrationRequestDTO;
import com.example.backend.model.RegistrationRequest;
import com.example.backend.repository.RegistrationRequestRepository;

@Service
public class RegistrationRequestService {
    private final RegistrationRequestRepository repository;

    public RegistrationRequestService(RegistrationRequestRepository repository) {
        this.repository = repository;
    }

    public RegistrationRequest create(RegistrationRequestDTO dto) {
        if (repository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email already registered.");
        }
        RegistrationRequest request = new RegistrationRequest();
        request.setFullName(dto.getFullName());
        request.setEmail(dto.getEmail());
        request.setPortfolioUrl(dto.getPortfolioUrl());
        request.setIntroduction(dto.getIntroduction());
        request.setPhoneNumber(dto.getPhoneNumber());
        request.setStatus("PENDING");

        request.setCreatedAt(LocalDateTime.now());

        return repository.save(request);

    }
}

package com.example.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.backend.model.RegistrationRequest;

public interface RegistrationRequestRepository
        extends JpaRepository<RegistrationRequest, Long> {
    boolean existsexistsByEmail(String email);
}

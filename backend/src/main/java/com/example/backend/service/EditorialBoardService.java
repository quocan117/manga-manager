package com.example.backend.service;

import com.example.backend.dto.ReviewRegistrationRequest;
import com.example.backend.model.RegistrationRequest;
import com.example.backend.model.Role;
import com.example.backend.model.User;
import com.example.backend.repository.RegistrationRequestRepository;
import com.example.backend.repository.RoleRepository;
import com.example.backend.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EditorialBoardService {

    private final RegistrationRequestRepository requestRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public EditorialBoardService(
            RegistrationRequestRepository requestRepository,
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder) {
        this.requestRepository = requestRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<RegistrationRequest> getAllRequests() {
        return requestRepository.findAll();
    }

    public RegistrationRequest approve(Long requestId, ReviewRegistrationRequest dto) {
        RegistrationRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Registration request not found"));

        if (!"PENDING".equals(request.getStatus())) {
            throw new RuntimeException("Request already reviewed");
        }

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        User editorialBoard = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Editorial board user not found"));

        Role mangakaRole = roleRepository.findByRoleName("MANGAKA")
                .orElseThrow(() -> new RuntimeException("MANGAKA role not found"));

        User mangaka = new User();
        mangaka.setUsername(request.getEmail());
        mangaka.setEmail(request.getEmail());
        mangaka.setPassword(passwordEncoder.encode(dto.getTempPassword()));
        mangaka.setStatus("ACTIVE");
        mangaka.setCreatedAt(LocalDateTime.now());
        mangaka.setRole(mangakaRole);

        userRepository.save(mangaka);

        request.setStatus("APPROVED");
        request.setReviewNote(dto.getReviewNote());
        request.setReviewedAt(LocalDateTime.now());
        request.setReviewedBy(editorialBoard);

        return requestRepository.save(request);
    }

    public RegistrationRequest reject(Long requestId, ReviewRegistrationRequest dto) {
        RegistrationRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Registration request not found"));

        if (!"PENDING".equals(request.getStatus())) {
            throw new RuntimeException("Request already reviewed");
        }

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        User editorialBoard = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Editorial board user not found"));

        request.setStatus("REJECTED");
        request.setReviewNote(dto.getReviewNote());
        request.setReviewedAt(LocalDateTime.now());
        request.setReviewedBy(editorialBoard);

        return requestRepository.save(request);
    }
}
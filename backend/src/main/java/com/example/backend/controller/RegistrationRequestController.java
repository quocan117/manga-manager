package com.example.backend.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.dto.RegistrationRequestDTO;
import com.example.backend.model.RegistrationRequest;
import com.example.backend.service.RegistrationRequestService;

@RestController
@RequestMapping("/registration-request")
public class RegistrationRequestController {
    private final RegistrationRequestService service;

    public RegistrationRequestController(
            RegistrationRequestService service) {

        this.service = service;

    }

    @PostMapping
    public RegistrationRequest createRequest(
            @RequestBody RegistrationRequestDTO dto) {

        return service.create(dto);

    }
}

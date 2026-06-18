package com.example.backend.controller;

import com.example.backend.dto.ReviewRegistrationRequest;
import com.example.backend.model.RegistrationRequest;
import com.example.backend.service.EditorialBoardService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/editorial-board")
@CrossOrigin(origins = "*")
public class EditorialBoardController {

    private final EditorialBoardService service;

    public EditorialBoardController(EditorialBoardService service) {
        this.service = service;
    }

    @PreAuthorize("hasRole('EDITORIAL_BOARD')")
    @GetMapping("/registration-requests")
    public List<RegistrationRequest> getAllRequests() {
        return service.getAllRequests();
    }

    @PreAuthorize("hasRole('EDITORIAL_BOARD')")
    @PutMapping("/registration-requests/{id}/approve")
    public RegistrationRequest approve(
            @PathVariable Long id,
            @RequestBody ReviewRegistrationRequest request) {
        return service.approve(id, request);
    }

    @PreAuthorize("hasRole('EDITORIAL_BOARD')")
    @PutMapping("/registration-requests/{id}/reject")
    public RegistrationRequest reject(
            @PathVariable Long id,
            @RequestBody ReviewRegistrationRequest request) {
        return service.reject(id, request);
    }
}

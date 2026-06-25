package com.example.backend.controller;

import com.example.backend.dto.EditorialBoardDtos.CreateUserRequest;
import com.example.backend.dto.EditorialBoardDtos.BoardDecisionRequest;
import com.example.backend.dto.EditorialBoardDtos.BoardDecisionResponse;
import com.example.backend.dto.EditorialBoardDtos.ReviewSeriesResponse;
import com.example.backend.dto.EditorialBoardDtos.UpdateUserRequest;
import com.example.backend.dto.EditorialBoardDtos.UserResponse;
import com.example.backend.dto.ReviewRegistrationRequest;
import com.example.backend.model.RegistrationRequest;
import com.example.backend.service.EditorialBoardService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/editorial-board")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('EDITORIAL_BOARD')")
@SecurityRequirement(name = "Bearer Authentication")
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
    @GetMapping("/series/reviewing")
    public List<ReviewSeriesResponse> getReviewingSeries() {
        return service.getReviewingSeries();
    }

    @PreAuthorize("hasRole('EDITORIAL_BOARD')")
    @GetMapping("/series/{id}/review")
    public ReviewSeriesResponse getSeriesReview(@PathVariable Long id) {
        return service.getSeriesReview(id);
    }

    @PreAuthorize("hasRole('EDITORIAL_BOARD')")
    @GetMapping("/series/{id}/decisions")
    public List<BoardDecisionResponse> getSeriesDecisions(@PathVariable Long id) {
        return service.getSeriesDecisions(id);
    }

    @PreAuthorize("hasRole('EDITORIAL_BOARD')")
    @PostMapping("/series/{id}/decisions")
    public ReviewSeriesResponse voteSeries(
            @PathVariable Long id,
            @Valid @RequestBody BoardDecisionRequest request) {
        return service.voteSeries(id, request);
    }

    @PreAuthorize("hasRole('EDITORIAL_BOARD')")
    @PutMapping("/series/{id}/cancel")
    public ReviewSeriesResponse cancelSeries(
            @PathVariable Long id,
            @RequestBody(required = false) BoardDecisionRequest request) {
        return service.cancelSeries(id, request);
    }

    @PreAuthorize("hasRole('EDITORIAL_BOARD')")
    @GetMapping("/users")
    public List<UserResponse> getUsers() {
        return service.getUsers();
    }

    @PreAuthorize("hasRole('EDITORIAL_BOARD')")
    @PostMapping("/users")
    public UserResponse createUser(@Valid @RequestBody CreateUserRequest request) {
        return service.createUser(request);
    }

    @PreAuthorize("hasRole('EDITORIAL_BOARD')")
    @PutMapping("/users/{id}")
    public UserResponse updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request) {
        return service.updateUser(id, request);
    }

    @PreAuthorize("hasRole('EDITORIAL_BOARD')")
    @DeleteMapping("/users/{id}")
    public UserResponse deleteUser(@PathVariable Long id) {
        return service.deleteUser(id);
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

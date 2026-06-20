package com.example.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.dto.GuestAccessRequest;
import com.example.backend.dto.GuestAccessResponse;
import com.example.backend.service.GuestAccessService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping({ "/guest-access", "/api/guest/access" })
@CrossOrigin(origins = "*")
public class GuestAccessController {
    private final GuestAccessService service;

    public GuestAccessController(GuestAccessService service) {
        this.service = service;

    }

    @PostMapping
    public ResponseEntity<GuestAccessResponse> createGuest(
            @RequestBody GuestAccessRequest request,
            HttpServletRequest httpRequest) {
        return ResponseEntity.ok(service.createGuest(request, resolveIpAddress(httpRequest)));

    }

    private String resolveIpAddress(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}

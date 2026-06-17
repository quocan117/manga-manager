package com.example.backend.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.example.backend.dto.GuestAccessRequest;
import com.example.backend.dto.GuestAccessResponse;
import com.example.backend.model.GuestAccessLog;
import com.example.backend.repository.GuestAccessLogRepository;

@Service
public class GuestAccessService {
    private final GuestAccessLogRepository guestAccessLogRepository;

    public GuestAccessService(GuestAccessLogRepository guestAccessLogRepository) {
        this.guestAccessLogRepository = guestAccessLogRepository;
    }

    public GuestAccessResponse createGuest(GuestAccessRequest request) {
        GuestAccessLog guest = new GuestAccessLog();
        guest.setSessionToken(request.getSessionToken());
        guest.setIpAddress(request.getIpAddress());
        guest.setUserAgent(request.getUserAgent());
        guest.setCreatedAt(LocalDateTime.now());
        guest.setLastActiveAt(LocalDateTime.now());
        GuestAccessLog saved = guestAccessLogRepository.save(guest);
        GuestAccessResponse response = new GuestAccessResponse();
        response.setLogId(saved.getLogId());
        return response;
    }
}

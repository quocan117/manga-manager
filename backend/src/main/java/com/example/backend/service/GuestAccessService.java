package com.example.backend.service;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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

    @Transactional
    public GuestAccessResponse createGuest(GuestAccessRequest request, String ipAddress) {
        if (request.getSessionToken() == null || request.getSessionToken().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "sessionToken is required");
        }

        String sessionToken = request.getSessionToken().trim();
        GuestAccessLog guest = guestAccessLogRepository.findBySessionToken(sessionToken)
                .orElseGet(GuestAccessLog::new);
        boolean isNewGuest = guest.getLogId() == null;

        guest.setSessionToken(sessionToken);
        guest.setIpAddress(ipAddress);
        guest.setUserAgent(request.getUserAgent());
        if (isNewGuest) {
            guest.setCreatedAt(LocalDateTime.now());
        }
        guest.setLastActiveAt(LocalDateTime.now());
        GuestAccessLog saved = guestAccessLogRepository.save(guest);
        GuestAccessResponse response = new GuestAccessResponse();
        response.setLogId(saved.getLogId());
        return response;
    }
}

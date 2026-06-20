package com.example.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.backend.dto.GuestAccessRequest;
import com.example.backend.model.GuestAccessLog;
import com.example.backend.repository.GuestAccessLogRepository;

@ExtendWith(MockitoExtension.class)
class GuestAccessServiceTests {
    @Mock
    private GuestAccessLogRepository repository;

    @InjectMocks
    private GuestAccessService service;

    @Test
    void existingSessionTokenReturnsTheSameLogId() {
        LocalDateTime createdAt = LocalDateTime.now().minusDays(1);
        GuestAccessLog existingGuest = new GuestAccessLog();
        existingGuest.setLogId(7L);
        existingGuest.setSessionToken("stable-token");
        existingGuest.setCreatedAt(createdAt);

        GuestAccessRequest request = new GuestAccessRequest();
        request.setSessionToken("stable-token");
        request.setUserAgent("new-agent");

        when(repository.findBySessionToken("stable-token")).thenReturn(Optional.of(existingGuest));
        when(repository.save(any(GuestAccessLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.createGuest(request, "127.0.0.1");

        assertEquals(7L, response.getLogId());
        assertSame(createdAt, existingGuest.getCreatedAt());
        assertEquals("127.0.0.1", existingGuest.getIpAddress());
        verify(repository).save(existingGuest);
    }
}

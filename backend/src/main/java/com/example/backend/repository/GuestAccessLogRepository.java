package com.example.backend.repository;

import com.example.backend.model.GuestAccessLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GuestAccessLogRepository extends JpaRepository<GuestAccessLog, Long> {
    Optional<GuestAccessLog> findBySessionToken(String sessionToken);
}

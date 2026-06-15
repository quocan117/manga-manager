package com.example.backend.repository;

import com.example.backend.model.GuestAccessLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GuestAccessLogRepository extends JpaRepository<GuestAccessLog, Long> {
}

package com.example.backend.repository;

import com.example.backend.model.GuestAccessLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GuestAccessLogRepository extends JpaRepository<GuestAccessLog, Long> {
}

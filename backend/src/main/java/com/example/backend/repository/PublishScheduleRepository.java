package com.example.backend.repository;

import com.example.backend.model.PublishSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PublishScheduleRepository extends JpaRepository<PublishSchedule, Long> {
}

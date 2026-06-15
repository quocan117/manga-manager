package com.example.backend.repository;

import com.example.backend.model.PublishSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PublishScheduleRepository extends JpaRepository<PublishSchedule, Long> {
}

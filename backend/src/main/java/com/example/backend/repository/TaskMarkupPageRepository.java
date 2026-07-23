package com.example.backend.repository;

import com.example.backend.model.TaskMarkupPage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskMarkupPageRepository extends JpaRepository<TaskMarkupPage, Long> {
    List<TaskMarkupPage> findByTaskTaskIdAndRoundNumberOrderByOrderIndexAscCreatedAtAsc(
            Long taskId,
            Integer roundNumber);
}

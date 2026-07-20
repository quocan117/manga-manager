package com.example.backend.repository;

import com.example.backend.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByChapterChapterIdAndAssignedByEmailOrderByCreatedAtDesc(
            Long chapterId, String assignedByEmail);

    List<Task> findByAssignedToEmailOrderByCreatedAtDesc(String assignedToEmail);

    List<Task> findByChapterSeriesSeriesIdOrderByDueDateAsc(Long seriesId);

    List<Task> findByChapterSeriesSeriesIdAndAssignedByEmailOrderByCreatedAtDesc(
            Long seriesId, String assignedByEmail);

    List<Task> findByChapterChapterIdOrderByDueDateAsc(Long chapterId);

    List<Task> findAllByOrderByDueDateAsc();
}

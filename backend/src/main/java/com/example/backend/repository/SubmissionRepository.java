package com.example.backend.repository;

import com.example.backend.model.Submission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    List<Submission> findByChapterChapterIdOrderBySubmittedAtDesc(Long chapterId);

    List<Submission> findByTaskTaskIdOrderBySubmittedAtDesc(Long taskId);

    List<Submission> findByChapterSeriesSeriesIdOrderBySubmittedAtDesc(Long seriesId);
}

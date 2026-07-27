package com.example.backend.repository;

import com.example.backend.model.ReaderFeedbackImport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReaderFeedbackImportRepository extends JpaRepository<ReaderFeedbackImport, Long> {
    List<ReaderFeedbackImport> findAllByOrderByImportedAtDesc();

    List<ReaderFeedbackImport> findBySeriesSeriesIdOrderByImportedAtDesc(Long seriesId);

    Optional<ReaderFeedbackImport> findBySeriesSeriesIdAndPeriodStartAndPeriodEnd(
            Long seriesId,
            LocalDateTime periodStart,
            LocalDateTime periodEnd);
}

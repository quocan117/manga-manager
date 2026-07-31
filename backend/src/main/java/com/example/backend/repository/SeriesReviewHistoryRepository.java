package com.example.backend.repository;

import com.example.backend.model.SeriesReviewHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SeriesReviewHistoryRepository extends JpaRepository<SeriesReviewHistory, Long> {
    List<SeriesReviewHistory> findBySeriesSeriesIdOrderByCreatedAtDesc(Long seriesId);
}

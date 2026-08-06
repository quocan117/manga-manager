package com.example.backend.repository;

import com.example.backend.model.SeriesReviewHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Collection;

public interface SeriesReviewHistoryRepository extends JpaRepository<SeriesReviewHistory, Long> {
    List<SeriesReviewHistory> findBySeriesSeriesIdOrderByCreatedAtDesc(Long seriesId);

    List<SeriesReviewHistory> findBySeriesSeriesIdOrderByCreatedAtAsc(Long seriesId);

    List<SeriesReviewHistory> findByActionInOrderByCreatedAtDesc(Collection<String> actions);
}

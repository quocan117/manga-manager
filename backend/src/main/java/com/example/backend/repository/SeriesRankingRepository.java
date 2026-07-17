package com.example.backend.repository;

import com.example.backend.model.SeriesRanking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SeriesRankingRepository extends JpaRepository<SeriesRanking, Long> {
    List<SeriesRanking> findBySeriesAuthorEmailOrderByCalculatedAtDesc(String email);

    List<SeriesRanking> findAllByOrderByCalculatedAtDesc();

    Optional<SeriesRanking> findBySeriesSeriesIdAndPeriod(Long seriesId, String period);
}

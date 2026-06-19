package com.example.backend.repository;

import com.example.backend.model.SeriesRanking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SeriesRankingRepository extends JpaRepository<SeriesRanking, Long> {
    List<SeriesRanking> findBySeriesAuthorEmailOrderByCalculatedAtDesc(String email);
}

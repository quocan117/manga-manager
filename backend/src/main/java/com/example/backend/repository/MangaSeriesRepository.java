package com.example.backend.repository;

import com.example.backend.model.MangaSeries;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MangaSeriesRepository extends JpaRepository<MangaSeries, Long> {
    List<MangaSeries> findAllByOrderByCreatedAtDesc();

    List<MangaSeries> findByStatusIgnoreCaseOrderByCreatedAtDesc(String status);

    List<MangaSeries> findByStatusIgnoreCaseOrderBySubmittedAtDesc(String status);

    Optional<MangaSeries> findBySeriesIdAndStatusIgnoreCase(Long seriesId, String status);

    List<MangaSeries> findByAuthorEmailOrderByCreatedAtDesc(String email);
}

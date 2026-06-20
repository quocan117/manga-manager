package com.example.backend.repository;

import com.example.backend.model.MangaSeries;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MangaSeriesRepository extends JpaRepository<MangaSeries, Long> {
    List<MangaSeries> findAllByOrderByCreatedAtDesc();

    List<MangaSeries> findByAuthorEmailOrderByCreatedAtDesc(String email);
}

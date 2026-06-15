package com.example.backend.repository;

import com.example.backend.model.MangaSeries;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MangaSeriesRepository extends JpaRepository<MangaSeries, Long> {
}

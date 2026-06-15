package com.example.backend.repository;

import com.example.backend.model.MangaSeries;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MangaSeriesRepository extends JpaRepository<MangaSeries, Long> {
}

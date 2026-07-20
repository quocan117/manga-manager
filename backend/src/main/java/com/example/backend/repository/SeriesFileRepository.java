package com.example.backend.repository;

import com.example.backend.model.SeriesFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SeriesFileRepository extends JpaRepository<SeriesFile, Long> {
    List<SeriesFile> findBySeriesSeriesIdOrderByUploadedAtDesc(Long seriesId);
}

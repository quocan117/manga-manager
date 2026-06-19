package com.example.backend.repository;

import com.example.backend.model.Chapter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChapterRepository extends JpaRepository<Chapter, Long> {
    List<Chapter> findBySeriesSeriesIdOrderByChapterNumberAsc(Long seriesId);

    boolean existsBySeriesSeriesIdAndChapterNumber(Long seriesId, Integer chapterNumber);
}

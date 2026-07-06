package com.example.backend.repository;

import com.example.backend.model.Chapter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChapterRepository extends JpaRepository<Chapter, Long> {
    List<Chapter> findBySeriesSeriesIdOrderByChapterNumberAsc(Long seriesId);

    List<Chapter> findBySeriesSeriesIdAndStatusIgnoreCaseOrderByChapterNumberAsc(
            Long seriesId, String status);

    List<Chapter> findBySeriesTantouEditorEmailAndStatusIgnoreCaseOrderByCreatedAtDesc(
            String email, String status);

    boolean existsBySeriesSeriesIdAndChapterNumber(Long seriesId, Integer chapterNumber);
}

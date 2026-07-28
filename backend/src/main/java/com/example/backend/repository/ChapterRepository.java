package com.example.backend.repository;

import com.example.backend.model.Chapter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChapterRepository extends JpaRepository<Chapter, Long> {
    List<Chapter> findBySeriesSeriesIdOrderByChapterNumberAsc(Long seriesId);

    List<Chapter> findBySeriesSeriesIdAndStatusIgnoreCaseOrderByChapterNumberAsc(
            Long seriesId, String status);

    Optional<Chapter> findFirstBySeriesSeriesIdAndStatusIgnoreCaseOrderByChapterNumberAsc(
            Long seriesId, String status);

    Optional<Chapter> findFirstBySeriesSeriesIdOrderByChapterNumberAsc(Long seriesId);

    List<Chapter> findBySeriesTantouEditorEmailAndStatusIgnoreCaseOrderByCreatedAtDesc(
            String email, String status);

    long countBySeriesSeriesId(Long seriesId);

    long countBySeriesSeriesIdAndStatusIgnoreCase(Long seriesId, String status);

    boolean existsBySeriesSeriesIdAndChapterNumber(Long seriesId, Integer chapterNumber);
}

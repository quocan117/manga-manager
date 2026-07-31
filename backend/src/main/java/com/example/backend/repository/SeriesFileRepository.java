package com.example.backend.repository;

import com.example.backend.model.SeriesFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SeriesFileRepository extends JpaRepository<SeriesFile, Long> {
    List<SeriesFile> findBySeriesSeriesIdAndPurposeAndActiveTrueOrderByUploadedAtDesc(
            Long seriesId,
            String purpose);

    List<SeriesFile> findBySeriesSeriesIdAndPurposeOrderByUploadedAtDesc(
            Long seriesId,
            String purpose);

    List<SeriesFile> findByChapterChapterIdAndPurposeAndActiveTrueOrderByUploadedAtAsc(
            Long chapterId,
            String purpose);

    List<SeriesFile> findByChapterChapterIdAndPurposeOrderByUploadedAtDesc(
            Long chapterId,
            String purpose);

    List<SeriesFile> findByTaskTaskIdAndRoundNumberAndPurposeAndActiveTrueOrderByUploadedAtAsc(
            Long taskId,
            Integer roundNumber,
            String purpose);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update SeriesFile file
            set file.active = false
            where file.series.seriesId = :seriesId
              and file.purpose = :purpose
              and file.active = true
            """)
    int deactivateActiveFiles(
            @Param("seriesId") Long seriesId,
            @Param("purpose") String purpose);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update SeriesFile file
            set file.active = false
            where file.chapter.chapterId = :chapterId
              and file.purpose = :purpose
              and file.active = true
            """)
    int deactivateActiveChapterFiles(
            @Param("chapterId") Long chapterId,
            @Param("purpose") String purpose);
}

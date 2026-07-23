package com.example.backend.repository;

import com.example.backend.model.SeriesFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SeriesFileRepository extends JpaRepository<SeriesFile, Long> {
    List<SeriesFile> findBySeriesSeriesIdAndActiveTrueOrderByUploadedAtDesc(Long seriesId);

    List<SeriesFile> findByTaskTaskIdAndRoundNumberAndPurposeAndActiveTrueOrderByUploadedAtAsc(
            Long taskId,
            Integer roundNumber,
            String purpose);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update SeriesFile file
            set file.active = false
            where file.series.seriesId = :seriesId
              and file.fileType = :fileType
              and file.active = true
            """)
    int deactivateActiveFiles(
            @Param("seriesId") Long seriesId,
            @Param("fileType") String fileType);
}

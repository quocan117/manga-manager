package com.example.backend.repository;

import com.example.backend.model.MangaSeries;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface MangaSeriesRepository extends JpaRepository<MangaSeries, Long> {
    List<MangaSeries> findAllByOrderByCreatedAtDesc();

    List<MangaSeries> findByStatusIgnoreCaseOrderByCreatedAtDesc(String status);

    List<MangaSeries> findByStatusIgnoreCaseOrderBySubmittedAtDesc(String status);

    Optional<MangaSeries> findBySeriesIdAndStatusIgnoreCase(Long seriesId, String status);

    List<MangaSeries> findByAuthorEmailOrderByCreatedAtDesc(String email);

    long countByTantouEditorUserIdAndStatusIn(Long editorId, Collection<String> statuses);

    @Query("""
            select series from MangaSeries series
            where series.tantouEditor is null or series.tantouEditor.email = :email
            order by series.createdAt desc
            """)
    List<MangaSeries> findVisibleToTantouEditorOrderByCreatedAtDesc(@Param("email") String email);

    @Query("""
            select series from MangaSeries series
            where lower(series.status) = lower(:status)
              and (series.tantouEditor is null or series.tantouEditor.email = :email)
            order by series.submittedAt desc
            """)
    List<MangaSeries> findVisibleToTantouEditorByStatusOrderBySubmittedAtDesc(
            @Param("email") String email,
            @Param("status") String status);
}

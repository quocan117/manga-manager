package com.example.backend.repository;

import com.example.backend.model.MangaSeries;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface MangaSeriesRepository extends JpaRepository<MangaSeries, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select series from MangaSeries series where series.seriesId = :seriesId")
    Optional<MangaSeries> findByIdForUpdate(@Param("seriesId") Long seriesId);

    List<MangaSeries> findAllByOrderByCreatedAtDesc();

    List<MangaSeries> findByStatusIgnoreCaseOrderByCreatedAtDesc(String status);

    List<MangaSeries> findByStatusIgnoreCaseOrderBySubmittedAtDesc(String status);

    Optional<MangaSeries> findBySeriesIdAndStatusIgnoreCase(Long seriesId, String status);

    @Query("""
            select series from MangaSeries series
            where upper(series.status) in :statuses
            order by series.createdAt desc
            """)
    List<MangaSeries> findPublicSeriesByStatusesOrderByCreatedAtDesc(
            @Param("statuses") Collection<String> statuses);

    @Query("""
            select series from MangaSeries series
            where series.seriesId = :seriesId
              and upper(series.status) in :statuses
            """)
    Optional<MangaSeries> findPublicSeriesByIdAndStatuses(
            @Param("seriesId") Long seriesId,
            @Param("statuses") Collection<String> statuses);

    @Query("""
            select series from MangaSeries series
            where upper(series.status) in :statuses
            order by series.createdAt desc
            """)
    List<MangaSeries> findByStatusesOrderByCreatedAtDesc(
            @Param("statuses") Collection<String> statuses);

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

    // Tìm các series đang ở trạng thái chỉ định và được gán trước một mốc thời gian cụ thể
    List<MangaSeries> findByStatusIgnoreCaseAndEditorAssignedAtBefore(String status, LocalDateTime time);

    @Query("""
            select series from MangaSeries series
            where upper(series.status) = upper(:status)
              and series.editorAssignedAt < :time
              and (series.editorAssignmentLocked is null or series.editorAssignmentLocked = false)
            """)
    List<MangaSeries> findOverdueUnlockedSeries(
            @Param("status") String status,
            @Param("time") LocalDateTime time);
}

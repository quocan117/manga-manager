package com.example.backend.repository;

import com.example.backend.model.ChapterLikeLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ChapterLikeLogRepository extends JpaRepository<ChapterLikeLog, Long> {
    long countByChapterChapterId(Long chapterId);

    boolean existsByGuestLogLogIdAndChapterChapterId(Long logId, Long chapterId);

    List<ChapterLikeLog> findByLikedAtBetweenOrderByLikedAtDesc(LocalDateTime from, LocalDateTime to);

    @Query("""
            SELECT l.chapter.series.seriesId AS seriesId,
                   l.chapter.series.title AS seriesTitle,
                   COUNT(l) AS voteCount
            FROM ChapterLikeLog l
            WHERE l.likedAt BETWEEN :from AND :to
            GROUP BY l.chapter.series.seriesId, l.chapter.series.title
            """)
    List<SeriesVoteCount> countVotesBySeriesBetween(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);

    interface SeriesVoteCount {
        Long getSeriesId();

        String getSeriesTitle();

        Long getVoteCount();
    }
}

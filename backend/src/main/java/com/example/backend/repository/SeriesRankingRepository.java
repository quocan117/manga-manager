package com.example.backend.repository;

import com.example.backend.model.SeriesRanking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SeriesRankingRepository extends JpaRepository<SeriesRanking, Long> {
    List<SeriesRanking> findBySeriesAuthorEmailOrderByCalculatedAtDesc(String email);

    List<SeriesRanking> findAllByOrderByCalculatedAtDesc();

    Optional<SeriesRanking> findBySeriesSeriesIdAndPeriodStartAndPeriodEnd(
            Long seriesId,
            LocalDateTime periodStart,
            LocalDateTime periodEnd);

    // Lấy bảng xếp hạng của MỘT chu kỳ cụ thể, sắp theo vị trí xếp hạng
    List<SeriesRanking> findByPeriodStartAndPeriodEndOrderByRankingPositionAsc(
            LocalDateTime periodStart,
            LocalDateTime periodEnd);

    // Danh sách các chu kỳ đã từng tổng hợp (mới nhất trước), để FE render dropdown filter
    @Query("""
            SELECT DISTINCT r.periodStart AS periodStart, r.periodEnd AS periodEnd
            FROM SeriesRanking r
            WHERE r.periodStart IS NOT NULL AND r.periodEnd IS NOT NULL
            ORDER BY r.periodStart DESC, r.periodEnd DESC
            """)
    List<RankingPeriod> findDistinctPeriods();

    @Query("""
            SELECT r
            FROM SeriesRanking r
            WHERE r.periodStart = :periodStart AND r.periodEnd = :periodEnd
            ORDER BY r.voteCount DESC, r.series.title ASC
            """)
    List<SeriesRanking> findForPositionRecalculation(
            @Param("periodStart") LocalDateTime periodStart,
            @Param("periodEnd") LocalDateTime periodEnd);

    // Tổng lượt bình chọn CỘNG DỒN qua mọi chu kỳ, theo từng series (từ khi phát hành đến nay)
    @Query("""
            SELECT r.series.seriesId AS seriesId,
                   r.series.title AS seriesTitle,
                   SUM(r.voteCount) AS totalVotes
            FROM SeriesRanking r
            GROUP BY r.series.seriesId, r.series.title
            """)
    List<SeriesTotalVotes> sumVotesAllPeriodsGroupBySeries();

    interface SeriesTotalVotes {
        Long getSeriesId();

        String getSeriesTitle();

        Long getTotalVotes();
    }

    interface RankingPeriod {
        LocalDateTime getPeriodStart();

        LocalDateTime getPeriodEnd();
    }
}

package com.example.backend.repository;

import com.example.backend.model.SeriesRanking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SeriesRankingRepository extends JpaRepository<SeriesRanking, Long> {
    List<SeriesRanking> findBySeriesAuthorEmailOrderByCalculatedAtDesc(String email);

    List<SeriesRanking> findAllByOrderByCalculatedAtDesc();

    Optional<SeriesRanking> findBySeriesSeriesIdAndPeriod(Long seriesId, String period);

    // Lấy bảng xếp hạng của MỘT chu kỳ cụ thể, sắp theo vị trí xếp hạng
    List<SeriesRanking> findByPeriodOrderByRankingPositionAsc(String period);

    // Danh sách các chu kỳ đã từng tổng hợp (mới nhất trước), để FE render dropdown filter
    @Query("SELECT DISTINCT r.period FROM SeriesRanking r ORDER BY r.period DESC")
    List<String> findDistinctPeriods();

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
}
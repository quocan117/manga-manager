package com.example.backend.repository;

import com.example.backend.model.BoardDecision;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BoardDecisionRepository extends JpaRepository<BoardDecision, Long> {
    List<BoardDecision> findBySeriesSeriesIdOrderByDecisionDateDesc(Long seriesId);

    Optional<BoardDecision> findBySeriesSeriesIdAndBoardMemberUserId(Long seriesId, Long boardMemberId);

    long countBySeriesSeriesIdAndDecisionTypeIgnoreCase(Long seriesId, String decisionType);

    @Query("""
            select decision
            from BoardDecision decision
            where decision.series.seriesId = :seriesId
              and exists (
                    select assignment.assignmentId
                    from SeriesBoardAssignment assignment
                    where assignment.series = decision.series
                      and assignment.boardMember = decision.boardMember
              )
            order by decision.decisionDate desc
            """)
    List<BoardDecision> findPanelDecisionsBySeriesIdOrderByDecisionDateDesc(
            @Param("seriesId") Long seriesId);

    @Query("""
            select count(decision)
            from BoardDecision decision
            where decision.series.seriesId = :seriesId
              and upper(decision.decisionType) = upper(:decisionType)
              and exists (
                    select assignment.assignmentId
                    from SeriesBoardAssignment assignment
                    where assignment.series = decision.series
                      and assignment.boardMember = decision.boardMember
              )
            """)
    long countPanelDecisionsBySeriesIdAndDecisionType(
            @Param("seriesId") Long seriesId,
            @Param("decisionType") String decisionType);

    void deleteBySeriesSeriesId(Long seriesId);
}

package com.example.backend.repository;

import com.example.backend.model.BoardDecision;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BoardDecisionRepository extends JpaRepository<BoardDecision, Long> {
    List<BoardDecision> findBySeriesSeriesIdOrderByDecisionDateDesc(Long seriesId);

    Optional<BoardDecision> findBySeriesSeriesIdAndBoardMemberUserId(Long seriesId, Long boardMemberId);

    long countBySeriesSeriesIdAndDecisionTypeIgnoreCase(Long seriesId, String decisionType);

    void deleteBySeriesSeriesId(Long seriesId);
}

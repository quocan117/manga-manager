package com.example.backend.repository;

import com.example.backend.model.SeriesBoardAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SeriesBoardAssignmentRepository extends JpaRepository<SeriesBoardAssignment, Long> {
    List<SeriesBoardAssignment> findBySeriesSeriesIdOrderByAssignedAtAsc(Long seriesId);

    Optional<SeriesBoardAssignment> findBySeriesSeriesIdAndBoardMemberUserId(Long seriesId, Long boardMemberId);

    long countBySeriesSeriesId(Long seriesId);

    void deleteBySeriesSeriesId(Long seriesId);
}

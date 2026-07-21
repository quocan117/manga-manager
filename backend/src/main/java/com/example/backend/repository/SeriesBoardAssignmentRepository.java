package com.example.backend.repository;

import com.example.backend.model.SeriesBoardAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface SeriesBoardAssignmentRepository extends JpaRepository<SeriesBoardAssignment, Long> {
    List<SeriesBoardAssignment> findBySeriesSeriesIdOrderByAssignedAtAsc(Long seriesId);

    Optional<SeriesBoardAssignment> findBySeriesSeriesIdAndBoardMemberUserId(Long seriesId, Long boardMemberId);

    boolean existsBySeriesSeriesIdAndBoardMemberEmailIgnoreCase(Long seriesId, String email);

    long countBySeriesSeriesId(Long seriesId);

    @Query("""
            select assignment.boardMember.userId as boardMemberId,
                   count(assignment) as assignmentCount
            from SeriesBoardAssignment assignment
            where assignment.boardMember.userId in :boardMemberIds
            group by assignment.boardMember.userId
            """)
    List<BoardMemberAssignmentCount> countAssignmentsByBoardMemberIds(
            @Param("boardMemberIds") Collection<Long> boardMemberIds);

    interface BoardMemberAssignmentCount {
        Long getBoardMemberId();

        long getAssignmentCount();
    }
}

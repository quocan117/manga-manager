package com.example.backend.repository;

import com.example.backend.model.BoardDecision;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BoardDecisionRepository extends JpaRepository<BoardDecision, Long> {
}

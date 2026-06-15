package com.example.backend.repository;

import com.example.backend.model.BoardDecision;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardDecisionRepository extends JpaRepository<BoardDecision, Long> {
}

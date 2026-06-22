package com.example.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.backend.model.PageDrawingRevision;

public interface PageDrawingRevisionRepository extends JpaRepository<PageDrawingRevision, Long> {
    List<PageDrawingRevision> findByDrawingDrawingIdOrderByVersionNumberDesc(Long drawingId);

    Optional<PageDrawingRevision> findByRevisionIdAndDrawingDrawingId(Long revisionId, Long drawingId);
}

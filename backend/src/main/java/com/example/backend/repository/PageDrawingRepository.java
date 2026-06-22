package com.example.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.backend.model.PageDrawing;

public interface PageDrawingRepository extends JpaRepository<PageDrawing, Long> {
    Optional<PageDrawing> findByPagePageIdAndTaskIsNull(Long pageId);
}

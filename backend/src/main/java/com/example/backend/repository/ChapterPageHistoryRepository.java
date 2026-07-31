package com.example.backend.repository;

import com.example.backend.model.ChapterPageHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChapterPageHistoryRepository extends JpaRepository<ChapterPageHistory, Long> {
    List<ChapterPageHistory> findByPagePageIdOrderByCreatedAtDesc(Long pageId);
}

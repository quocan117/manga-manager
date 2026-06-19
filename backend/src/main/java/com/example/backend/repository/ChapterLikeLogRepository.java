package com.example.backend.repository;

import com.example.backend.model.ChapterLikeLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChapterLikeLogRepository extends JpaRepository<ChapterLikeLog, Long> {
    long countByChapterChapterId(Long chapterId);
}

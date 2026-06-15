package com.example.backend.repository;

import com.example.backend.model.ChapterLikeLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChapterLikeLogRepository extends JpaRepository<ChapterLikeLog, Long> {
}

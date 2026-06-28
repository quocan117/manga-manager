package com.example.backend.repository;

import com.example.backend.model.ReviewComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewCommentRepository extends JpaRepository<ReviewComment, Long> {
    List<ReviewComment> findByPagePageIdOrderByCreatedAtDesc(Long pageId);

    List<ReviewComment> findByPageChapterChapterIdOrderByCreatedAtDesc(Long chapterId);

    List<ReviewComment> findByPageChapterSeriesSeriesIdOrderByCreatedAtDesc(Long seriesId);
}

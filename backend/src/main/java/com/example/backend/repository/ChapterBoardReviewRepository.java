package com.example.backend.repository;

import com.example.backend.model.ChapterBoardReview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChapterBoardReviewRepository extends JpaRepository<ChapterBoardReview, Long> {
    List<ChapterBoardReview> findByChapterChapterIdOrderByBoardMemberUsernameAsc(Long chapterId);

    Optional<ChapterBoardReview> findByChapterChapterIdAndBoardMemberUserId(
            Long chapterId, Long boardMemberId);

    List<ChapterBoardReview>
            findByBoardMemberEmailIgnoreCaseAndConfirmedIsNullAndChapterStatusIgnoreCaseOrderByChapterCreatedAtDesc(
                    String email, String chapterStatus);

    long countByChapterChapterId(Long chapterId);

    long countByChapterChapterIdAndConfirmedTrue(Long chapterId);

    boolean existsByChapterChapterIdAndConfirmedFalse(Long chapterId);

    void deleteByChapterChapterId(Long chapterId);
}

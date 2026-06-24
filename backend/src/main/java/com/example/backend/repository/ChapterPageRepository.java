package com.example.backend.repository;

import com.example.backend.model.ChapterPage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChapterPageRepository extends JpaRepository<ChapterPage, Long> {
    boolean existsByChapterChapterIdAndPageNumber(Long chapterId, Integer pageNumber);

    List<ChapterPage> findByChapterChapterIdAndPageStatusIgnoreCaseOrderByPageNumberAsc(
            Long chapterId, String pageStatus);
}

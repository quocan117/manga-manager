package com.example.backend.repository;

import com.example.backend.model.ChapterRevisionNote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChapterRevisionNoteRepository extends JpaRepository<ChapterRevisionNote, Long> {
    List<ChapterRevisionNote> findByChapterChapterIdOrderByOrderIndexAscCreatedAtAsc(Long chapterId);

    void deleteByChapterChapterId(Long chapterId);
}

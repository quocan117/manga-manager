package com.example.backend.dto;

import java.time.LocalDateTime;

public record ChapterRevisionNoteResponse(
        Long id,
        Long chapterId,
        String imageUrl,
        String description,
        Integer orderIndex,
        LocalDateTime createdAt) {
}

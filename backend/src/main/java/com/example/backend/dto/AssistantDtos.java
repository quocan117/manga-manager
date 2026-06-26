package com.example.backend.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;

public class AssistantDtos {
    private AssistantDtos() {
    }

    public record TaskResponse(
            Long id,
            Long seriesId,
            String seriesTitle,
            Long chapterId,
            Integer chapterNumber,
            String chapterTitle,
            Long pageId,
            Integer pageNumber,
            String pageImageUrl,
            Long assignedById,
            String assignedByName,
            String taskType,
            String title,
            String description,
            String status,
            LocalDateTime dueDate,
            Float areaX,
            Float areaY,
            Float areaWidth,
            Float areaHeight,
            LocalDateTime createdAt,
            SubmissionResponse latestSubmission) {
    }

    public record SubmitTaskRequest(
            String artifactUrl,
            String note,
            @NotNull Long expectedDrawingVersion) {
    }

    public record SubmissionResponse(
            Long id,
            Long taskId,
            Long chapterId,
            Long submittedById,
            String submittedByName,
            String artifactUrl,
            String note,
            String status,
            String reviewNote,
            LocalDateTime submittedAt,
            LocalDateTime reviewedAt) {
    }

}

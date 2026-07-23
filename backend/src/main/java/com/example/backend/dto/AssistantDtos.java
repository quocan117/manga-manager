package com.example.backend.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.example.backend.dto.MangakaDtos.UploadedFileResponse;

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
            String originalFileUrl,
            Long assignedById,
            String assignedByName,
            String taskType,
            String title,
            String description,
            String status,
            Integer roundNumber,
            LocalDateTime dueDate,
            Float areaX,
            Float areaY,
            Float areaWidth,
            Float areaHeight,
            LocalDateTime createdAt,
            SubmissionResponse latestSubmission,
            List<UploadedFileResponse> sourceFiles) {
    }

    public record SubmitTaskRequest(
            String artifactUrl,
            String note,
            Long expectedDrawingVersion) {
    }

    public record SubmissionResponse(
            Long id,
            Long taskId,
            Long chapterId,
            Long submittedById,
            String submittedByName,
            String artifactUrl,
            String originalFileUrl,
            String note,
            String status,
            Integer roundNumber,
            String reviewNote,
            LocalDateTime submittedAt,
            LocalDateTime reviewedAt,
            List<UploadedFileResponse> resultFiles) {
    }

    public record NotificationResponse(
            Long id,
            String type,
            Long referenceId,
            String message,
            Boolean isRead,
            LocalDateTime createdAt) {
    }

}

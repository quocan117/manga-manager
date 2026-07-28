package com.example.backend.dto;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

public final class MangakaDtos {
    private MangakaDtos() {
    }

    public record CreateSeriesRequest(
            @NotBlank String title,
            @NotEmpty List<@NotBlank String> genres,
            String coverUrl,
            String description,
            String publicationType,
            String artStyle) {
    }

    public record UploadedFileResponse(
            Long id,
            Long seriesId,
            String fileName,
            String originalFileName,
            String fileUrl,
            String contentType,
            Long fileSize,
            String fileType,
            boolean previewable,
            LocalDateTime uploadedAt) {
    }

    public record CreateChapterRequest(
            @NotNull Long seriesId,
            @NotNull @Positive Integer chapterNumber,
            @NotBlank String title) {
    }

    public record CreatePageRequest(
            @NotNull Long chapterId,
            @NotNull @Positive Integer pageNumber,
            @NotBlank String imageUrl) {
    }

    public record CreateAssistantRequest(
            @NotBlank String username,
            @NotBlank @Email String email,
            @NotBlank @Size(min = 8) String password,
            String avatarUrl) {
    }

    public record AssignTaskRequest(
            @NotNull Long pageId,
            @NotNull Long assistantId,
            @NotBlank String taskType,
            @NotBlank String title,
            String description,
            @NotNull @Future @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dueDate,
            @NotNull @PositiveOrZero Float areaX,
            @NotNull @PositiveOrZero Float areaY,
            @NotNull @Positive Float areaWidth,
            @NotNull @Positive Float areaHeight,
            List<MultipartFile> markupImages,
            String markupCanvasData,
            @NotEmpty List<MultipartFile> originalFiles) {
    }

    public record ReviseTaskRequest(
            List<MultipartFile> markupImages,
            String markupCanvasData,
            @NotEmpty List<MultipartFile> originalFiles) {
    }

    public record TaskMarkupPageResponse(
            Long id,
            Long taskId,
            Integer roundNumber,
            String imageUrl,
            String canvasData,
            Integer orderIndex,
            LocalDateTime createdAt) {
    }

    public record ReviewSubmissionRequest(
            @NotBlank String decision,
            String reviewNote) {
    }

    public record SeriesResponse(
            Long id,
            String title,
            List<String> genres,
            String coverUrl,
            String description,
            String status,
            LocalDateTime submittedAt,
            Float rankingScore,
            List<UploadedFileResponse> uploadedFiles) {
    }

    public record AssistantResponse(Long id, String username, String email, String status,
                                    LocalDateTime createdAt, String avatarUrl) {
    }

    public record ChapterResponse(
            Long id,
            Long seriesId,
            Integer chapterNumber,
            String title,
            List<UploadedFileResponse> manuscriptFiles,
            String status,
            LocalDateTime releaseDate,
            LocalDateTime createdAt) {
    }

    public record PageResponse(
            Long id,
            Long chapterId,
            Integer pageNumber,
            String imageUrl,
            String status) {
    }

    public record TaskResponse(
            Long id,
            Long chapterId,
            Long pageId,
            Integer pageNumber,
            Long assistantId,
            String assistantName,
            String taskType,
            String title,
            String description,
            String originalFileUrl,
            String status,
            Integer roundNumber,
            LocalDateTime dueDate,
            Float areaX,
            Float areaY,
            Float areaWidth,
            Float areaHeight,
            List<UploadedFileResponse> sourceFiles) {
    }

    public record AssistantParticipationResponse(
            Long id,
            String username,
            String email,
            String status,
            LocalDateTime createdAt,
            String avatarUrl,
            long totalTasks,
            long assignedTasks,
            long inProgressTasks,
            long submittedTasks,
            long approvedTasks,
            long revisionRequestedTasks) {
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

    public record RankingResponse(
            Long id,
            Long seriesId,
            String seriesTitle,
            Integer position,
            Float score,
            Integer voteCount,
            LocalDateTime periodStart,
            LocalDateTime periodEnd,
            LocalDateTime calculatedAt) {
    }

    public record FeedbackHistoryResponse(
            Long importId,
            LocalDateTime periodStart,
            LocalDateTime periodEnd,
            Integer voteCount) {
    }

    public record RankingSummaryResponse(
            Long seriesId,
            LocalDateTime periodStart,
            LocalDateTime periodEnd,
            Integer position,
            Float score,
            Integer voteCount,
            int totalSeriesInPeriod,
            long totalVotesInPeriod) {
    }

    public record NotificationResponse(
            Long id,
            String type,
            Long referenceId,
            String message,
            Boolean isRead,
            LocalDateTime createdAt) {
    }

    public record UpdateAssistantStatusRequest(
            @NotBlank String status) {
    }
}

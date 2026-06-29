package com.example.backend.dto;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public final class TantouEditorDtos {
    private TantouEditorDtos() {
    }

    public record SeriesSummaryResponse(
            Long id,
            String title,
            String author,
            List<String> genres,
            String coverUrl,
            String description,
            String status,
            LocalDateTime createdAt,
            LocalDateTime submittedAt) {
    }

    public record ManuscriptResponse(
            SeriesSummaryResponse series,
            List<ChapterManuscriptResponse> chapters,
            ProgressResponse progress,
            List<ScheduleResponse> schedules) {
    }

    public record ChapterManuscriptResponse(
            Long id,
            Integer chapterNumber,
            String title,
            String status,
            LocalDateTime releaseDate,
            List<PageManuscriptResponse> pages) {
    }

    public record PageManuscriptResponse(
            Long id,
            Integer pageNumber,
            String imageUrl,
            String status,
            List<CommentResponse> comments) {
    }

    public record CommentRequest(
            @NotBlank String commentText,
            @NotBlank String commentType,
            @PositiveOrZero Float positionX,
            @PositiveOrZero Float positionY,
            String status) {
    }

    public record CommentResponse(
            Long id,
            Long pageId,
            Long editorId,
            String editorName,
            String commentText,
            String commentType,
            String status,
            Float positionX,
            Float positionY,
            LocalDateTime createdAt) {
    }

    public record ScheduleRequest(
            @NotNull Long seriesId,
            @NotNull LocalDateTime publishDate,
            @NotBlank String frequency,
            String status) {
    }

    public record ScheduleResponse(
            Long id,
            Long seriesId,
            String seriesTitle,
            LocalDateTime publishDate,
            String frequency,
            String status) {
    }

    public record DossierResponse(
            SeriesSummaryResponse series,
            long approveVotes,
            long rejectVotes,
            List<BoardDecisionResponse> boardDecisions,
            ProgressResponse progress,
            List<ScheduleResponse> schedules,
            List<CommentResponse> comments) {
    }

    public record ReviewDecisionRequest(String note) {
    }

    public record BoardDecisionResponse(
            Long id,
            Long boardMemberId,
            String boardMemberName,
            String decisionType,
            String reason,
            LocalDateTime decisionDate) {
    }

    public record ProgressResponse(
            Long seriesId,
            String seriesTitle,
            long totalChapters,
            long totalPages,
            long finalizedPages,
            long totalTasks,
            long assignedTasks,
            long inProgressTasks,
            long submittedTasks,
            long approvedTasks,
            long revisionRequestedTasks,
            long overdueTasks,
            long openComments,
            LocalDateTime nextDeadline,
            double completionRate) {
    }
}

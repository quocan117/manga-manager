package com.example.backend.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.example.backend.dto.MangakaDtos.UploadedFileResponse;
import com.example.backend.dto.MangakaDtos.PageHistoryResponse;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.web.multipart.MultipartFile;

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
            LocalDateTime submittedAt,
            List<UploadedFileResponse> uploadedFiles) {
    }

    public record ChapterRevisionNoteRequest(
            @NotNull MultipartFile image,
            String canvasData,
            @NotBlank String description,
            @NotNull @PositiveOrZero Integer orderIndex) {
    }

    public record ManuscriptResponse(
            SeriesSummaryResponse series,
            List<ChapterManuscriptResponse> chapters,
            ProgressResponse progress) {
    }

    public record ChapterManuscriptResponse(
            Long id,
            Integer chapterNumber,
            String title,
            Long seriesId,
            String seriesTitle,
            List<UploadedFileResponse> manuscriptFiles,
            String status,
            LocalDateTime releaseDate,
            List<PageManuscriptResponse> pages) {
    }

    public record PageManuscriptResponse(
            Long id,
            Integer pageNumber,
            String imageUrl,
            String status,
            List<CommentResponse> comments,
            List<PageHistoryResponse> history) {
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

    public record DossierResponse(
            SeriesSummaryResponse series,
            long approveVotes,
            long rejectVotes,
            List<BoardDecisionResponse> boardDecisions,
            ProgressResponse progress,
            List<CommentResponse> comments) {
    }

    public record ReviewDecisionRequest(String note) {
    }

    public record RejectSeriesRequest(
            @NotBlank(message = "Vui lòng nhập lý do từ chối") String reason) {
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
            String status,
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

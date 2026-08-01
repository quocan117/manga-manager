package com.example.backend.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.example.backend.dto.MangakaDtos.UploadedFileResponse;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public final class EditorialBoardDtos {
        private EditorialBoardDtos() {
        }

        public record CreateUserRequest(
                        @NotBlank String username,
                        @NotBlank @Email String email,
                        @NotBlank @Size(min = 8) String password,
                        @NotBlank String role,
                        String avatarUrl,
                        String status) {
        }

        public record UpdateUserRequest(
                        String username,
                        @Email String email,
                        @Size(min = 8) String password,
                        String role,
                        String avatarUrl,
                        String status) {
        }

        public record UserResponse(
                        Long id,
                        String username,
                        String email,
                        String avatarUrl,
                        String status,
                        String role,
                        Long createdById,
                        String createdByName,
                        LocalDateTime createdAt) {
        }

        public record BoardDecisionRequest(
                        @NotBlank String decisionType,
                        String reason) {
        }

        public record AssignEditorRequest(
                        @NotNull Long editorId) {
        }

        public record BoardMemberAssignmentResponse(
                        Long boardMemberId,
                        String boardMemberName,
                        String boardMemberEmail,
                        LocalDateTime assignedAt) {
        }

        public record BoardDecisionResponse(
                        Long id,
                        Long seriesId,
                        Long boardMemberId,
                        String boardMemberName,
                        String decisionType,
                        String reason,
                        LocalDateTime decisionDate) {
        }

        public record SeriesReviewHistoryResponse(
                        Long id,
                        Long seriesId,
                        Long actorId,
                        String actorName,
                        String actorRole,
                        String action,
                        String previousStatus,
                        String newStatus,
                        String reason,
                        Long referenceId,
                        LocalDateTime createdAt) {
        }

        public record ScheduleRequest(
                        @NotNull Long seriesId,
                        @NotNull @Future(message = "Publish date must be in the future") LocalDateTime publishDate,
                        @NotBlank @Pattern(
                                        regexp = "(?i)DAILY|WEEKLY|MONTHLY",
                                        message = "Frequency must be DAILY, WEEKLY, or MONTHLY") String frequency,
                        String status) {
        }

        public record ScheduleResponse(
                        Long id,
                        Long seriesId,
                        String seriesTitle,
                        LocalDateTime publishDate,
                        String frequency,
                        String status,
                        boolean isOverdue,
                        boolean isPublicationCoordinator) {
        }

        public record BoardChapterResponse(
                        Long id,
                        Integer chapterNumber,
                        String title,
                        Long seriesId,
                        String seriesTitle,
                        List<UploadedFileResponse> manuscriptFiles,
                        String status,
                        LocalDateTime releaseDate) {
        }

        public record ApprovedSeriesManagementResponse(
                        Long id,
                        String title,
                        String status,
                        Long publicationCoordinatorId,
                        String publicationCoordinatorName,
                        List<BoardMemberAssignmentResponse> boardPanel,
                        ScheduleResponse publishSchedule,
                        long chapterCount,
                        long publishedChapterCount,
                        double progress,
                        boolean isPublicationCoordinator,
                        List<UploadedFileResponse> uploadedFiles) {
        }

        public record RejectedEditorResponse(
                        Long editorId,
                        String name,
                        String email,
                        long currentTaskCount,
                        String reason,
                        LocalDateTime rejectedAt) {
        }

        public record AssignedSeriesResponse(
                        Long id,
                        String title,
                        String coverUrl) {
        }

        public record ReviewSeriesResponse(
                        Long id,
                        String title,
                        String author,
                        List<String> genres,
                        String coverUrl,
                        String description,
                        String status,
                        LocalDateTime submittedAt,
                        long approveVotes,
                        long rejectVotes,
                        long totalBoardMembers,
                        long requiredVotes,
                        String currentUserDecision,
                        boolean currentUserAssigned,
                        List<BoardDecisionResponse> decisions,
                        Long tantouEditorId,
                        String tantouEditorName,
                        String tantouEditorEmail,
                        Boolean editorAssignmentLocked,
                        long editorRejectCount,
                        long totalActiveEditors,
                        String publicationType,
                        String artStyle,
                        LocalDateTime createdAt,
                        long totalEligibleBoardMembers,
                        List<BoardMemberAssignmentResponse> assignedBoardMembers,
                        List<UploadedFileResponse> uploadedFiles,
                        List<RejectedEditorResponse> rejectedEditors,
                        List<SeriesReviewHistoryResponse> reviewHistory) {
        }

        public record ReaderVoteResponse(
                        Long likeId,
                        Long seriesId,
                        String seriesTitle,
                        Integer chapterNumber,
                        String chapterTitle,
                        String guestSessionToken,
                        LocalDateTime likedAt) {
        }

        public record SeriesVoteSummaryResponse(
                        Long seriesId,
                        String seriesTitle,
                        long voteCount) {
        }

        public record ImportReaderFeedbackRequest(
                        @NotNull Long seriesId,
                        @NotNull LocalDateTime periodStart,
                        @NotNull LocalDateTime periodEnd) {
        }

        public record ReaderFeedbackImportResponse(
                        Long id,
                        Long seriesId,
                        String seriesTitle,
                        LocalDateTime periodStart,
                        LocalDateTime periodEnd,
                        Integer voteCount,
                        LocalDateTime importedAt) {
        }

        public record SeriesFeedbackImportResponse(
                        Long importId,
                        LocalDateTime periodStart,
                        LocalDateTime periodEnd,
                        Integer voteCount) {
        }

        public record RankingPeriodResponse(
                        LocalDateTime periodStart,
                        LocalDateTime periodEnd) {
        }

        public record SeriesTotalVotesResponse(
                Long seriesId,
                String seriesTitle,
                long totalVotes) {
        }
}

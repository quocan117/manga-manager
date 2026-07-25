package com.example.backend.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.example.backend.dto.MangakaDtos.UploadedFileResponse;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
                        String status,
                        boolean isOverdue,
                        boolean isPublicationCoordinator) {
        }

        public record ChapterBoardReviewRequest(
                        @NotNull Boolean confirmed,
                        String comment) {
        }

        public record ChapterBoardReviewResponse(
                        Long id,
                        Long chapterId,
                        Long boardMemberId,
                        String boardMemberName,
                        Boolean confirmed,
                        String comment,
                        LocalDateTime reviewedAt) {
        }

        public record BoardChapterResponse(
                        Long id,
                        Integer chapterNumber,
                        String title,
                        Long seriesId,
                        String seriesTitle,
                        List<UploadedFileResponse> manuscriptFiles,
                        String status,
                        LocalDateTime releaseDate,
                        List<ChapterBoardReviewResponse> reviews) {
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
                        boolean isPublicationCoordinator) {
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
                        List<UploadedFileResponse> uploadedFiles) {
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
                        @NotBlank String period,
                        @NotNull LocalDateTime periodStart,
                        @NotNull LocalDateTime periodEnd) {
        }

        public record ReaderFeedbackImportResponse(
                        Long id,
                        Long seriesId,
                        String seriesTitle,
                        String period,
                        Integer voteCount,
                        LocalDateTime importedAt) {
        }

        public record SeriesTotalVotesResponse(
                Long seriesId,
                String seriesTitle,
                long totalVotes) {
        }
}

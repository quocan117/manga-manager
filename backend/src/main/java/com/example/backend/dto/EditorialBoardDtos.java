package com.example.backend.dto;

import java.time.LocalDateTime;
import java.util.List;

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

        public record BoardDecisionResponse(
                        Long id,
                        Long seriesId,
                        Long boardMemberId,
                        String boardMemberName,
                        String decisionType,
                        String reason,
                        LocalDateTime decisionDate) {
        }

        public record ReviewSeriesResponse(
                        Long id,
                        String title,
                        String author,
                        List<String> genres,
                        String coverUrl,
                        String description,
                        String status,
                        String storyboardUrl,
                        LocalDateTime submittedAt,
                        long approveVotes,
                        long rejectVotes,
                        long totalBoardMembers,
                        long requiredVotes,
                        String currentUserDecision,
                        List<BoardDecisionResponse> decisions) {
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
}

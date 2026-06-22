package com.example.backend.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.databind.JsonNode;

import jakarta.validation.constraints.NotNull;

public final class DrawingDtos {
        private DrawingDtos() {
        }

        public record SaveDrawingRequest(
                        @NotNull JsonNode canvasData,
                        String previewImageUrl,
                        Long sourceSubmissionId,
                        Long expectedVersion) {
        }

        public record VersionRequest(@NotNull Long expectedVersion) {
        }

        public record DrawingResponse(
                        Long id,
                        Long pageId,
                        Long taskId,
                        Long ownerId,
                        Long sourceSubmissionId,
                        JsonNode canvasData,
                        String previewImageUrl,
                        String status,
                        Long version,
                        LocalDateTime createdAt,
                        LocalDateTime updatedAt) {
        }

        public record RevisionResponse(
                        Long id,
                        Long version,
                        Long savedById,
                        JsonNode canvasData,
                        String previewImageUrl,
                        String status,
                        LocalDateTime createdAt) {
        }
}

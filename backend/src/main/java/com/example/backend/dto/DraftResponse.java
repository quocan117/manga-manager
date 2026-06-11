package com.example.backend.dto;

import com.example.backend.model.Draft;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class DraftResponse {

    private Long id;
    private String fileName;
    private String contentType;
    private long fileSize;
    private LocalDateTime uploadedAt;

    public static DraftResponse from(Draft draft) {
        return DraftResponse.builder()
                .id(draft.getId())
                .fileName(draft.getFileName())
                .contentType(draft.getContentType())
                .fileSize(draft.getFileSize())
                .uploadedAt(draft.getUploadedAt())
                .build();
    }
}

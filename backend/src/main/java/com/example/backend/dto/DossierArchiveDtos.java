package com.example.backend.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.example.backend.dto.MangakaDtos.UploadedFileResponse;

public final class DossierArchiveDtos {
    private DossierArchiveDtos() {
    }

    public record DossierRoundResponse(
            Integer roundNumber,
            LocalDateTime submittedAt,
            List<UploadedFileResponse> submittedFiles,
            String reviewedBy,
            String decision,
            String reviewNote,
            LocalDateTime reviewedAt) {
    }
}

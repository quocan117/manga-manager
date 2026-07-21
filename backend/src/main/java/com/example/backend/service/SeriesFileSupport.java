package com.example.backend.service;

import com.example.backend.model.SeriesFile;

import java.util.Locale;
import java.util.Set;

final class SeriesFileSupport {
    private static final Set<String> PREVIEWABLE_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp",
            "image/gif",
            "application/pdf");

    private SeriesFileSupport() {
    }

    static boolean isPreviewable(SeriesFile file) {
        if (file == null || file.getContentType() == null) {
            return false;
        }
        return PREVIEWABLE_CONTENT_TYPES.contains(file.getContentType().toLowerCase(Locale.ROOT));
    }

    static String downloadUrl(SeriesFile file) {
        return file.getFileId() == null ? null : "/series-files/" + file.getFileId() + "/download";
    }
}

package com.example.backend.service;

import com.example.backend.model.MangaSeries;
import com.example.backend.model.SeriesFile;
import com.example.backend.model.User;
import com.example.backend.repository.SeriesBoardAssignmentRepository;
import com.example.backend.repository.SeriesFileRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.net.MalformedURLException;
import java.nio.file.Path;

@Service
public class SeriesFileService {
    private static final String PUBLIC_FILE_PREFIX = "series-files/";

    private final SeriesFileRepository seriesFileRepository;
    private final SeriesBoardAssignmentRepository seriesBoardAssignmentRepository;

    @Value("${manga.upload.series-file-root:}")
    private String seriesFileUploadRootOverride;

    public SeriesFileService(
            SeriesFileRepository seriesFileRepository,
            SeriesBoardAssignmentRepository seriesBoardAssignmentRepository) {
        this.seriesFileRepository = seriesFileRepository;
        this.seriesBoardAssignmentRepository = seriesBoardAssignmentRepository;
    }

    public DownloadableSeriesFile getAuthorizedDownload(Long fileId) {
        SeriesFile file = seriesFileRepository.findById(fileId)
                .orElseThrow(() -> notFound("Series file not found"));
        MangaSeries series = file.getSeries();
        if (series == null || series.getSeriesId() == null) {
            throw notFound("Series file is not linked to a series");
        }

        String email = currentEmail();
        if (!canAccess(series, email)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You do not have permission to download this series file");
        }

        Path target = resolveStoredFile(file);
        Resource resource;
        try {
            resource = new UrlResource(target.toUri());
        } catch (MalformedURLException exception) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Stored series file path is invalid",
                    exception);
        }
        if (!resource.exists() || !resource.isReadable()) {
            throw notFound("Stored series file was not found on disk");
        }

        return new DownloadableSeriesFile(
                resource,
                safeDownloadName(file),
                file.getContentType(),
                file.getFileSize());
    }

    private boolean canAccess(MangaSeries series, String email) {
        User author = series.getAuthor();
        if (author != null && email.equalsIgnoreCase(author.getEmail())) {
            return true;
        }
        User editor = series.getTantouEditor();
        if (editor != null && email.equalsIgnoreCase(editor.getEmail())) {
            return true;
        }
        return seriesBoardAssignmentRepository.existsBySeriesSeriesIdAndBoardMemberEmailIgnoreCase(
                series.getSeriesId(), email);
    }

    private Path resolveStoredFile(SeriesFile file) {
        String fileUrl = file.getFileUrl();
        if (fileUrl == null || !fileUrl.replace('\\', '/').startsWith(PUBLIC_FILE_PREFIX)) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Stored series file URL is invalid");
        }

        String relativeValue = fileUrl.replace('\\', '/').substring(PUBLIC_FILE_PREFIX.length());
        Path uploadRoot = seriesFileUploadRoot();
        Path target = uploadRoot.resolve(relativeValue).toAbsolutePath().normalize();
        if (!target.startsWith(uploadRoot)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid series file path");
        }
        return target;
    }

    private String safeDownloadName(SeriesFile file) {
        String originalName = file.getOriginalFileName();
        String fallbackName = file.getFileName() == null ? "series-file" : file.getFileName();
        if (originalName == null || originalName.isBlank()) {
            return fallbackName;
        }
        String normalized = originalName.replace('\\', '/');
        int slashIndex = normalized.lastIndexOf('/');
        String name = slashIndex >= 0 ? normalized.substring(slashIndex + 1) : normalized;
        name = name.replace("\r", "").replace("\n", "").trim();
        return name.isBlank() ? fallbackName : name;
    }

    private Path seriesFileUploadRoot() {
        if (seriesFileUploadRootOverride != null && !seriesFileUploadRootOverride.isBlank()) {
            return Path.of(seriesFileUploadRootOverride).toAbsolutePath().normalize();
        }
        return Path.of("uploads/series-files").toAbsolutePath().normalize();
    }

    private String currentEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        return authentication.getName();
    }

    private ResponseStatusException notFound(String message) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, message);
    }

    public record DownloadableSeriesFile(
            Resource resource,
            String originalFileName,
            String contentType,
            Long fileSize) {
    }
}

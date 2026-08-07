package com.example.backend.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.backend.dto.DossierArchiveDtos.DossierRoundResponse;
import com.example.backend.dto.MangakaDtos.UploadedFileResponse;
import com.example.backend.model.MangaSeries;
import com.example.backend.model.SeriesEditorRejection;
import com.example.backend.model.SeriesFile;
import com.example.backend.model.SeriesReviewHistory;
import com.example.backend.model.User;
import com.example.backend.repository.MangaSeriesRepository;
import com.example.backend.repository.SeriesBoardAssignmentRepository;
import com.example.backend.repository.SeriesEditorRejectionRepository;
import com.example.backend.repository.SeriesFileRepository;
import com.example.backend.repository.SeriesReviewHistoryRepository;
import com.example.backend.repository.UserRepository;

@Service
public class DossierArchiveService {
    private final MangaSeriesRepository mangaSeriesRepository;
    private final SeriesFileRepository seriesFileRepository;
    private final SeriesReviewHistoryRepository seriesReviewHistoryRepository;
    private final SeriesEditorRejectionRepository seriesEditorRejectionRepository;
    private final SeriesBoardAssignmentRepository seriesBoardAssignmentRepository;
    private final UserRepository userRepository;

    public DossierArchiveService(
            MangaSeriesRepository mangaSeriesRepository,
            SeriesFileRepository seriesFileRepository,
            SeriesReviewHistoryRepository seriesReviewHistoryRepository,
            SeriesEditorRejectionRepository seriesEditorRejectionRepository,
            SeriesBoardAssignmentRepository seriesBoardAssignmentRepository,
            UserRepository userRepository) {
        this.mangaSeriesRepository = mangaSeriesRepository;
        this.seriesFileRepository = seriesFileRepository;
        this.seriesReviewHistoryRepository = seriesReviewHistoryRepository;
        this.seriesEditorRejectionRepository = seriesEditorRejectionRepository;
        this.seriesBoardAssignmentRepository = seriesBoardAssignmentRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<DossierRoundResponse> getArchiveHistory(Long seriesId) {
        MangaSeries series = mangaSeriesRepository.findById(seriesId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Series not found"));
        User currentUser = currentUser();
        requireArchiveAccess(series, currentUser);

        Map<Integer, List<SeriesFile>> filesByRound = seriesFileRepository
                .findBySeriesSeriesIdOrderByRoundNumberAscUploadedAtAsc(seriesId)
                .stream()
                .collect(Collectors.groupingBy(
                        file -> file.getRoundNumber() == null ? 1 : file.getRoundNumber(),
                        TreeMap::new,
                        Collectors.toList()));
        if (filesByRound.isEmpty()) {
            return List.of();
        }

        List<SeriesReviewHistory> history = seriesReviewHistoryRepository
                .findBySeriesSeriesIdOrderByCreatedAtAsc(seriesId);
        List<SeriesEditorRejection> rejections = seriesEditorRejectionRepository
                .findBySeriesSeriesId(seriesId);
        List<Integer> rounds = new ArrayList<>(filesByRound.keySet());
        List<DossierRoundResponse> response = new ArrayList<>();
        for (int index = 0; index < rounds.size(); index++) {
            Integer roundNumber = rounds.get(index);
            List<SeriesFile> roundFiles = filesByRound.get(roundNumber);
            LocalDateTime submittedAt = firstUploadAt(roundFiles);
            LocalDateTime nextRoundAt = index + 1 < rounds.size()
                    ? firstUploadAt(filesByRound.get(rounds.get(index + 1)))
                    : null;
            ReviewEvent review = latestReviewEvent(history, rejections, submittedAt, nextRoundAt);
            response.add(new DossierRoundResponse(
                    roundNumber,
                    submittedAt,
                    roundFiles.stream().map(this::toUploadedFileResponse).toList(),
                    review == null ? null : review.reviewedBy(),
                    review == null ? null : review.decision(),
                    review == null ? null : review.note(),
                    review == null ? null : review.reviewedAt()));
        }
        return response;
    }

    @Transactional(readOnly = true)
    public Map<String, List<Map<String, Object>>> getEditorWorkflowHistory(Long seriesId) {
        List<SeriesReviewHistory> history = seriesReviewHistoryRepository
                .findBySeriesSeriesIdOrderByCreatedAtAsc(seriesId);
        List<SeriesFile> allFiles = seriesFileRepository.findBySeriesSeriesId(seriesId);
        List<Map<String, Object>> submitToBoardHistory = new ArrayList<>();
        List<Map<String, Object>> requestRevisionHistory = new ArrayList<>();
        for (SeriesReviewHistory item : history) {
            String action = item.getAction() != null ? item.getAction().toUpperCase() : "";
            if (action.contains("SUBMITTED_TO_BOARD")) {
                List<SeriesFile> editorFiles = allFiles.stream()
                        .filter(f -> f.getUploadedAt() != null && !f.getUploadedAt().isAfter(item.getCreatedAt()))
                        .filter(f -> "EDITOR_DOSSIER".equalsIgnoreCase(f.getPurpose())) // <--- QUAN TRỌNG
                        .toList();
                java.time.LocalDateTime latestUploadTime = editorFiles.stream()
                        .map(SeriesFile::getUploadedAt)
                        .max(java.time.LocalDateTime::compareTo)
                        .orElse(null);
                List<Map<String, Object>> relatedFiles = new ArrayList<>();
                if (latestUploadTime != null) {
                    relatedFiles = editorFiles.stream()
                            .filter(f -> f.getUploadedAt().isAfter(latestUploadTime.minusMinutes(5)))
                            .map(f -> {
                                Map<String, Object> fMap = new HashMap<>();
                                fMap.put("id", f.getFileId());
                                fMap.put("originalFileName", f.getOriginalFileName());
                                fMap.put("fileUrl", f.getFileUrl());
                                fMap.put("fileSize", f.getFileSize());
                                fMap.put("contentType", f.getContentType());
                                return fMap;
                            })
                            .toList();
                }
                Map<String, Object> map = new HashMap<>();
                map.put("createdAt", item.getCreatedAt());
                map.put("note", item.getReason());
                map.put("files", relatedFiles);
                submitToBoardHistory.add(map);
            } else if (action.contains("REVISION") || action.contains("REJECT")) {
                List<SeriesFile> mangakaFiles = allFiles.stream()
                        .filter(f -> f.getUploadedAt() != null && !f.getUploadedAt().isAfter(item.getCreatedAt()))
                        .filter(f -> "SERIES_SUBMISSION".equalsIgnoreCase(f.getPurpose())) // <--- QUAN TRỌNG
                        .toList();
                java.time.LocalDateTime latestUploadTime = mangakaFiles.stream()
                        .map(SeriesFile::getUploadedAt)
                        .max(java.time.LocalDateTime::compareTo)
                        .orElse(null);
                List<Map<String, Object>> relatedFiles = new ArrayList<>();
                if (latestUploadTime != null) {
                    relatedFiles = mangakaFiles.stream()
                            .filter(f -> f.getUploadedAt().isAfter(latestUploadTime.minusMinutes(5)))
                            .map(f -> {
                                Map<String, Object> fMap = new HashMap<>();
                                fMap.put("id", f.getFileId());
                                fMap.put("originalFileName", f.getOriginalFileName());
                                fMap.put("fileUrl", f.getFileUrl());
                                fMap.put("fileSize", f.getFileSize());
                                fMap.put("contentType", f.getContentType());
                                return fMap;
                            })
                            .toList();
                }
                Map<String, Object> map = new HashMap<>();
                map.put("createdAt", item.getCreatedAt());
                map.put("note", item.getReason());
                map.put("files", relatedFiles);
                requestRevisionHistory.add(map);
            }
        }
        Map<String, List<Map<String, Object>>> result = new HashMap<>();
        result.put("submitToBoard", submitToBoardHistory);
        result.put("requestRevision", requestRevisionHistory);
        return result;
    }

    private ReviewEvent latestReviewEvent(
            List<SeriesReviewHistory> history,
            List<SeriesEditorRejection> rejections,
            LocalDateTime submittedAt,
            LocalDateTime nextRoundAt) {
        List<ReviewEvent> events = new ArrayList<>();
        history.stream()
                .filter(item -> isWithinRound(item.getCreatedAt(), submittedAt, nextRoundAt))
                .filter(item -> isReviewAction(item.getAction()))
                .map(item -> new ReviewEvent(
                        item.getActor() == null ? null : item.getActor().getUsername(),
                        item.getAction(),
                        item.getReason(),
                        item.getCreatedAt()))
                .forEach(events::add);
        rejections.stream()
                .filter(item -> isWithinRound(item.getRejectedAt(), submittedAt, nextRoundAt))
                .map(item -> new ReviewEvent(
                        item.getEditor() == null ? null : item.getEditor().getUsername(),
                        "EDITOR_REJECTED_SERIES",
                        item.getReason(),
                        item.getRejectedAt()))
                .forEach(events::add);
        return events.stream()
                .filter(event -> event.reviewedAt() != null)
                .max(Comparator.comparing(ReviewEvent::reviewedAt))
                .orElse(null);
    }

    private boolean isWithinRound(
            LocalDateTime value,
            LocalDateTime submittedAt,
            LocalDateTime nextRoundAt) {
        return value != null
                && (submittedAt == null || !value.isBefore(submittedAt))
                && (nextRoundAt == null || value.isBefore(nextRoundAt));
    }

    private boolean isReviewAction(String action) {
        if (action == null) {
            return false;
        }
        String normalized = action.toUpperCase();
        return normalized.contains("REJECT")
                || normalized.contains("REVISION")
                || normalized.contains("APPROVE")
                || normalized.contains("VOTE");
    }

    private LocalDateTime firstUploadAt(List<SeriesFile> files) {
        return files.stream()
                .map(SeriesFile::getUploadedAt)
                .filter(Objects::nonNull)
                .min(Comparator.naturalOrder())
                .orElse(null);
    }

    private UploadedFileResponse toUploadedFileResponse(SeriesFile file) {
        MangaSeries series = file.getSeries();
        return new UploadedFileResponse(
                file.getFileId(),
                series == null ? null : series.getSeriesId(),
                file.getFileName(),
                file.getOriginalFileName(),
                SeriesFileSupport.downloadUrl(file),
                file.getContentType(),
                file.getFileSize(),
                file.getFileType(),
                SeriesFileSupport.isPreviewable(file),
                file.getActive(),
                file.getRoundNumber(),
                file.getPurpose(),
                file.getUploadedAt());
    }

    private void requireArchiveAccess(MangaSeries series, User user) {
        String role = user.getRole() == null ? null : user.getRole().getRoleName();
        boolean allowed = "MANGAKA".equalsIgnoreCase(role)
                && series.getAuthor() != null
                && Objects.equals(series.getAuthor().getUserId(), user.getUserId());
        allowed = allowed || ("TANTOU_EDITOR".equalsIgnoreCase(role)
                && series.getTantouEditor() != null
                && Objects.equals(series.getTantouEditor().getUserId(), user.getUserId()));
        allowed = allowed || ("EDITORIAL_BOARD".equalsIgnoreCase(role)
                && seriesBoardAssignmentRepository
                .existsBySeriesSeriesIdAndBoardMemberUserId(series.getSeriesId(), user.getUserId()));
        if (!allowed) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You are not allowed to view this dossier archive");
        }
    }

    private User currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Authenticated user not found"));
    }

    private record ReviewEvent(
            String reviewedBy,
            String decision,
            String note,
            LocalDateTime reviewedAt) {
    }
}

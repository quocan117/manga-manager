package com.example.backend.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.backend.dto.DrawingDtos.DrawingResponse;
import com.example.backend.dto.DrawingDtos.RevisionResponse;
import com.example.backend.dto.DrawingDtos.SaveDrawingRequest;
import com.example.backend.dto.DrawingDtos.VersionRequest;
import com.example.backend.model.ChapterPage;
import com.example.backend.model.PageDrawing;
import com.example.backend.model.PageDrawingRevision;
import com.example.backend.model.Submission;
import com.example.backend.model.User;
import com.example.backend.repository.ChapterPageRepository;
import com.example.backend.repository.PageDrawingRepository;
import com.example.backend.repository.PageDrawingRevisionRepository;
import com.example.backend.repository.SubmissionRepository;
import com.example.backend.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class DrawingService {
    private final ChapterPageRepository pageRepository;
    private final PageDrawingRepository drawingRepository;
    private final PageDrawingRevisionRepository revisionRepository;
    private final SubmissionRepository submissionRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public DrawingService(
            ChapterPageRepository pageRepository,
            PageDrawingRepository drawingRepository,
            PageDrawingRevisionRepository revisionRepository,
            SubmissionRepository submissionRepository,
            UserRepository userRepository,
            ObjectMapper objectMapper) {
        this.pageRepository = pageRepository;
        this.drawingRepository = drawingRepository;
        this.revisionRepository = revisionRepository;
        this.submissionRepository = submissionRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public DrawingResponse getDrawing(Long pageId) {
        ownedPage(pageId);
        PageDrawing drawing = drawingRepository.findByPagePageIdAndTaskIsNull(pageId)
                .orElseThrow(() -> notFound("Drawing not found"));
        return toResponse(drawing);
    }

    @Transactional
    public DrawingResponse saveDrawing(Long pageId, SaveDrawingRequest request) {
        ChapterPage page = ownedPage(pageId);
        User mangaka = currentUser();
        PageDrawing drawing = drawingRepository.findByPagePageIdAndTaskIsNull(pageId).orElse(null);

        if (drawing == null) {
            if (request.expectedVersion() != null && request.expectedVersion() != 0L) {
                throw conflict("Drawing version is oudated");
            }
            drawing = new PageDrawing();
            drawing.setPage(page);
            drawing.setCreatedAt(LocalDateTime.now());
            drawing.setStatus("DRAFT");
        } else {
            assertVersion(drawing, request.expectedVersion());
            if ("FINALIZED".equals(drawing.getStatus())) {
                drawing.setStatus("DRAFT");
                page.setPageStatus("DRAFT");
                pageRepository.save(page);
            }
        }

        if (request.sourceSubmissionId() != null) {
            drawing.setSourceSubmission(ownedSourceSubmission(page, request.sourceSubmissionId()));
        }
        drawing.setOwner(mangaka);
        drawing.setCanvasData(request.canvasData().toString());
        drawing.setPreviewImageUrl(request.previewImageUrl());
        drawing.setUpdatedAt(LocalDateTime.now());

        return saveWithRevision(drawing, mangaka);
    }

    @Transactional
    public DrawingResponse finalizeDrawing(Long pageId, VersionRequest request) {
        ChapterPage page = ownedPage(pageId);
        User mangaka = currentUser();
        PageDrawing drawing = drawingRepository.findByPagePageIdAndTaskIsNull(pageId)
                .orElseGet(() -> {
                    PageDrawing newDrawing = new PageDrawing();
                    newDrawing.setPage(page);
                    newDrawing.setOwner(mangaka);
                    newDrawing.setCanvasData("{}"); // Canvas trống
                    newDrawing.setCreatedAt(LocalDateTime.now());
                    return newDrawing;
                });
        if (drawing.getDrawingId() != null) {
            assertVersion(drawing, request.expectedVersion());
        }
        drawing.setStatus("FINALIZED");
        drawing.setUpdatedAt(LocalDateTime.now());
        page.setPageStatus("DRAWING_FINALIZED");
        pageRepository.save(page);
        return saveWithRevision(drawing, mangaka);
    }

    @Transactional(readOnly = true)
    public List<RevisionResponse> getRevisions(Long pageId) {
        ownedPage(pageId);
        PageDrawing drawing = drawingRepository.findByPagePageIdAndTaskIsNull(pageId)
                .orElseThrow(() -> notFound("Drawing not found"));
        return revisionRepository.findByDrawingDrawingIdOrderByVersionNumberDesc(drawing.getDrawingId())
                .stream()
                .map(this::toRevisionResponse)
                .toList();
    }

    @Transactional
    public DrawingResponse restoreRevision(
            Long pageId,
            Long revisionId,
            VersionRequest request) {
        ownedPage(pageId);
        User mangaka = currentUser();
        PageDrawing drawing = drawingRepository.findByPagePageIdAndTaskIsNull(pageId)
                .orElseThrow(() -> notFound("Drawing not found"));
        assertVersion(drawing, request.expectedVersion());

        PageDrawingRevision revision = revisionRepository
                .findByRevisionIdAndDrawingDrawingId(revisionId, drawing.getDrawingId())
                .orElseThrow(() -> notFound("Drawing revision not found"));
        drawing.setCanvasData(revision.getCanvasData());
        drawing.setPreviewImageUrl(revision.getPreviewImageUrl());
        drawing.setStatus("DRAFT");
        drawing.setOwner(mangaka);
        drawing.setUpdatedAt(LocalDateTime.now());

        return saveWithRevision(drawing, mangaka);
    }

    private DrawingResponse saveWithRevision(PageDrawing drawing, User savedBy) {
        try {
            PageDrawing saved = drawingRepository.saveAndFlush(drawing);
            PageDrawingRevision revision = new PageDrawingRevision();
            revision.setDrawing(saved);
            revision.setSavedBy(savedBy);
            revision.setVersionNumber(saved.getVersion());
            revision.setCanvasData(saved.getCanvasData());
            revision.setPreviewImageUrl(saved.getPreviewImageUrl());
            revision.setStatus(saved.getStatus());
            revision.setCreatedAt(LocalDateTime.now());
            revisionRepository.save(revision);
            return toResponse(saved);
        } catch (OptimisticLockingFailureException exception) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Drawing was changed by another session",
                    exception);
        }
    }

    private Submission ownedSourceSubmission(ChapterPage page, Long submissionId) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> notFound("Submission not found"));
        if (submission.getChapter() == null
                || !submission.getChapter().getChapterId().equals(page.getChapter().getChapterId())) {
            throw badRequest("Submission does not belong to this chapter");
        }
        if (submission.getTask() != null && submission.getTask().getPage() != null
                && !submission.getTask().getPage().getPageId().equals(page.getPageId())) {
            throw badRequest("Submission does not belong to this page");
        }
        return submission;
    }

    private ChapterPage ownedPage(Long pageId) {
        ChapterPage page = pageRepository.findById(pageId)
                .orElseThrow(() -> notFound("Chapter page not found"));
        if (page.getChapter() == null
                || page.getChapter().getSeries() == null
                || page.getChapter().getSeries().getAuthor() == null
                || !currentEmail().equals(page.getChapter().getSeries().getAuthor().getEmail())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "You do not own this chapter page");
        }
        return page;
    }

    private User currentUser() {
        return userRepository.findByEmail(currentEmail())
                .orElseThrow(() -> notFound("Authenticated user not found"));
    }

    private String currentEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        return authentication.getName();
    }

    private void assertVersion(PageDrawing drawing, Long expectedVersion) {
        if (expectedVersion == null || !expectedVersion.equals(drawing.getVersion())) {
            throw conflict("Drawing version is stale");
        }
    }

    private DrawingResponse toResponse(PageDrawing drawing) {
        return new DrawingResponse(
                drawing.getDrawingId(),
                drawing.getPage().getPageId(),
                drawing.getTask() == null ? null : drawing.getTask().getTaskId(),
                drawing.getOwner().getUserId(),
                drawing.getSourceSubmission() == null
                        ? null
                        : drawing.getSourceSubmission().getSubmissionId(),
                parseCanvasData(drawing.getCanvasData()),
                drawing.getPreviewImageUrl(),
                drawing.getStatus(),
                drawing.getVersion(),
                drawing.getCreatedAt(),
                drawing.getUpdatedAt());
    }

    private RevisionResponse toRevisionResponse(PageDrawingRevision revision) {
        return new RevisionResponse(
                revision.getRevisionId(),
                revision.getVersionNumber(),
                revision.getSavedBy().getUserId(),
                parseCanvasData(revision.getCanvasData()),
                revision.getPreviewImageUrl(),
                revision.getStatus(),
                revision.getCreatedAt());
    }

    private JsonNode parseCanvasData(String canvasData) {
        try {
            return objectMapper.readTree(canvasData);
        } catch (JsonProcessingException exception) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Stored canvas data is invalid",
                    exception);
        }
    }

    private ResponseStatusException notFound(String message) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, message);
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    private ResponseStatusException conflict(String message) {
        return new ResponseStatusException(HttpStatus.CONFLICT, message);
    }
}

package com.example.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.backend.dto.ChapterRevisionNoteResponse;
import com.example.backend.dto.MangakaDtos.NotificationResponse;
import com.example.backend.dto.TantouEditorDtos.ChapterManuscriptResponse;
import com.example.backend.dto.TantouEditorDtos.ChapterRevisionNoteRequest;
import com.example.backend.dto.TantouEditorDtos.CommentRequest;
import com.example.backend.dto.TantouEditorDtos.CommentResponse;
import com.example.backend.dto.TantouEditorDtos.DossierResponse;
import com.example.backend.dto.TantouEditorDtos.ManuscriptResponse;
import com.example.backend.dto.TantouEditorDtos.ProgressResponse;
import com.example.backend.dto.TantouEditorDtos.RejectSeriesRequest;
import com.example.backend.dto.TantouEditorDtos.RequestDropSeriesRequest;
import com.example.backend.dto.TantouEditorDtos.ReviewDecisionRequest;
import com.example.backend.dto.TantouEditorDtos.SeriesSummaryResponse;
import com.example.backend.service.TantouEditorService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/tantou-editor")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('TANTOU_EDITOR')")
@SecurityRequirement(name = "Bearer Authentication")
public class TantouEditorController {
    private final TantouEditorService service;

    public TantouEditorController(TantouEditorService service) {
        this.service = service;
    }

    @GetMapping("/series")
    public List<SeriesSummaryResponse> getSeries() {
        return service.getSeries();
    }

    @GetMapping("/series/pending-editorial-review")
    public List<SeriesSummaryResponse> getPendingEditorialReviewSeries() {
        return service.getPendingEditorialReviewSeries();
    }

    @GetMapping("/chapters/pending-review")
    public List<ChapterManuscriptResponse> getPendingChapterReviews() {
        return service.getPendingChapterReviews();
    }

    @GetMapping("/chapters/{chapterId}")
    public ChapterManuscriptResponse getChapter(@PathVariable Long chapterId) {
        return service.getChapter(chapterId);
    }

    @PostMapping(value = "/chapters/{chapterId}/revision-notes", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ChapterRevisionNoteResponse createChapterRevisionNote(
            @PathVariable Long chapterId,
            @Valid @ModelAttribute ChapterRevisionNoteRequest request) {
        return service.createChapterRevisionNote(
                chapterId,
                request.image(),
                request.canvasData(),
                request.description(),
                request.orderIndex());
    }

    @PostMapping("/chapters/{chapterId}/request-revision")
    public ChapterManuscriptResponse requestChapterRevision(@PathVariable Long chapterId) {
        return service.requestChapterRevision(chapterId);
    }

    @PostMapping("/chapters/{chapterId}/approve-and-ready")
    public ChapterManuscriptResponse approveAndReadyChapter(@PathVariable Long chapterId) {
        return service.approveAndReadyChapter(chapterId);
    }

    @GetMapping("/series/{seriesId}/manuscript")
    public ManuscriptResponse getManuscript(@PathVariable Long seriesId) {
        return service.getManuscript(seriesId);
    }

    @GetMapping("/series/{seriesId}/dossier")
    public DossierResponse getDossier(@PathVariable Long seriesId) {
        return service.getDossier(seriesId);
    }

    @PatchMapping(
            value = "/series/{seriesId}/submit-to-board",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public DossierResponse submitToEditorialBoard(
            @PathVariable Long seriesId,
            @RequestParam(required = false) String note,
            @RequestParam(value = "files", required = false) List<MultipartFile> files) {
        return service.submitToEditorialBoard(seriesId, note, files);
    }

    @PatchMapping("/series/{seriesId}/request-drop")
    public DossierResponse requestSeriesDrop(
            @PathVariable Long seriesId,
            @Valid @RequestBody RequestDropSeriesRequest request) {
        return service.requestSeriesDrop(seriesId, request.reason());
    }

    @PatchMapping("/series/{seriesId}/request-revision")
    public DossierResponse requestRevision(
            @PathVariable Long seriesId,
            @RequestBody(required = false) ReviewDecisionRequest request) {
        return service.requestRevision(seriesId, request == null ? null : request.note());
    }

    @GetMapping("/series/{seriesId}/progress")
    public ProgressResponse getSeriesProgress(@PathVariable Long seriesId) {
        return service.getSeriesProgress(seriesId);
    }

    @GetMapping("/studio/progress")
    public List<ProgressResponse> getStudioProgress() {
        return service.getStudioProgress();
    }

    @GetMapping("/pages/{pageId}/comments")
    public List<CommentResponse> getPageComments(@PathVariable Long pageId) {
        return service.getPageComments(pageId);
    }

    @PostMapping("/pages/{pageId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentResponse createComment(
            @PathVariable Long pageId,
            @Valid @RequestBody CommentRequest request) {
        return service.createComment(pageId, request);
    }

    @PutMapping("/comments/{commentId}")
    public CommentResponse updateComment(
            @PathVariable Long commentId,
            @Valid @RequestBody CommentRequest request) {
        return service.updateComment(commentId, request);
    }

    @PatchMapping("/comments/{commentId}/resolve")
    public CommentResponse resolveComment(@PathVariable Long commentId) {
        return service.resolveComment(commentId);
    }

    @DeleteMapping("/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable Long commentId) {
        service.deleteComment(commentId);
    }

    @PostMapping("/series/{seriesId}/accept")
    @ResponseStatus(HttpStatus.OK)
    public void acceptSeries(@PathVariable Long seriesId) {
        service.acceptSeries(seriesId);
    }

    @PostMapping("/series/{seriesId}/reject")
    @ResponseStatus(HttpStatus.OK)
    public void rejectSeries(
            @PathVariable Long seriesId,
            @Valid @RequestBody RejectSeriesRequest request) {
        service.rejectSeries(seriesId, request.reason());
    }

    @GetMapping("/notifications")
    public List<NotificationResponse> getNotifications() {
        return service.getNotifications();
    }

    @PatchMapping("/notifications/{notificationId}/read")
    public NotificationResponse markNotificationRead(@PathVariable Long notificationId) {
        return service.markNotificationRead(notificationId);
    }
}

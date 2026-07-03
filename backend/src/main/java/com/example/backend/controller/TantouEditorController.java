package com.example.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.dto.MangakaDtos.NotificationResponse;
import com.example.backend.dto.TantouEditorDtos.CommentRequest;
import com.example.backend.dto.TantouEditorDtos.CommentResponse;
import com.example.backend.dto.TantouEditorDtos.DossierResponse;
import com.example.backend.dto.TantouEditorDtos.ManuscriptResponse;
import com.example.backend.dto.TantouEditorDtos.ProgressResponse;
import com.example.backend.dto.TantouEditorDtos.ReviewDecisionRequest;
import com.example.backend.dto.TantouEditorDtos.ScheduleRequest;
import com.example.backend.dto.TantouEditorDtos.ScheduleResponse;
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

    @GetMapping("/series/{seriesId}/manuscript")
    public ManuscriptResponse getManuscript(@PathVariable Long seriesId) {
        return service.getManuscript(seriesId);
    }

    @GetMapping("/series/{seriesId}/dossier")
    public DossierResponse getDossier(@PathVariable Long seriesId) {
        return service.getDossier(seriesId);
    }

    @PatchMapping("/series/{seriesId}/submit-to-board")
    public DossierResponse submitToEditorialBoard(
            @PathVariable Long seriesId,
            @RequestBody(required = false) ReviewDecisionRequest request) {
        return service.submitToEditorialBoard(seriesId, request == null ? null : request.note());
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

    @GetMapping("/schedules")
    public List<ScheduleResponse> getSchedules(@RequestParam(required = false) Long seriesId) {
        return service.getSchedules(seriesId);
    }

    @PostMapping("/schedules")
    @ResponseStatus(HttpStatus.CREATED)
    public ScheduleResponse createSchedule(@Valid @RequestBody ScheduleRequest request) {
        return service.createSchedule(request);
    }

    @PutMapping("/schedules/{scheduleId}")
    public ScheduleResponse updateSchedule(
            @PathVariable Long scheduleId,
            @Valid @RequestBody ScheduleRequest request) {
        return service.updateSchedule(scheduleId, request);
    }

    @DeleteMapping("/schedules/{scheduleId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSchedule(@PathVariable Long scheduleId) {
        service.deleteSchedule(scheduleId);
    }

    @PostMapping("/series/{seriesId}/accept")
    @ResponseStatus(HttpStatus.OK)
    public void acceptSeries(@PathVariable Long seriesId) {
        service.acceptSeries(seriesId);
    }

    @PostMapping("/series/{seriesId}/reject")
    @ResponseStatus(HttpStatus.OK)
    public void rejectSeries(@PathVariable Long seriesId) {
        service.rejectSeries(seriesId);
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
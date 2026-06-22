package com.example.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.dto.MangakaDtos.AssignTaskRequest;
import com.example.backend.dto.MangakaDtos.AssistantResponse;
import com.example.backend.dto.MangakaDtos.ChapterResponse;
import com.example.backend.dto.MangakaDtos.CreateChapterRequest;
import com.example.backend.dto.MangakaDtos.CreatePageRequest;
import com.example.backend.dto.MangakaDtos.CreateSeriesRequest;
import com.example.backend.dto.MangakaDtos.NotificationResponse;
import com.example.backend.dto.MangakaDtos.PageResponse;
import com.example.backend.dto.MangakaDtos.RankingResponse;
import com.example.backend.dto.MangakaDtos.ReviewSubmissionRequest;
import com.example.backend.dto.MangakaDtos.SeriesResponse;
import com.example.backend.dto.MangakaDtos.SubmissionResponse;
import com.example.backend.dto.MangakaDtos.SubmitSeriesReviewRequest;
import com.example.backend.dto.MangakaDtos.TaskResponse;
import com.example.backend.service.MangakaService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/mangaka")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('MANGAKA')")
public class MangakaController {
    private final MangakaService service;

    public MangakaController(MangakaService service) {
        this.service = service;
    }

    @PostMapping("/series")
    @ResponseStatus(HttpStatus.CREATED)
    public SeriesResponse createSeries(@Valid @RequestBody CreateSeriesRequest request) {
        return service.createSeries(request);
    }

    @GetMapping({"/series", "/my-series"})
    public List<SeriesResponse> getMySeries() {
        return service.getMySeries();
    }

    @GetMapping("/series/{seriesId}/chapters")
    public List<ChapterResponse> getSeriesChapters(@PathVariable Long seriesId) {
        return service.getSeriesChapters(seriesId);
    }

    @PostMapping("/series/{seriesId}/submit")
    public SeriesResponse submitSeries(
            @PathVariable Long seriesId,
            @Valid @RequestBody SubmitSeriesReviewRequest request) {
        return service.submitSeries(seriesId, request);
    }

    @PostMapping("/chapters")
    @ResponseStatus(HttpStatus.CREATED)
    public ChapterResponse createChapter(@Valid @RequestBody CreateChapterRequest request) {
        return service.createChapter(request);
    }

    @PostMapping("/pages")
    @ResponseStatus(HttpStatus.CREATED)
    public PageResponse createPage(@Valid @RequestBody CreatePageRequest request) {
        return service.createPage(request);
    }

    @GetMapping("/assistants")
    public List<AssistantResponse> getAvailableAssistants() {
        return service.getAvailableAssistants();
    }

    @PostMapping("/tasks")
    @ResponseStatus(HttpStatus.CREATED)
    public TaskResponse assignTask(@Valid @RequestBody AssignTaskRequest request) {
        return service.assignTask(request);
    }

    @GetMapping("/chapters/{chapterId}/tasks")
    public List<TaskResponse> getChapterTasks(@PathVariable Long chapterId) {
        return service.getChapterTasks(chapterId);
    }

    @GetMapping("/chapters/{chapterId}/submissions")
    public List<SubmissionResponse> getChapterSubmissions(@PathVariable Long chapterId) {
        return service.getChapterSubmissions(chapterId);
    }

    @PatchMapping("/submissions/{submissionId}/review")
    public SubmissionResponse reviewSubmission(
            @PathVariable Long submissionId,
            @Valid @RequestBody ReviewSubmissionRequest request) {
        return service.reviewSubmission(submissionId, request);
    }

    @GetMapping("/rankings")
    public List<RankingResponse> getRankings() {
        return service.getRankings();
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

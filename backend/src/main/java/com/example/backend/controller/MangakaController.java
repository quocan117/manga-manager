package com.example.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.backend.dto.MangakaDtos.AssignTaskRequest;
import com.example.backend.dto.MangakaDtos.AssistantResponse;
import com.example.backend.dto.MangakaDtos.CreateAssistantRequest;
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
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/mangaka")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('MANGAKA')")
@SecurityRequirement(name = "Bearer Authentication")
public class MangakaController {
    private final MangakaService service;

    public MangakaController(MangakaService service) {
        this.service = service;
    }

    @PostMapping(value = "/series", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public SeriesResponse createSeries(@Valid @RequestBody CreateSeriesRequest request) {
        return service.createSeries(request);
    }

    @PostMapping(value = "/series", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public SeriesResponse createSeriesWithCoverUpload(
            @RequestParam String title,
            @RequestParam List<String> genres,
            @RequestParam(required = false) String coverUrl,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String publicationType,
            @RequestParam(required = false) String artStyle,
            @RequestParam(value = "coverImage", required = false) MultipartFile coverImage) {
        return service.createSeriesWithCoverUpload(
                title, genres, coverUrl, description, publicationType, artStyle, coverImage);
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

    @PostMapping(value = "/chapters/{chapterId}/pages", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public List<PageResponse> uploadChapterPages(
            @PathVariable Long chapterId,
            @RequestParam("images") List<MultipartFile> images) {
        return service.uploadChapterPages(chapterId, images);
    }

    @GetMapping("/chapters/{chapterId}/pages")
    public List<PageResponse> getChapterPages(@PathVariable Long chapterId) {
        return service.getChapterPages(chapterId);
    }

    @GetMapping("/chapters/{chapterId}")
    public ChapterResponse getChapter(@PathVariable Long chapterId) {
        return service.getChapter(chapterId);
    }

    @GetMapping("/assistants")
    public List<AssistantResponse> getAvailableAssistants() {
        return service.getAvailableAssistants();
    }

    @PostMapping("/assistants")
    @ResponseStatus(HttpStatus.CREATED)
    public AssistantResponse createAssistant(@Valid @RequestBody CreateAssistantRequest request) {
        return service.createAssistant(request);
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

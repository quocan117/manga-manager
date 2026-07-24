package com.example.backend.controller;

import com.example.backend.dto.EditorialBoardDtos.CreateUserRequest;
import com.example.backend.dto.EditorialBoardDtos.AssignEditorRequest;
import com.example.backend.dto.EditorialBoardDtos.BoardDecisionRequest;
import com.example.backend.dto.EditorialBoardDtos.BoardDecisionResponse;
import com.example.backend.dto.EditorialBoardDtos.BoardChapterResponse;
import com.example.backend.dto.EditorialBoardDtos.ChapterBoardReviewRequest;
import com.example.backend.dto.EditorialBoardDtos.ImportReaderFeedbackRequest;
import com.example.backend.dto.EditorialBoardDtos.ApprovedSeriesManagementResponse;
import com.example.backend.dto.EditorialBoardDtos.ReaderFeedbackImportResponse;
import com.example.backend.dto.EditorialBoardDtos.ReaderVoteResponse;
import com.example.backend.dto.EditorialBoardDtos.ReviewSeriesResponse;
import com.example.backend.dto.EditorialBoardDtos.SeriesVoteSummaryResponse;
import com.example.backend.dto.EditorialBoardDtos.UpdateUserRequest;
import com.example.backend.dto.EditorialBoardDtos.UserResponse;
import com.example.backend.dto.EditorialBoardDtos.ScheduleResponse;
import com.example.backend.dto.EditorialBoardDtos.ScheduleRequest;
import com.example.backend.dto.MangakaDtos.NotificationResponse;
import com.example.backend.dto.MangakaDtos.RankingResponse;
import com.example.backend.dto.ReviewRegistrationRequest;
import com.example.backend.dto.EditorialBoardDtos.SeriesTotalVotesResponse;
import com.example.backend.model.RegistrationRequest;
import com.example.backend.service.EditorialBoardService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/editorial-board")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('EDITORIAL_BOARD')")
@SecurityRequirement(name = "Bearer Authentication")
public class EditorialBoardController {

    private final EditorialBoardService service;

    public EditorialBoardController(EditorialBoardService service) {
        this.service = service;
    }

    @PreAuthorize("hasRole('EDITORIAL_BOARD')")
    @GetMapping("/registration-requests")
    public List<RegistrationRequest> getAllRequests() {
        return service.getAllRequests();
    }

    @PreAuthorize("hasRole('EDITORIAL_BOARD')")
    @GetMapping("/series/reviewing")
    public List<ReviewSeriesResponse> getReviewingSeries() {
        return service.getReviewingSeries();
    }

    @PreAuthorize("hasRole('EDITORIAL_BOARD')")
    @GetMapping("/series/editor-assignment-required")
    public List<ReviewSeriesResponse> getEditorAssignmentRequiredSeries() {
        return service.getEditorAssignmentRequiredSeries();
    }

    @PreAuthorize("hasRole('EDITORIAL_BOARD')")
    @GetMapping("/series/{id}/review")
    public ReviewSeriesResponse getSeriesReview(@PathVariable Long id) {
        return service.getSeriesReview(id);
    }

    @PreAuthorize("hasRole('EDITORIAL_BOARD')")
    @GetMapping("/series/{id}/decisions")
    public List<BoardDecisionResponse> getSeriesDecisions(@PathVariable Long id) {
        return service.getSeriesDecisions(id);
    }

    @PreAuthorize("hasRole('EDITORIAL_BOARD')")
    @GetMapping("/publish-schedules")
    public List<ScheduleResponse> getPublishSchedules() {
        return service.getPublishSchedules();
    }

    @GetMapping("/series/approved")
    public List<ApprovedSeriesManagementResponse> getApprovedSeries() {
        return service.getApprovedSeries();
    }

    @GetMapping("/series/{id}/chapters")
    public List<BoardChapterResponse> getApprovedSeriesChapters(@PathVariable Long id) {
        return service.getApprovedSeriesChapters(id);
    }

    @GetMapping("/chapters/pending-review")
    public List<BoardChapterResponse> getPendingChapterReviews() {
        return service.getPendingChapterReviews();
    }

    @GetMapping("/chapters/{id}")
    public BoardChapterResponse getChapterReview(@PathVariable Long id) {
        return service.getChapterReview(id);
    }

    @PostMapping("/chapters/{id}/review")
    public BoardChapterResponse reviewChapter(
            @PathVariable Long id,
            @Valid @RequestBody ChapterBoardReviewRequest request) {
        return service.reviewChapter(id, request);
    }

    @PreAuthorize("hasRole('EDITORIAL_BOARD')")
    @PostMapping("/publish-schedules")
    public ScheduleResponse createPublishSchedule(@Valid @RequestBody ScheduleRequest request) {
        return service.createSchedule(request);
    }

    @PreAuthorize("hasRole('EDITORIAL_BOARD')")
    @PutMapping("/publish-schedules/{scheduleId}")
    public ScheduleResponse updatePublishSchedule(
            @PathVariable Long scheduleId,
            @Valid @RequestBody ScheduleRequest request) {
        return service.updateSchedule(scheduleId, request);
    }

    @PreAuthorize("hasRole('EDITORIAL_BOARD')")
    @DeleteMapping("/publish-schedules/{scheduleId}")
    public void deletePublishSchedule(@PathVariable Long scheduleId) {
        service.deleteSchedule(scheduleId);
    }

    @PreAuthorize("hasRole('EDITORIAL_BOARD')")
    @GetMapping("/reader-votes")
    public List<ReaderVoteResponse> getReaderVotes(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return service.getReaderVotes(from, to);
    }

    @PreAuthorize("hasRole('EDITORIAL_BOARD')")
    @GetMapping("/reader-votes/summary")
    public List<SeriesVoteSummaryResponse> getReaderVoteSummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return service.getReaderVoteSummary(from, to);
    }

    @PreAuthorize("hasRole('EDITORIAL_BOARD')")
    @PostMapping("/reader-feedback-imports")
    public List<ReaderFeedbackImportResponse> importReaderFeedback(
            @Valid @RequestBody ImportReaderFeedbackRequest request) {
        return service.importReaderFeedback(request);
    }

    @PreAuthorize("hasRole('EDITORIAL_BOARD')")
    @GetMapping("/reader-feedback-imports")
    public List<ReaderFeedbackImportResponse> getReaderFeedbackImports() {
        return service.getReaderFeedbackImports();
    }

    @PreAuthorize("hasRole('EDITORIAL_BOARD')")
    @GetMapping("/rankings")
    public List<RankingResponse> getRankings(@RequestParam(required = false) String period) {
        return service.getRankings(period);
    }
    @PreAuthorize("hasRole('EDITORIAL_BOARD')")
    @GetMapping("/rankings/periods")
    public List<String> getRankingPeriods() {
        return service.getRankingPeriods();
    }
    @PreAuthorize("hasRole('EDITORIAL_BOARD')")
    @GetMapping("/rankings/total-votes")
    public List<SeriesTotalVotesResponse> getSeriesTotalVotes() {
        return service.getSeriesTotalVotes();
    }

    @PreAuthorize("hasRole('EDITORIAL_BOARD')")
    @GetMapping("/notifications")
    public List<NotificationResponse> getNotifications() {
        return service.getNotifications();
    }

    @PreAuthorize("hasRole('EDITORIAL_BOARD')")
    @PatchMapping("/notifications/{id}/read")
    public NotificationResponse markNotificationRead(@PathVariable Long id) {
        return service.markNotificationRead(id);
    }

    @PreAuthorize("hasRole('EDITORIAL_BOARD')")
    @PostMapping("/series/{id}/decisions")
    public ReviewSeriesResponse voteSeries(
            @PathVariable Long id,
            @Valid @RequestBody BoardDecisionRequest request) {
        return service.voteSeries(id, request);
    }

    @PreAuthorize("hasRole('EDITORIAL_BOARD')")
    @PatchMapping("/series/{id}/assign-editor")
    public ReviewSeriesResponse assignEditor(
            @PathVariable Long id,
            @Valid @RequestBody AssignEditorRequest request) {
        return service.assignEditor(id, request);
    }

    @PreAuthorize("hasRole('EDITORIAL_BOARD')")
    @PutMapping("/series/{id}/cancel")
    public ReviewSeriesResponse cancelSeries(
            @PathVariable Long id,
            @RequestBody(required = false) BoardDecisionRequest request) {
        return service.cancelSeries(id, request);
    }

    @PreAuthorize("hasRole('EDITORIAL_BOARD')")
    @GetMapping("/users")
    public List<UserResponse> getUsers() {
        return service.getUsers();
    }

    @PreAuthorize("hasRole('EDITORIAL_BOARD')")
    @PostMapping("/users")
    public UserResponse createUser(@Valid @RequestBody CreateUserRequest request) {
        return service.createUser(request);
    }

    @PreAuthorize("hasRole('EDITORIAL_BOARD')")
    @PutMapping("/users/{id}")
    public UserResponse updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request) {
        return service.updateUser(id, request);
    }

    @PreAuthorize("hasRole('EDITORIAL_BOARD')")
    @DeleteMapping("/users/{id}")
    public UserResponse deleteUser(@PathVariable Long id) {
        return service.deleteUser(id);
    }

    @PreAuthorize("hasRole('EDITORIAL_BOARD')")
    @PutMapping("/registration-requests/{id}/approve")
    public RegistrationRequest approve(
            @PathVariable Long id,
            @RequestBody ReviewRegistrationRequest request) {
        return service.approve(id, request);
    }

    @PreAuthorize("hasRole('EDITORIAL_BOARD')")
    @PutMapping("/registration-requests/{id}/reject")
    public RegistrationRequest reject(
            @PathVariable Long id,
            @RequestBody ReviewRegistrationRequest request) {
        return service.reject(id, request);
    }
}

package com.example.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.dto.AssistantDtos.SubmissionResponse;
import com.example.backend.dto.AssistantDtos.SubmitTaskRequest;
import com.example.backend.dto.AssistantDtos.TaskResponse;
import com.example.backend.dto.AssistantDtos.NotificationResponse;
import com.example.backend.dto.DrawingDtos.DrawingResponse;
import com.example.backend.dto.DrawingDtos.RevisionResponse;
import com.example.backend.dto.DrawingDtos.SaveDrawingRequest;
import com.example.backend.dto.DrawingDtos.VersionRequest;
import com.example.backend.service.AssistantService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/assistant")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ASSISTANT')")
@SecurityRequirement(name = "Bearer Authentication")
public class AssistantController {
    private final AssistantService service;

    public AssistantController(AssistantService service) {
        this.service = service;
    }

    @GetMapping("/tasks")
    public List<TaskResponse> getMyTasks() {
        return service.getMyTasks();
    }

    @GetMapping("/tasks/{taskId}")
    public TaskResponse getTask(@PathVariable Long taskId) {
        return service.getTask(taskId);
    }

    @PatchMapping("/tasks/{taskId}/accept")
    public TaskResponse acceptTask(@PathVariable Long taskId) {
        return service.acceptTask(taskId);
    }

    @GetMapping("/tasks/{taskId}/drawing")
    public DrawingResponse getDrawing(@PathVariable Long taskId) {
        return service.getDrawing(taskId);
    }

    @PutMapping("/tasks/{taskId}/drawing")
    public DrawingResponse saveDrawing(
            @PathVariable Long taskId,
            @Valid @RequestBody SaveDrawingRequest request) {
        return service.saveDrawing(taskId, request);
    }

    @PostMapping("/tasks/{taskId}/drawing/finalize")
    public DrawingResponse finalizeDrawing(
            @PathVariable Long taskId,
            @Valid @RequestBody VersionRequest request) {
        return service.finalizeDrawing(taskId, request);
    }

    @GetMapping("/tasks/{taskId}/drawing/revisions")
    public List<RevisionResponse> getRevisions(@PathVariable Long taskId) {
        return service.getRevisions(taskId);
    }

    @PostMapping("/tasks/{taskId}/drawing/revisions/{revisionId}/restore")
    public DrawingResponse restoreRevision(
            @PathVariable Long taskId,
            @PathVariable Long revisionId,
            @Valid @RequestBody VersionRequest request) {
        return service.restoreRevision(taskId, revisionId, request);
    }

    @GetMapping("/tasks/{taskId}/submissions")
    public List<SubmissionResponse> getSubmissions(@PathVariable Long taskId) {
        return service.getSubmissions(taskId);
    }

    @PostMapping("/tasks/{taskId}/submissions")
    @ResponseStatus(HttpStatus.CREATED)
    public SubmissionResponse submitTask(
            @PathVariable Long taskId,
            @Valid @RequestBody SubmitTaskRequest request) {
        return service.submitTask(taskId, request);
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

package com.example.backend.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.example.backend.dto.AssistantDtos.SubmissionResponse;
import com.example.backend.dto.AssistantDtos.SubmitTaskRequest;
import com.example.backend.dto.AssistantDtos.TaskResponse;
import com.example.backend.dto.AssistantDtos.NotificationResponse;
import com.example.backend.dto.DrawingDtos.DrawingResponse;
import com.example.backend.dto.DrawingDtos.RevisionResponse;
import com.example.backend.dto.DrawingDtos.SaveDrawingRequest;
import com.example.backend.dto.DrawingDtos.VersionRequest;
import com.example.backend.dto.MangakaDtos.UploadedFileResponse;
import com.example.backend.dto.MangakaDtos.TaskMarkupPageResponse;
import com.example.backend.model.Chapter;
import com.example.backend.model.ChapterPage;
import com.example.backend.model.MangaSeries;
import com.example.backend.model.Notification;
import com.example.backend.model.PageDrawing;
import com.example.backend.model.PageDrawingRevision;
import com.example.backend.model.SeriesFile;
import com.example.backend.model.Submission;
import com.example.backend.model.Task;
import com.example.backend.model.TaskMarkupPage;
import com.example.backend.model.User;
import com.example.backend.repository.NotificationRepository;
import com.example.backend.repository.PageDrawingRepository;
import com.example.backend.repository.PageDrawingRevisionRepository;
import com.example.backend.repository.SeriesFileRepository;
import com.example.backend.repository.SubmissionRepository;
import com.example.backend.repository.TaskRepository;
import com.example.backend.repository.TaskMarkupPageRepository;
import com.example.backend.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class AssistantService {
    private static final String ASSIGNED_STATUS = "ASSIGNED";
    private static final String IN_PROGRESS_STATUS = "IN_PROGRESS";
    private static final String SUBMITTED_STATUS = "SUBMITTED";
    private static final String REVISION_REQUESTED_STATUS = "REVISION_REQUESTED";
    private static final Set<String> WORKABLE_STATUSES = Set.of(
            ASSIGNED_STATUS,
            IN_PROGRESS_STATUS,
            REVISION_REQUESTED_STATUS);

    private final TaskRepository taskRepository;
    private final SubmissionRepository submissionRepository;
    private final PageDrawingRepository drawingRepository;
    private final PageDrawingRevisionRepository revisionRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final SeriesFileRepository seriesFileRepository;
    private final TaskMarkupPageRepository taskMarkupPageRepository;
    private final TaskFileStorageService taskFileStorageService;
    private final ObjectMapper objectMapper;

    public AssistantService(
            TaskRepository taskRepository,
            SubmissionRepository submissionRepository,
            PageDrawingRepository drawingRepository,
            PageDrawingRevisionRepository revisionRepository,
            UserRepository userRepository,
            NotificationRepository notificationRepository,
            SeriesFileRepository seriesFileRepository,
            TaskMarkupPageRepository taskMarkupPageRepository,
            TaskFileStorageService taskFileStorageService,
            ObjectMapper objectMapper) {
        this.taskRepository = taskRepository;
        this.submissionRepository = submissionRepository;
        this.drawingRepository = drawingRepository;
        this.revisionRepository = revisionRepository;
        this.userRepository = userRepository;
        this.notificationRepository = notificationRepository;
        this.seriesFileRepository = seriesFileRepository;
        this.taskMarkupPageRepository = taskMarkupPageRepository;
        this.taskFileStorageService = taskFileStorageService;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getMyTasks() {
        return taskRepository.findByAssignedToEmailOrderByCreatedAtDesc(currentEmail())
                .stream()
                .map(this::toTaskResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TaskResponse getTask(Long taskId) {
        return toTaskResponse(assignedTask(taskId));
    }

    @Transactional
    public TaskResponse acceptTask(Long taskId) {
        Task task = assignedTask(taskId);
        if (!ASSIGNED_STATUS.equals(task.getStatus())) {
            throw conflict("Task cannot be accepted in its current status");
        }
        task.setStatus(IN_PROGRESS_STATUS);
        return toTaskResponse(taskRepository.save(task));
    }

    @Transactional
    public DrawingResponse getDrawing(Long taskId) {
        Task task = assignedTask(taskId);
        return toDrawingResponse(getOrCreateDrawing(task, currentUser()));
    }

    @Transactional(readOnly = true)
    public DrawingResponse getMasterDrawing(Long taskId) {
        Task task = assignedTask(taskId);
        PageDrawing masterDrawing = drawingRepository
                .findByPagePageIdAndTaskIsNull(taskPage(task).getPageId())
                .orElse(null);

        if (masterDrawing == null) {
            return null;
        }
        return toDrawingResponse(masterDrawing);
    }

    @Transactional
    public DrawingResponse saveDrawing(Long taskId, SaveDrawingRequest request) {
        Task task = assignedTask(taskId);
        assertTaskCanBeWorkedOn(task);

        User assistant = currentUser();
        PageDrawing drawing = drawingRepository
                .findByTaskTaskIdAndOwnerEmail(taskId, assistant.getEmail())
                .orElse(null);

        if (drawing == null) {
            if (request.expectedVersion() != null && request.expectedVersion() != 0L) {
                throw conflict("Drawing version is outdated");
            }
            drawing = new PageDrawing();
            drawing.setTask(task);
            drawing.setPage(taskPage(task));
            drawing.setOwner(assistant);
            drawing.setCreatedAt(LocalDateTime.now());
            drawing.setStatus("DRAFT");
        } else {
            assertVersion(drawing, request.expectedVersion());
            if ("FINALIZED".equals(drawing.getStatus())) {
                drawing.setStatus("DRAFT");
            }
        }

        drawing.setCanvasData(request.canvasData().toString());
        drawing.setPreviewImageUrl(blankToNull(request.previewImageUrl()));
        drawing.setUpdatedAt(LocalDateTime.now());

        if (!IN_PROGRESS_STATUS.equals(task.getStatus())) {
            task.setStatus(IN_PROGRESS_STATUS);
            taskRepository.save(task);
        }
        return saveWithRevision(drawing, assistant);
    }

    @Transactional
    public DrawingResponse finalizeDrawing(Long taskId, VersionRequest request) {
        Task task = assignedTask(taskId);
        assertTaskCanBeWorkedOn(task);
        User assistant = currentUser();
        PageDrawing drawing = taskDrawing(taskId, assistant);
        assertVersion(drawing, request.expectedVersion());

        drawing.setStatus("FINALIZED");
        drawing.setUpdatedAt(LocalDateTime.now());
        if (!IN_PROGRESS_STATUS.equals(task.getStatus())) {
            task.setStatus(IN_PROGRESS_STATUS);
            taskRepository.save(task);
        }
        return saveWithRevision(drawing, assistant);
    }

    @Transactional(readOnly = true)
    public List<RevisionResponse> getRevisions(Long taskId) {
        assignedTask(taskId);
        PageDrawing drawing = taskDrawing(taskId, currentUser());
        return revisionRepository.findByDrawingDrawingIdOrderByVersionNumberDesc(drawing.getDrawingId())
                .stream()
                .map(this::toRevisionResponse)
                .toList();
    }

    @Transactional
    public DrawingResponse restoreRevision(Long taskId, Long revisionId, VersionRequest request) {
        Task task = assignedTask(taskId);
        assertTaskCanBeWorkedOn(task);
        User assistant = currentUser();
        PageDrawing drawing = taskDrawing(taskId, assistant);
        assertVersion(drawing, request.expectedVersion());

        PageDrawingRevision revision = revisionRepository
                .findByRevisionIdAndDrawingDrawingId(revisionId, drawing.getDrawingId())
                .orElseThrow(() -> notFound("Drawing revision not found"));
        drawing.setCanvasData(revision.getCanvasData());
        drawing.setPreviewImageUrl(revision.getPreviewImageUrl());
        drawing.setStatus("DRAFT");
        drawing.setUpdatedAt(LocalDateTime.now());

        if (!IN_PROGRESS_STATUS.equals(task.getStatus())) {
            task.setStatus(IN_PROGRESS_STATUS);
            taskRepository.save(task);
        }
        return saveWithRevision(drawing, assistant);
    }

    @Transactional
    public SubmissionResponse submitTask(
            Long taskId,
            SubmitTaskRequest request,
            List<MultipartFile> resultFiles) {
        Task task = assignedTask(taskId);
        assertTaskCanBeWorkedOn(task);
        if (REVISION_REQUESTED_STATUS.equals(task.getStatus())) {
            task.setRoundNumber(currentRound(task) + 1);
        }
        User assistant = currentUser();
        PageDrawing drawing = drawingRepository
                .findByTaskTaskIdAndOwnerEmail(taskId, assistant.getEmail())
                .orElse(null);
        if (drawing != null && request.expectedDrawingVersion() != null) {
            assertVersion(drawing, request.expectedDrawingVersion());
        } else if (drawing == null && request.expectedDrawingVersion() != null) {
            throw conflict("Drawing does not exist");
        }

        List<SeriesFile> savedResultFiles = taskFileStorageService.storeTaskFiles(
                task,
                assistant,
                resultFiles,
                currentRound(task),
                TaskFileStorageService.TASK_SUBMISSION);

        String artifactUrl = blankToNull(request.artifactUrl());
        if (artifactUrl == null && drawing != null) {
            artifactUrl = blankToNull(drawing.getPreviewImageUrl());
        }
        if (artifactUrl == null) {
            artifactUrl = savedResultFiles.stream()
                    .filter(SeriesFileSupport::isPreviewable)
                    .findFirst()
                    .map(SeriesFileSupport::downloadUrl)
                    .orElse(SeriesFileSupport.downloadUrl(savedResultFiles.get(0)));
        }

        Submission submission = new Submission();
        submission.setTask(task);
        submission.setChapter(task.getChapter());
        submission.setSubmittedBy(assistant);
        submission.setSubmittedAt(LocalDateTime.now());
        submission.setStatus(SUBMITTED_STATUS);
        submission.setRoundNumber(currentRound(task));
        submission.setArtifactUrl(artifactUrl);
        submission.setOriginalFileUrl(SeriesFileSupport.downloadUrl(savedResultFiles.get(0)));
        submission.setNote(blankToNull(request.note()));

        task.setStatus(SUBMITTED_STATUS);
        taskRepository.save(task);
        Submission savedSubmission = submissionRepository.save(submission);
        notify(
                task.getAssignedBy(),
                "TASK_SUBMITTED",
                task.getTaskId(),
                "Trợ lý " + assistant.getUsername() + " đã hoàn thiện phần việc. Vui lòng kiểm duyệt");
        return toSubmissionResponse(savedSubmission);
    }

    @Transactional(readOnly = true)
    public List<TaskMarkupPageResponse> getTaskMarkupPages(Long taskId) {
        Task task = assignedTask(taskId);
        return taskMarkupPageRepository
                .findByTaskTaskIdAndRoundNumberOrderByOrderIndexAscCreatedAtAsc(
                        taskId,
                        currentRound(task))
                .stream()
                .map(this::toTaskMarkupPageResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SubmissionResponse> getSubmissions(Long taskId) {
        assignedTask(taskId);
        return submissionRepository.findByTaskTaskIdOrderBySubmittedAtDesc(taskId)
                .stream()
                .map(this::toSubmissionResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotifications() {
        return notificationRepository.findByUserEmailOrderByCreatedAtDesc(currentEmail())
                .stream()
                .map(this::toNotificationResponse)
                .toList();
    }

    @Transactional
    public NotificationResponse markNotificationRead(Long notificationId) {
        Notification notification = notificationRepository
                .findByNotificationIdAndUserEmail(notificationId, currentEmail())
                .orElseThrow(() -> notFound("Notification not found"));
        notification.setIsRead(true);
        return toNotificationResponse(notificationRepository.save(notification));
    }

    private void notify(User user, String type, Long refId, String message) {
        if (user == null) {
            return;
        }
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setType(type);
        notification.setReferenceId(refId);
        notification.setMessage(message);
        notification.setIsRead(false);
        notification.setCreatedAt(LocalDateTime.now());
        notificationRepository.save(notification);
    }

    private PageDrawing getOrCreateDrawing(Task task, User assistant) {
        return drawingRepository.findByTaskTaskIdAndOwnerEmail(task.getTaskId(), assistant.getEmail())
                .orElseGet(() -> {
                    PageDrawing drawing = new PageDrawing();
                    drawing.setTask(task);
                    drawing.setPage(taskPage(task));
                    drawing.setOwner(assistant);
                    drawing.setCanvasData("{}");
                    drawing.setStatus("DRAFT");
                    drawing.setCreatedAt(LocalDateTime.now());
                    drawing.setUpdatedAt(LocalDateTime.now());
                    return drawingRepository.saveAndFlush(drawing);
                });
    }

    private PageDrawing taskDrawing(Long taskId, User assistant) {
        return drawingRepository.findByTaskTaskIdAndOwnerEmail(taskId, assistant.getEmail())
                .orElseThrow(() -> notFound("Drawing not found"));
    }

    private Task assignedTask(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> notFound("Task not found"));
        if (task.getAssignedTo() == null || !currentEmail().equals(task.getAssignedTo().getEmail())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not assigned to this task");
        }
        return task;
    }

    private ChapterPage taskPage(Task task) {
        if (task.getPage() == null) {
            throw badRequest("Task does not have a drawing page");
        }
        return task.getPage();
    }

    private void assertTaskCanBeWorkedOn(Task task) {
        if (task.getStatus() == null || !WORKABLE_STATUSES.contains(task.getStatus())) {
            throw conflict("Task cannot be changed in its current status");
        }
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
            return toDrawingResponse(saved);
        } catch (OptimisticLockingFailureException exception) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Drawing was changed by another session",
                    exception);
        }
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

    private TaskResponse toTaskResponse(Task task) {
        Chapter chapter = task.getChapter();
        MangaSeries series = chapter == null ? null : chapter.getSeries();
        ChapterPage page = task.getPage();
        User assignedBy = task.getAssignedBy();
        List<Submission> submissions = submissionRepository.findByTaskTaskIdOrderBySubmittedAtDesc(task.getTaskId());
        SubmissionResponse latestSubmission = submissions.isEmpty() ? null : toSubmissionResponse(submissions.get(0));

        return new TaskResponse(
                task.getTaskId(),
                series == null ? null : series.getSeriesId(),
                series == null ? null : series.getTitle(),
                chapter == null ? null : chapter.getChapterId(),
                chapter == null ? null : chapter.getChapterNumber(),
                chapter == null ? null : chapter.getTitle(),
                page == null ? null : page.getPageId(),
                page == null ? null : page.getPageNumber(),
                page == null ? null : page.getImageUrl(),
                task.getOriginalFileUrl(),
                assignedBy == null ? null : assignedBy.getUserId(),
                assignedBy == null ? null : assignedBy.getUsername(),
                task.getTaskType(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                currentRound(task),
                task.getDueDate(),
                task.getAreaX(),
                task.getAreaY(),
                task.getAreaWidth(),
                task.getAreaHeight(),
                task.getCreatedAt(),
                latestSubmission,
                sourceFiles(task));
    }

    private List<UploadedFileResponse> sourceFiles(Task task) {
        if (task == null || task.getTaskId() == null) {
            return List.of();
        }
        return seriesFileRepository
                .findByTaskTaskIdAndRoundNumberAndPurposeAndActiveTrueOrderByUploadedAtAsc(
                        task.getTaskId(),
                        currentRound(task),
                        TaskFileStorageService.TASK_ORIGINAL)
                .stream()
                .map(this::toUploadedFileResponse)
                .toList();
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

    private SubmissionResponse toSubmissionResponse(Submission submission) {
        User submitter = submission.getSubmittedBy();
        return new SubmissionResponse(
                submission.getSubmissionId(),
                submission.getTask() == null ? null : submission.getTask().getTaskId(),
                submission.getChapter() == null ? null : submission.getChapter().getChapterId(),
                submitter == null ? null : submitter.getUserId(),
                submitter == null ? null : submitter.getUsername(),
                submission.getArtifactUrl(),
                submission.getOriginalFileUrl(),
                submission.getNote(),
                submission.getStatus(),
                submissionRound(submission),
                submission.getReviewNote(),
                submission.getSubmittedAt(),
                submission.getReviewedAt(),
                resultFiles(submission));
    }

    private List<UploadedFileResponse> resultFiles(Submission submission) {
        Task task = submission.getTask();
        if (task == null || task.getTaskId() == null) {
            return List.of();
        }
        return seriesFileRepository
                .findByTaskTaskIdAndRoundNumberAndPurposeAndActiveTrueOrderByUploadedAtAsc(
                        task.getTaskId(),
                        submissionRound(submission),
                        TaskFileStorageService.TASK_SUBMISSION)
                .stream()
                .map(this::toUploadedFileResponse)
                .toList();
    }

    private TaskMarkupPageResponse toTaskMarkupPageResponse(TaskMarkupPage markupPage) {
        return new TaskMarkupPageResponse(
                markupPage.getMarkupPageId(),
                markupPage.getTask().getTaskId(),
                markupPage.getRoundNumber(),
                markupPage.getImageUrl(),
                markupPage.getCanvasData(),
                markupPage.getOrderIndex(),
                markupPage.getCreatedAt());
    }

    private int currentRound(Task task) {
        return task.getRoundNumber() == null ? 1 : task.getRoundNumber();
    }

    private int submissionRound(Submission submission) {
        return submission.getRoundNumber() == null ? 1 : submission.getRoundNumber();
    }

    private DrawingResponse toDrawingResponse(PageDrawing drawing) {
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

    private NotificationResponse toNotificationResponse(Notification notification) {
        return new NotificationResponse(
                notification.getNotificationId(),
                notification.getType(),
                notification.getReferenceId(),
                notification.getMessage(),
                notification.getIsRead(),
                notification.getCreatedAt());
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

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
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

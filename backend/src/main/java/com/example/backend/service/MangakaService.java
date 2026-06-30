package com.example.backend.service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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
import com.example.backend.model.Chapter;
import com.example.backend.model.ChapterPage;
import com.example.backend.model.MangaSeries;
import com.example.backend.model.Notification;
import com.example.backend.model.Role;
import com.example.backend.model.Submission;
import com.example.backend.model.Task;
import com.example.backend.model.User;
import com.example.backend.repository.ChapterPageRepository;
import com.example.backend.repository.ChapterRepository;
import com.example.backend.repository.MangaSeriesRepository;
import com.example.backend.repository.NotificationRepository;
import com.example.backend.repository.RoleRepository;
import com.example.backend.repository.SeriesRankingRepository;
import com.example.backend.repository.SubmissionRepository;
import com.example.backend.repository.TaskRepository;
import com.example.backend.repository.UserRepository;

@Service
public class MangakaService {
    private static final Set<String> TASK_TYPES = Set.of("BACKGROUND", "TEXT", "EFFECTS", "OTHER");
    private static final Set<String> REVIEW_DECISIONS = Set.of("APPROVED", "REVISION_REQUESTED");
    private static final String ASSISTANT_ROLE = "ASSISTANT";
    private static final String ACTIVE_STATUS = "ACTIVE";
    private static final String TANTOU_REVIEW_STATUS = "TANTOU_REVIEW";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final MangaSeriesRepository mangaSeriesRepository;
    private final ChapterRepository chapterRepository;
    private final ChapterPageRepository chapterPageRepository;
    private final TaskRepository taskRepository;
    private final SubmissionRepository submissionRepository;
    private final SeriesRankingRepository seriesRankingRepository;
    private final NotificationRepository notificationRepository;

    public MangakaService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            MangaSeriesRepository mangaSeriesRepository,
            ChapterRepository chapterRepository,
            ChapterPageRepository chapterPageRepository,
            TaskRepository taskRepository,
            SubmissionRepository submissionRepository,
            SeriesRankingRepository seriesRankingRepository,
            NotificationRepository notificationRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.mangaSeriesRepository = mangaSeriesRepository;
        this.chapterRepository = chapterRepository;
        this.chapterPageRepository = chapterPageRepository;
        this.taskRepository = taskRepository;
        this.submissionRepository = submissionRepository;
        this.seriesRankingRepository = seriesRankingRepository;
        this.notificationRepository = notificationRepository;
    }

    @Transactional
    public SeriesResponse createSeries(CreateSeriesRequest request) {
        User mangaka = currentUser();
        MangaSeries series = new MangaSeries();
        series.setTitle(request.title().trim());
        series.setAuthor(mangaka);
        series.setGenre(String.join(", ", request.genres()));
        series.setCoverImage(blankToNull(request.coverUrl()));
        series.setDescription(blankToNull(request.description()));
        series.setPublicationType(blankToNull(request.publicationType()));
        series.setArtStyle(blankToNull(request.artStyle()));
        series.setStatus("DRAFT");
        series.setCreatedAt(LocalDateTime.now());
        return toSeriesResponse(mangaSeriesRepository.save(series));
    }

    @Transactional(readOnly = true)
    public List<SeriesResponse> getMySeries() {
        return mangaSeriesRepository.findByAuthorEmailOrderByCreatedAtDesc(currentEmail())
                .stream()
                .map(this::toSeriesResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ChapterResponse> getSeriesChapters(Long seriesId) {
        ownedSeries(seriesId);
        return chapterRepository.findBySeriesSeriesIdOrderByChapterNumberAsc(seriesId)
                .stream()
                .map(this::toChapterResponse)
                .toList();
    }

    @Transactional
    public SeriesResponse submitSeries(Long seriesId, SubmitSeriesReviewRequest request) {
        MangaSeries series = ownedSeries(seriesId);
        if (!Set.of("DRAFT", "REVISION_REQUESTED").contains(series.getStatus())) {
            throw conflict("Only draft or revision-requested series can be submitted");
        }
        series.setStoryboardUrl(request.storyboardUrl());
        series.setSubmittedAt(LocalDateTime.now());
        series.setStatus(TANTOU_REVIEW_STATUS);
        return toSeriesResponse(mangaSeriesRepository.save(series));
    }

    @Transactional
    public ChapterResponse createChapter(CreateChapterRequest request) {
        MangaSeries series = ownedSeries(request.seriesId());
        if (chapterRepository.existsBySeriesSeriesIdAndChapterNumber(
                series.getSeriesId(), request.chapterNumber())) {
            throw conflict("Chapter number already exists in this manga series");
        }

        Chapter chapter = new Chapter();
        chapter.setSeries(series);
        chapter.setChapterNumber(request.chapterNumber());
        chapter.setTitle(request.title().trim());
        chapter.setStatus("DRAFT");
        chapter.setCreatedAt(LocalDateTime.now());
        return toChapterResponse(chapterRepository.save(chapter));
    }

    @Transactional
    public PageResponse createPage(CreatePageRequest request) {
        Chapter chapter = ownedChapter(request.chapterId());
        if (chapterPageRepository.existsByChapterChapterIdAndPageNumber(
                chapter.getChapterId(), request.pageNumber())) {
            throw conflict("Page number already exists in this chapter");
        }

        ChapterPage page = new ChapterPage();
        page.setChapter(chapter);
        page.setPageNumber(request.pageNumber());
        page.setImageUrl(request.imageUrl());
        page.setPageStatus("DRAFT");
        page.setCreatedAt(LocalDateTime.now());
        return toPageResponse(chapterPageRepository.save(page));
    }

    @Transactional(readOnly = true)
    public List<PageResponse> getChapterPages(Long chapterId) {
        ownedChapter(chapterId);
        return chapterPageRepository.findByChapterChapterIdOrderByPageNumberAsc(chapterId)
                .stream()
                .map(this::toPageResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AssistantResponse> getAvailableAssistants() {
        return userRepository.findByRoleRoleNameAndStatusOrderByUsernameAsc(ASSISTANT_ROLE, ACTIVE_STATUS)
                .stream()
                .map(user -> new AssistantResponse(
                        user.getUserId(), user.getUsername(), user.getEmail(), user.getAvatarUrl()))
                .toList();
    }

    @Transactional
    public AssistantResponse createAssistant(CreateAssistantRequest request) {
        User mangaka = currentUser();
        String username = request.username().trim();
        String email = request.email().trim();
        if (userRepository.existsByUsername(username)) {
            throw conflict("Username already exists");
        }
        if (userRepository.existsByEmail(email)) {
            throw conflict("Email already exists");
        }

        Role assistantRole = roleRepository.findByRoleName(ASSISTANT_ROLE)
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setRoleName(ASSISTANT_ROLE);
                    return roleRepository.save(role);
                });

        User assistant = new User();
        assistant.setUsername(username);
        assistant.setEmail(email);
        assistant.setPassword(passwordEncoder.encode(request.password()));
        assistant.setAvatarUrl(blankToNull(request.avatarUrl()));
        assistant.setStatus(ACTIVE_STATUS);
        assistant.setRole(assistantRole);
        assistant.setCreatedBy(mangaka);
        assistant.setCreatedAt(LocalDateTime.now());

        return toAssistantResponse(userRepository.save(assistant));
    }

    @Transactional
    public TaskResponse assignTask(AssignTaskRequest request) {
        User mangaka = currentUser();
        ChapterPage page = chapterPageRepository.findById(request.pageId())
                .orElseThrow(() -> notFound("Chapter page not found"));
        assertOwnedChapter(page.getChapter());

        User assistant = userRepository.findById(request.assistantId())
                .orElseThrow(() -> notFound("Assistant not found"));
        if (assistant.getRole() == null || !ASSISTANT_ROLE.equals(assistant.getRole().getRoleName())
                || !ACTIVE_STATUS.equals(assistant.getStatus())) {
            throw badRequest("The selected user is not an active assistant");
        }

        String taskType = request.taskType().trim().toUpperCase(Locale.ROOT);
        if (!TASK_TYPES.contains(taskType)) {
            throw badRequest("Task type must be BACKGROUND, TEXT, EFFECTS, or OTHER");
        }

        Task task = new Task();
        task.setTitle(request.title().trim());
        task.setDescription(request.description());
        task.setAssignedTo(assistant);
        task.setAssignedBy(mangaka);
        task.setChapter(page.getChapter());
        task.setPage(page);
        task.setTaskType(taskType);
        task.setAreaX(request.areaX());
        task.setAreaY(request.areaY());
        task.setAreaWidth(request.areaWidth());
        task.setAreaHeight(request.areaHeight());
        task.setDueDate(request.dueDate());
        task.setStatus("ASSIGNED");
        task.setCreatedAt(LocalDateTime.now());
        return toTaskResponse(taskRepository.save(task));
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getChapterTasks(Long chapterId) {
        ownedChapter(chapterId);
        return taskRepository.findByChapterChapterIdAndAssignedByEmailOrderByCreatedAtDesc(
                chapterId, currentEmail())
                .stream()
                .map(this::toTaskResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SubmissionResponse> getChapterSubmissions(Long chapterId) {
        ownedChapter(chapterId);
        return submissionRepository.findByChapterChapterIdOrderBySubmittedAtDesc(chapterId)
                .stream()
                .map(this::toSubmissionResponse)
                .toList();
    }

    @Transactional
    public SubmissionResponse reviewSubmission(Long submissionId, ReviewSubmissionRequest request) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> notFound("Submission not found"));
        assertOwnedChapter(submission.getChapter());

        String decision = request.decision().trim().toUpperCase(Locale.ROOT);
        if (!REVIEW_DECISIONS.contains(decision)) {
            throw badRequest("Decision must be APPROVED or REVISION_REQUESTED");
        }
        if ("REVISION_REQUESTED".equals(decision)
                && (request.reviewNote() == null || request.reviewNote().isBlank())) {
            throw badRequest("A review note is required when requesting revision");
        }

        submission.setStatus(decision);
        submission.setReviewNote(request.reviewNote());
        submission.setReviewedAt(LocalDateTime.now());
        submission.setReviewedBy(currentUser());
        if (submission.getTask() != null) {
            submission.getTask().setStatus(decision);
            taskRepository.save(submission.getTask());
        }
        return toSubmissionResponse(submissionRepository.save(submission));
    }

    @Transactional(readOnly = true)
    public List<RankingResponse> getRankings() {
        return seriesRankingRepository.findBySeriesAuthorEmailOrderByCalculatedAtDesc(currentEmail())
                .stream()
                .map(ranking -> new RankingResponse(
                        ranking.getRankingId(),
                        ranking.getSeries().getSeriesId(),
                        ranking.getSeries().getTitle(),
                        ranking.getRankingPosition(),
                        ranking.getScore(),
                        ranking.getVoteCount(),
                        ranking.getPeriod(),
                        ranking.getCalculatedAt()))
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

    private MangaSeries ownedSeries(Long seriesId) {
        MangaSeries series = mangaSeriesRepository.findById(seriesId)
                .orElseThrow(() -> notFound("Manga series not found"));
        if (series.getAuthor() == null || !currentEmail().equals(series.getAuthor().getEmail())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not own this manga series");
        }
        return series;
    }

    private Chapter ownedChapter(Long chapterId) {
        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> notFound("Chapter not found"));
        assertOwnedChapter(chapter);
        return chapter;
    }

    private void assertOwnedChapter(Chapter chapter) {
        if (chapter == null || chapter.getSeries() == null || chapter.getSeries().getAuthor() == null
                || !currentEmail().equals(chapter.getSeries().getAuthor().getEmail())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not own this chapter");
        }
    }

    private SeriesResponse toSeriesResponse(MangaSeries series) {
        List<String> genres = series.getGenre() == null || series.getGenre().isBlank()
                ? List.of()
                : Arrays.stream(series.getGenre().split(","))
                        .map(String::trim)
                        .filter(value -> !value.isBlank())
                        .toList();
        return new SeriesResponse(
                series.getSeriesId(), series.getTitle(), genres, series.getCoverImage(),
                series.getDescription(), series.getStatus(), series.getStoryboardUrl(),
                series.getSubmittedAt(), series.getRankingScore());
    }

    private TaskResponse toTaskResponse(Task task) {
        ChapterPage page = task.getPage();
        User assistant = task.getAssignedTo();
        return new TaskResponse(
                task.getTaskId(), task.getChapter().getChapterId(),
                page == null ? null : page.getPageId(),
                page == null ? null : page.getPageNumber(),
                assistant == null ? null : assistant.getUserId(),
                assistant == null ? null : assistant.getUsername(),
                task.getTaskType(), task.getTitle(), task.getDescription(), task.getStatus(),
                task.getDueDate(), task.getAreaX(), task.getAreaY(),
                task.getAreaWidth(), task.getAreaHeight());
    }

    private AssistantResponse toAssistantResponse(User user) {
        return new AssistantResponse(
                user.getUserId(), user.getUsername(), user.getEmail(), user.getAvatarUrl());
    }

    private ChapterResponse toChapterResponse(Chapter chapter) {
        return new ChapterResponse(
                chapter.getChapterId(), chapter.getSeries().getSeriesId(), chapter.getChapterNumber(),
                chapter.getTitle(), chapter.getStatus(), chapter.getCreatedAt());
    }

    private PageResponse toPageResponse(ChapterPage page) {
        return new PageResponse(
                page.getPageId(), page.getChapter().getChapterId(), page.getPageNumber(),
                page.getImageUrl(), page.getPageStatus());
    }

    private SubmissionResponse toSubmissionResponse(Submission submission) {
        User submitter = submission.getSubmittedBy();
        return new SubmissionResponse(
                submission.getSubmissionId(),
                submission.getTask() == null ? null : submission.getTask().getTaskId(),
                submission.getChapter().getChapterId(),
                submitter == null ? null : submitter.getUserId(),
                submitter == null ? null : submitter.getUsername(),
                submission.getArtifactUrl(), submission.getNote(), submission.getStatus(),
                submission.getReviewNote(), submission.getSubmittedAt(), submission.getReviewedAt());
    }

    private NotificationResponse toNotificationResponse(Notification notification) {
        return new NotificationResponse(
                notification.getNotificationId(), notification.getType(), notification.getReferenceId(),
                notification.getMessage(), notification.getIsRead(), notification.getCreatedAt());
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

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}

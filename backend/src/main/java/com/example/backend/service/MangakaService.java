package com.example.backend.service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.example.backend.dto.ChapterRevisionNoteResponse;
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
import com.example.backend.dto.MangakaDtos.SubmitChapterToEditorRequest;
import com.example.backend.dto.MangakaDtos.SubmitSeriesReviewRequest;
import com.example.backend.dto.MangakaDtos.TaskResponse;
import com.example.backend.dto.MangakaDtos.UpdateAssistantStatusRequest;
import com.example.backend.model.Chapter;
import com.example.backend.model.ChapterPage;
import com.example.backend.model.ChapterRevisionNote;
import com.example.backend.model.MangaSeries;
import com.example.backend.model.Notification;
import com.example.backend.model.Role;
import com.example.backend.model.Submission;
import com.example.backend.model.Task;
import com.example.backend.model.User;
import com.example.backend.repository.ChapterPageRepository;
import com.example.backend.repository.ChapterRepository;
import com.example.backend.repository.ChapterRevisionNoteRepository;
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
    private static final String TANTOU_EDITOR_ROLE = "TANTOU_EDITOR";
    private static final String ACTIVE_STATUS = "ACTIVE";
    private static final String INACTIVE_STATUS = "INACTIVE";
    private static final String DELETED_STATUS = "DELETED";
    private static final Set<String> ASSISTANT_STATUSES = Set.of(ACTIVE_STATUS, INACTIVE_STATUS);
    private static final String SUBMITTED_TO_EDITOR_STATUS = "SUBMITTED_TO_EDITOR";
    private static final String PENDING_EDITOR_STATUS = "PENDING_EDITOR";
    private static final String TANTOU_REVIEW_STATUS = "TANTOU_REVIEW";
    private static final String REVISION_REQUESTED_STATUS = "REVISION_REQUESTED";
    private static final String PUBLISHED_STATUS = "PUBLISHED";
    private static final Set<String> TANTOU_WORKLOAD_STATUSES = Set.of(
            "DRAFT", TANTOU_REVIEW_STATUS, REVISION_REQUESTED_STATUS);
    private static final long MAX_PAGE_IMAGE_SIZE_BYTES = 5L * 1024 * 1024;
    private static final long MAX_COVER_IMAGE_SIZE_BYTES = 5L * 1024 * 1024;
    private static final Set<String> PAGE_IMAGE_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp", "image/gif");
    private static final Set<String> COVER_IMAGE_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp", "image/gif");
    private static final Set<String> PAGE_IMAGE_EXTENSIONS = Set.of(
            ".jpg", ".jpeg", ".png", ".webp", ".gif");
    private static final Set<String> COVER_IMAGE_EXTENSIONS = Set.of(
            ".jpg", ".jpeg", ".png", ".webp", ".gif");

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final MangaSeriesRepository mangaSeriesRepository;
    private final ChapterRepository chapterRepository;
    private final ChapterRevisionNoteRepository chapterRevisionNoteRepository;
    private final ChapterPageRepository chapterPageRepository;
    private final TaskRepository taskRepository;
    private final SubmissionRepository submissionRepository;
    private final SeriesRankingRepository seriesRankingRepository;
    private final NotificationRepository notificationRepository;

    @Value("${manga.upload.page-image-root:}")
    private String pageImageUploadRootOverride;

    @Value("${manga.upload.cover-image-root:}")
    private String coverImageUploadRootOverride;

    public MangakaService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            MangaSeriesRepository mangaSeriesRepository,
            ChapterRepository chapterRepository,
            ChapterRevisionNoteRepository chapterRevisionNoteRepository,
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
        this.chapterRevisionNoteRepository = chapterRevisionNoteRepository;
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

    @Transactional
    public SeriesResponse createSeriesWithCoverUpload(
            String title,
            List<String> genres,
            String coverUrl,
            String description,
            String publicationType,
            String artStyle,
            MultipartFile coverImage) {
        String storedCoverUrl = blankToNull(coverUrl);
        if (coverImage != null && !coverImage.isEmpty()) {
            storedCoverUrl = storeSeriesCoverImage(coverImage);
        }

        return createSeries(new CreateSeriesRequest(
                title,
                parseGenreValues(genres),
                storedCoverUrl,
                description,
                publicationType,
                artStyle));
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
        String storyboardUrl = requireHttpUrl(request.storyboardUrl(), "storyboardUrl");
        series.setStoryboardUrl(storyboardUrl);

        if (REVISION_REQUESTED_STATUS.equalsIgnoreCase(series.getStatus())
                && series.getTantouEditor() != null) {
            series.setStatus(TANTOU_REVIEW_STATUS);
            mangaSeriesRepository.save(series);
            notify(series.getTantouEditor(), "SERIES_RESUBMITTED", series.getSeriesId(),
                    "Mangaka " + series.getAuthor().getUsername() + " đã gửi lại hồ sơ series '"
                            + series.getTitle() + "' sau khi chỉnh sửa.");
            return toSeriesResponse(series);
        }

        User assignedEditor = getEditorWithLeastWorkload(null);
        series.setTantouEditor(assignedEditor);
        series.setStatus(PENDING_EDITOR_STATUS);
        series.setEditorAssignedAt(LocalDateTime.now());

        mangaSeriesRepository.save(series);

        Notification notif = new Notification();
        notif.setUser(assignedEditor);
        notif.setType("SYSTEM_ASSIGNMENT");
        notif.setReferenceId(series.getSeriesId());
        notif.setMessage("Mangaka " + series.getAuthor().getUsername() + " vừa gửi hồ sơ series '" + series.getTitle()
                + "'. Vui lòng nhấn Nhận hồ sơ trong vòng 24h.");
        notif.setCreatedAt(LocalDateTime.now());
        notif.setIsRead(false);
        notificationRepository.save(notif);

        return toSeriesResponse(series);
    }

    private void notify(User user, String type, Long refId, String message) {
        if (user == null)
            return;
        Notification n = new Notification();
        n.setUser(user);
        n.setType(type);
        n.setReferenceId(refId);
        n.setMessage(message);
        n.setIsRead(false);
        n.setCreatedAt(LocalDateTime.now());
        notificationRepository.save(n);
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

    @Transactional
    public List<PageResponse> uploadChapterPages(Long chapterId, List<MultipartFile> images) {
        Chapter chapter = ownedChapter(chapterId);
        if (images == null || images.isEmpty() || images.stream().allMatch(MultipartFile::isEmpty)) {
            throw badRequest("At least one image file is required");
        }

        images.forEach(this::validatePageImage);

        int nextPageNumber = chapterPageRepository.findByChapterChapterIdOrderByPageNumberAsc(chapterId)
                .stream()
                .map(ChapterPage::getPageNumber)
                .filter(Objects::nonNull)
                .max(Integer::compareTo)
                .orElse(0) + 1;

        Path chapterDirectory = pageImageUploadRoot().resolve("chapter-" + chapterId).normalize();
        try {
            Files.createDirectories(chapterDirectory);
        } catch (IOException exception) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "Could not create page image folder", exception);
        }

        List<PageResponse> responses = new ArrayList<>();
        for (MultipartFile image : images) {
            String fileName = pageImageFileName(chapterId, nextPageNumber, image.getOriginalFilename());
            Path target = chapterDirectory.resolve(fileName).normalize();
            if (!target.startsWith(chapterDirectory)) {
                throw badRequest("Invalid image file name");
            }

            try (InputStream inputStream = image.getInputStream()) {
                Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException exception) {
                throw new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR, "Could not save page image", exception);
            }

            ChapterPage page = new ChapterPage();
            page.setChapter(chapter);
            page.setPageNumber(nextPageNumber);
            page.setImageUrl("pages/chapter-" + chapterId + "/" + fileName);
            page.setPageStatus("DRAFT");
            page.setCreatedAt(LocalDateTime.now());
            responses.add(toPageResponse(chapterPageRepository.save(page)));
            nextPageNumber++;
        }

        return responses;
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
    public ChapterResponse getChapter(Long chapterId) {
        Chapter chapter = ownedChapter(chapterId);
        return toChapterResponse(chapter);
    }

    @Transactional
    public ChapterResponse submitChapterToEditor(Long chapterId, SubmitChapterToEditorRequest request) {
        Chapter chapter = ownedChapter(chapterId);
        if (PUBLISHED_STATUS.equalsIgnoreCase(chapter.getStatus())) {
            throw badRequest("Published chapters cannot be submitted again");
        }

        MangaSeries series = chapter.getSeries();
        User editor = series == null ? null : series.getTantouEditor();
        if (editor == null) {
            throw conflict("This series has no assigned tantou editor");
        }

        String manuscriptUrl = requireHttpUrl(request.manuscriptUrl(), "manuscriptUrl");

        if (REVISION_REQUESTED_STATUS.equalsIgnoreCase(chapter.getStatus())) {
            chapterRevisionNoteRepository.deleteByChapterChapterId(chapterId);
        }

        chapter.setManuscriptUrl(manuscriptUrl);
        chapter.setStatus(SUBMITTED_TO_EDITOR_STATUS);
        Chapter savedChapter = chapterRepository.save(chapter);
        notify(
                editor,
                "NEW_CHAPTER_SUBMISSION",
                savedChapter.getChapterId(),
                "New chapter submitted: " + savedChapter.getTitle());
        return toChapterResponse(savedChapter);
    }

    @Transactional(readOnly = true)
    public List<ChapterRevisionNoteResponse> getChapterRevisionNotes(Long chapterId) {
        ownedChapter(chapterId);
        return chapterRevisionNoteRepository.findByChapterChapterIdOrderByOrderIndexAscCreatedAtAsc(chapterId)
                .stream()
                .map(this::toChapterRevisionNoteResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AssistantResponse> getAvailableAssistants() {
        User mangaka = currentUser();
        return userRepository.findByRoleRoleNameAndCreatedByOrderByUsernameAsc(ASSISTANT_ROLE, mangaka)
                .stream()
                .map(this::toAssistantResponse)
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
    public AssistantResponse updateAssistantStatus(Long assistantId, UpdateAssistantStatusRequest request) {
        User assistant = ownedAssistant(assistantId);
        String normalized = request.status() == null
                ? ""
                : request.status().trim().toUpperCase(Locale.ROOT);
        if (!ASSISTANT_STATUSES.contains(normalized)) {
            throw badRequest("Status must be ACTIVE or INACTIVE");
        }
        assistant.setStatus(normalized);
        return toAssistantResponse(userRepository.save(assistant));
    }

    @Transactional
    public AssistantResponse deleteAssistant(Long assistantId) {
        User assistant = ownedAssistant(assistantId);
        assistant.setStatus(DELETED_STATUS);
        return toAssistantResponse(userRepository.save(assistant));
    }

    private User ownedAssistant(Long assistantId) {
        User mangaka = currentUser();
        User assistant = userRepository.findById(assistantId)
                .orElseThrow(() -> notFound("Assistant not found"));
        if (assistant.getRole() == null || !ASSISTANT_ROLE.equals(assistant.getRole().getRoleName())) {
            throw badRequest("The selected user is not an assistant");
        }
        User createdBy = assistant.getCreatedBy();
        if (createdBy == null || !createdBy.getUserId().equals(mangaka.getUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only manage assistants you created");
        }
        return assistant;
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
        task.setOriginalFileUrl(blankToNull(request.originalFileUrl()));
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
        Task savedTask = taskRepository.save(task);
        notify(
                assistant,
                "TASK_ASSIGNED",
                savedTask.getTaskId(),
                "Bạn được giao nhiệm vụ \"" + savedTask.getTitle()
                        + "\" ở trang " + page.getPageNumber() + ".");
        return toTaskResponse(savedTask);
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
        Submission savedSubmission = submissionRepository.save(submission);
        notify(
                submission.getSubmittedBy(),
                "APPROVED".equals(decision) ? "TASK_APPROVED" : "TASK_REVISION_REQUESTED",
                submission.getTask() == null ? submission.getSubmissionId() : submission.getTask().getTaskId(),
                "Bài nộp của bạn đã được duyệt với kết quả " + decision + ".");
        return toSubmissionResponse(savedSubmission);
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
                task.getTaskType(), task.getTitle(), task.getDescription(), task.getOriginalFileUrl(), task.getStatus(),
                task.getDueDate(), task.getAreaX(), task.getAreaY(),
                task.getAreaWidth(), task.getAreaHeight());
    }

    private AssistantResponse toAssistantResponse(User user) {
        return new AssistantResponse(
                user.getUserId(), user.getUsername(), user.getEmail(), user.getStatus(),
                user.getCreatedAt(), user.getAvatarUrl());
    }

    private ChapterResponse toChapterResponse(Chapter chapter) {
        return new ChapterResponse(
                chapter.getChapterId(), chapter.getSeries().getSeriesId(), chapter.getChapterNumber(),
                chapter.getTitle(), chapter.getManuscriptUrl(), chapter.getStatus(), chapter.getCreatedAt());
    }

    private ChapterRevisionNoteResponse toChapterRevisionNoteResponse(ChapterRevisionNote note) {
        Chapter chapter = note.getChapter();
        return new ChapterRevisionNoteResponse(
                note.getNoteId(),
                chapter == null ? null : chapter.getChapterId(),
                note.getImageUrl(),
                note.getOrderIndex(),
                note.getCreatedAt());
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
                submission.getArtifactUrl(), submission.getOriginalFileUrl(), submission.getNote(),
                submission.getStatus(),
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

    private String requireHttpUrl(String rawUrl, String fieldName) {
        String value = blankToNull(rawUrl);
        if (value == null) {
            throw badRequest(fieldName + " is required");
        }

        try {
            URI uri = URI.create(value);
            String scheme = uri.getScheme();
            if (scheme == null
                    || uri.getHost() == null
                    || (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme))) {
                throw badRequest(fieldName + " must be a valid http(s) URL");
            }
        } catch (IllegalArgumentException exception) {
            throw badRequest(fieldName + " must be a valid http(s) URL");
        }
        return value;
    }

    private ResponseStatusException conflict(String message) {
        return new ResponseStatusException(HttpStatus.CONFLICT, message);
    }

    private User assignTantouEditor(User currentEditor) {
        if (isActiveTantouEditor(currentEditor)) {
            return currentEditor;
        }

        List<User> editors = userRepository.findByRoleRoleNameAndStatusOrderByUsernameAsc(
                TANTOU_EDITOR_ROLE, ACTIVE_STATUS);
        if (editors.isEmpty()) {
            throw conflict("No active tantou editor is available for assignment");
        }

        long lowestWorkload = editors.stream()
                .mapToLong(this::countTantouEditorWorkload)
                .min()
                .orElse(0L);

        List<User> candidates = editors.stream()
                .filter(editor -> countTantouEditorWorkload(editor) == lowestWorkload)
                .toList();
        return candidates.get(ThreadLocalRandom.current().nextInt(candidates.size()));
    }

    private boolean isActiveTantouEditor(User user) {
        return user != null
                && user.getStatus() != null
                && ACTIVE_STATUS.equalsIgnoreCase(user.getStatus())
                && user.getRole() != null
                && TANTOU_EDITOR_ROLE.equals(user.getRole().getRoleName());
    }

    private long countTantouEditorWorkload(User editor) {
        return mangaSeriesRepository.countByTantouEditorUserIdAndStatusIn(
                editor.getUserId(), TANTOU_WORKLOAD_STATUSES);
    }

    private String storeSeriesCoverImage(MultipartFile coverImage) {
        validateCoverImage(coverImage);

        Path uploadRoot = coverImageUploadRoot();
        try {
            Files.createDirectories(uploadRoot);
        } catch (IOException exception) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "Could not create cover image folder", exception);
        }

        String fileName = coverImageFileName(coverImage.getOriginalFilename());
        Path target = uploadRoot.resolve(fileName).normalize();
        if (!target.startsWith(uploadRoot)) {
            throw badRequest("Invalid cover image file name");
        }

        try (InputStream inputStream = coverImage.getInputStream()) {
            Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException exception) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "Could not save cover image", exception);
        }

        return "series/" + fileName;
    }

    private void validatePageImage(MultipartFile image) {
        if (image == null || image.isEmpty()) {
            throw badRequest("Image file cannot be empty");
        }
        if (image.getSize() > MAX_PAGE_IMAGE_SIZE_BYTES) {
            throw new ResponseStatusException(
                    HttpStatus.PAYLOAD_TOO_LARGE, "Each page image must be 5MB or smaller");
        }

        String contentType = image.getContentType();
        if (contentType == null || !PAGE_IMAGE_CONTENT_TYPES.contains(contentType.toLowerCase(Locale.ROOT))) {
            throw badRequest("Only JPG, PNG, WEBP, or GIF images are allowed");
        }
    }

    private void validateCoverImage(MultipartFile image) {
        if (image == null || image.isEmpty()) {
            throw badRequest("Cover image file cannot be empty");
        }
        if (image.getSize() > MAX_COVER_IMAGE_SIZE_BYTES) {
            throw new ResponseStatusException(
                    HttpStatus.PAYLOAD_TOO_LARGE, "Cover image must be 5MB or smaller");
        }

        String contentType = image.getContentType();
        if (contentType == null || !COVER_IMAGE_CONTENT_TYPES.contains(contentType.toLowerCase(Locale.ROOT))) {
            throw badRequest("Only JPG, PNG, WEBP, or GIF cover images are allowed");
        }
    }

    private String pageImageFileName(Long chapterId, int pageNumber, String originalFilename) {
        String extension = fileExtension(originalFilename);
        if (!PAGE_IMAGE_EXTENSIONS.contains(extension)) {
            throw badRequest("Only JPG, PNG, WEBP, or GIF images are allowed");
        }
        return "chapter-" + chapterId + "-page-" + pageNumber + "-" + UUID.randomUUID() + extension;
    }

    private String coverImageFileName(String originalFilename) {
        String extension = fileExtension(originalFilename);
        if (!COVER_IMAGE_EXTENSIONS.contains(extension)) {
            throw badRequest("Only JPG, PNG, WEBP, or GIF cover images are allowed");
        }
        return "series-cover-" + UUID.randomUUID() + extension;
    }

    private String fileExtension(String originalFilename) {
        if (originalFilename == null || originalFilename.isBlank()) {
            throw badRequest("Image file name is required");
        }
        int extensionIndex = originalFilename.lastIndexOf('.');
        if (extensionIndex < 0 || extensionIndex == originalFilename.length() - 1) {
            throw badRequest("Image file extension is required");
        }
        return originalFilename.substring(extensionIndex).toLowerCase(Locale.ROOT);
    }

    private Path pageImageUploadRoot() {
        if (pageImageUploadRootOverride != null && !pageImageUploadRootOverride.isBlank()) {
            return Path.of(pageImageUploadRootOverride).toAbsolutePath().normalize();
        }

        return Path.of("uploads/pages").toAbsolutePath().normalize();
    }

    private Path coverImageUploadRoot() {
        if (coverImageUploadRootOverride != null && !coverImageUploadRootOverride.isBlank()) {
            return Path.of(coverImageUploadRootOverride).toAbsolutePath().normalize();
        }

        return Path.of("uploads/covers").toAbsolutePath().normalize();
    }

    private List<String> parseGenreValues(List<String> genreValues) {
        List<String> genres = new ArrayList<>();
        if (genreValues != null) {
            for (String value : genreValues) {
                if (value == null) {
                    continue;
                }
                for (String genre : value.split(",")) {
                    String trimmed = genre.trim();
                    if (!trimmed.isBlank()) {
                        genres.add(trimmed);
                    }
                }
            }
        }
        if (genres.isEmpty()) {
            throw badRequest("At least one genre is required");
        }
        return genres;
    }

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    public User getEditorWithLeastWorkload(Long excludeEditorId) {
        List<User> editors = userRepository.findByRoleRoleNameAndStatusOrderByUsernameAsc("TANTOU_EDITOR", "ACTIVE");

        if (excludeEditorId != null) {
            editors.removeIf(e -> e.getUserId().equals(excludeEditorId));
        }

        if (editors.isEmpty()) {
            throw new RuntimeException("Hiện không có biên tập viên nào khả dụng.");
        }

        // Các trạng thái được tính là "đang có việc"
        List<String> activeStatuses = Arrays.asList("PENDING_EDITOR", "TANTOU_REVIEW", "REVIEWING");
        int minWorkload = Integer.MAX_VALUE;
        List<User> candidates = new ArrayList<>();

        for (User editor : editors) {
            int workload = (int) mangaSeriesRepository.countByTantouEditorUserIdAndStatusIn(editor.getUserId(),
                    activeStatuses);
            if (workload < minWorkload) {
                minWorkload = workload;
                candidates.clear();
                candidates.add(editor);
            } else if (workload == minWorkload) {
                candidates.add(editor);
            }
        }
        // Chọn ngẫu nhiên nếu có nhiều người cùng mức độ ưu tiên
        return candidates.get(new Random().nextInt(candidates.size()));
    }
}

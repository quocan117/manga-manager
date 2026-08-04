package com.example.backend.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
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
import com.example.backend.dto.MangakaDtos.AssistantParticipationResponse;
import com.example.backend.dto.MangakaDtos.AssistantResponse;
import com.example.backend.dto.MangakaDtos.CreateAssistantRequest;
import com.example.backend.dto.MangakaDtos.ChapterResponse;
import com.example.backend.dto.MangakaDtos.CreateChapterRequest;
import com.example.backend.dto.MangakaDtos.CreatePageRequest;
import com.example.backend.dto.MangakaDtos.CreateSeriesRequest;
import com.example.backend.dto.MangakaDtos.FeedbackHistoryResponse;
import com.example.backend.dto.MangakaDtos.NotificationResponse;
import com.example.backend.dto.MangakaDtos.PageHistoryResponse;
import com.example.backend.dto.MangakaDtos.PageResponse;
import com.example.backend.dto.MangakaDtos.RankingResponse;
import com.example.backend.dto.MangakaDtos.RankingSummaryResponse;
import com.example.backend.dto.MangakaDtos.ReviewSubmissionRequest;
import com.example.backend.dto.MangakaDtos.ReviseTaskRequest;
import com.example.backend.dto.MangakaDtos.SeriesResponse;
import com.example.backend.dto.MangakaDtos.SubmissionResponse;
import com.example.backend.dto.MangakaDtos.TaskResponse;
import com.example.backend.dto.MangakaDtos.TaskMarkupPageResponse;
import com.example.backend.dto.MangakaDtos.UpdateAssistantStatusRequest;
import com.example.backend.dto.MangakaDtos.UploadedFileResponse;
import com.example.backend.model.Chapter;
import com.example.backend.model.ChapterPage;
import com.example.backend.model.ChapterPageHistory;
import com.example.backend.model.ChapterRevisionNote;
import com.example.backend.model.MangaSeries;
import com.example.backend.model.Notification;
import com.example.backend.model.Role;
import com.example.backend.model.SeriesFile;
import com.example.backend.model.Submission;
import com.example.backend.model.Task;
import com.example.backend.model.TaskMarkupPage;
import com.example.backend.model.User;
import com.example.backend.repository.ChapterPageRepository;
import com.example.backend.repository.ChapterPageHistoryRepository;
import com.example.backend.repository.ChapterRepository;
import com.example.backend.repository.ChapterRevisionNoteRepository;
import com.example.backend.repository.BoardDecisionRepository;
import com.example.backend.repository.MangaSeriesRepository;
import com.example.backend.repository.NotificationRepository;
import com.example.backend.repository.ReaderFeedbackImportRepository;
import com.example.backend.repository.RoleRepository;
import com.example.backend.repository.SeriesFileRepository;
import com.example.backend.repository.SeriesRankingRepository;
import com.example.backend.repository.SubmissionRepository;
import com.example.backend.repository.TaskRepository;
import com.example.backend.repository.TaskMarkupPageRepository;
import com.example.backend.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class MangakaService {
    private static final long MIN_TASK_DEADLINE_HOURS = 24;
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
    private static final String EDITOR_ASSIGNMENT_REQUIRED_STATUS = "EDITOR_ASSIGNMENT_REQUIRED";
    private static final String TANTOU_REVIEW_STATUS = "TANTOU_REVIEW";
    private static final String REVISION_REQUESTED_STATUS = "REVISION_REQUESTED";
    private static final String SERIES_SUBMISSION_PURPOSE = "SERIES_SUBMISSION";
    private static final String CHAPTER_MANUSCRIPT_PURPOSE = "CHAPTER_MANUSCRIPT";
    private static final Set<String> CHAPTER_SUBMISSION_STATUSES = Set.of(
            "DRAFT", REVISION_REQUESTED_STATUS);
    private static final List<String> TANTOU_ACTIVE_WORKLOAD_STATUSES = List.of(
            PENDING_EDITOR_STATUS, TANTOU_REVIEW_STATUS, "REVIEWING");
    private static final long MAX_PAGE_IMAGE_SIZE_BYTES = 5L * 1024 * 1024;
    private static final long MAX_COVER_IMAGE_SIZE_BYTES = 5L * 1024 * 1024;
    private static final long MAX_SERIES_FILE_SIZE_BYTES = 20L * 1024 * 1024;
    private static final long MAX_ZIP_FILE_SIZE_BYTES = 100L * 1024 * 1024;
    private static final long MAX_SERIES_SUBMISSION_SIZE_BYTES = 200L * 1024 * 1024;
    private static final int MAX_SERIES_FILES_PER_SUBMISSION = 20;
    private static final Set<String> PAGE_IMAGE_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp", "image/gif");
    private static final Set<String> COVER_IMAGE_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp", "image/gif");
    private static final Set<String> PAGE_IMAGE_EXTENSIONS = Set.of(
            ".jpg", ".jpeg", ".png", ".webp", ".gif");
    private static final Set<String> COVER_IMAGE_EXTENSIONS = Set.of(
            ".jpg", ".jpeg", ".png", ".webp", ".gif");
    private static final Set<String> SERIES_FILE_EXTENSIONS = Set.of(
            ".jpg", ".jpeg", ".png", ".webp", ".gif",
            ".pdf", ".txt", ".md",
            ".doc", ".docx", ".xls", ".xlsx", ".ppt", ".pptx",
            ".zip");

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final MangaSeriesRepository mangaSeriesRepository;
    private final BoardDecisionRepository boardDecisionRepository;
    private final ChapterRepository chapterRepository;
    private final ChapterRevisionNoteRepository chapterRevisionNoteRepository;
    private final ChapterPageRepository chapterPageRepository;
    private final ChapterPageHistoryRepository chapterPageHistoryRepository;
    private final TaskRepository taskRepository;
    private final SubmissionRepository submissionRepository;
    private final SeriesRankingRepository seriesRankingRepository;
    private final ReaderFeedbackImportRepository readerFeedbackImportRepository;
    private final SeriesFileRepository seriesFileRepository;
    private final NotificationRepository notificationRepository;
    private final TaskMarkupPageRepository taskMarkupPageRepository;
    private final TaskFileStorageService taskFileStorageService;
    private final SeriesHistoryService seriesHistoryService;
    private final ObjectMapper objectMapper;

    @Value("${manga.upload.page-image-root:}")
    private String pageImageUploadRootOverride;

    @Value("${manga.upload.cover-image-root:}")
    private String coverImageUploadRootOverride;

    @Value("${manga.upload.series-file-root:}")
    private String seriesFileUploadRootOverride;

    @Value("${manga.upload.task-markup-root:}")
    private String taskMarkupUploadRootOverride;

    public MangakaService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            MangaSeriesRepository mangaSeriesRepository,
            BoardDecisionRepository boardDecisionRepository,
            ChapterRepository chapterRepository,
            ChapterRevisionNoteRepository chapterRevisionNoteRepository,
            ChapterPageRepository chapterPageRepository,
            ChapterPageHistoryRepository chapterPageHistoryRepository,
            TaskRepository taskRepository,
            SubmissionRepository submissionRepository,
            SeriesRankingRepository seriesRankingRepository,
            ReaderFeedbackImportRepository readerFeedbackImportRepository,
            SeriesFileRepository seriesFileRepository,
            NotificationRepository notificationRepository,
            TaskMarkupPageRepository taskMarkupPageRepository,
            TaskFileStorageService taskFileStorageService,
            SeriesHistoryService seriesHistoryService,
            ObjectMapper objectMapper) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.mangaSeriesRepository = mangaSeriesRepository;
        this.boardDecisionRepository = boardDecisionRepository;
        this.chapterRepository = chapterRepository;
        this.chapterRevisionNoteRepository = chapterRevisionNoteRepository;
        this.chapterPageRepository = chapterPageRepository;
        this.chapterPageHistoryRepository = chapterPageHistoryRepository;
        this.taskRepository = taskRepository;
        this.submissionRepository = submissionRepository;
        this.seriesRankingRepository = seriesRankingRepository;
        this.readerFeedbackImportRepository = readerFeedbackImportRepository;
        this.seriesFileRepository = seriesFileRepository;
        this.notificationRepository = notificationRepository;
        this.taskMarkupPageRepository = taskMarkupPageRepository;
        this.taskFileStorageService = taskFileStorageService;
        this.seriesHistoryService = seriesHistoryService;
        this.objectMapper = objectMapper;
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
        series.setEditorAssignmentLocked(false);
        series.setCreatedAt(LocalDateTime.now());
        MangaSeries savedSeries = mangaSeriesRepository.save(series);
        seriesHistoryService.record(
                savedSeries,
                mangaka,
                "SERIES_CREATED",
                null,
                savedSeries.getStatus(),
                null,
                savedSeries.getSeriesId());
        return toSeriesResponse(savedSeries);
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

    @Transactional(readOnly = true)
    public List<FeedbackHistoryResponse> getSeriesFeedbackHistory(Long seriesId) {
        ownedSeries(seriesId);
        return readerFeedbackImportRepository.findBySeriesSeriesIdOrderByImportedAtDesc(seriesId)
                .stream()
                .map(feedbackImport -> new FeedbackHistoryResponse(
                        feedbackImport.getImportId(),
                        feedbackImport.getPeriodStart(),
                        feedbackImport.getPeriodEnd(),
                        feedbackImport.getVoteCount()))
                .toList();
    }

    @Transactional(readOnly = true)
    public RankingSummaryResponse getSeriesRankingSummary(
            Long seriesId,
            LocalDateTime periodStart,
            LocalDateTime periodEnd) {
        ownedSeries(seriesId);
        validatePeriodRange(periodStart, periodEnd);
        var ranking = seriesRankingRepository
                .findBySeriesSeriesIdAndPeriodStartAndPeriodEnd(
                        seriesId,
                        periodStart,
                        periodEnd)
                .orElseThrow(() -> notFound("Ranking not found for this series and period"));
        return new RankingSummaryResponse(
                seriesId,
                ranking.getPeriodStart(),
                ranking.getPeriodEnd(),
                ranking.getRankingPosition(),
                ranking.getScore(),
                ranking.getVoteCount(),
                seriesRankingRepository.countByPeriodStartAndPeriodEnd(periodStart, periodEnd),
                seriesRankingRepository.sumVoteCountByPeriodStartAndPeriodEnd(periodStart, periodEnd));
    }

    @Transactional
    public SeriesResponse submitSeriesWithFiles(Long seriesId, List<MultipartFile> files) {
        return submitSeriesInternal(seriesId, files);
    }

    private SeriesResponse submitSeriesInternal(Long seriesId, List<MultipartFile> files) {
        MangaSeries series = ownedSeries(seriesId);
        User mangaka = series.getAuthor();
        String previousStatus = series.getStatus();
        List<MultipartFile> submissionFiles = requireSeriesSubmissionFiles(files);
        boolean resubmission = REVISION_REQUESTED_STATUS.equalsIgnoreCase(series.getStatus())
                && series.getTantouEditor() != null;
        if (resubmission) {
            seriesFileRepository.deactivateActiveFiles(seriesId, SERIES_SUBMISSION_PURPOSE);
        }
        int submissionRound = seriesFileRepository.findMaxRoundNumberBySeriesAndPurpose(
                seriesId, SERIES_SUBMISSION_PURPOSE) + 1;
        storeSeriesFiles(
                series,
                mangaka,
                submissionFiles,
                SERIES_SUBMISSION_PURPOSE,
                submissionRound);

        if (resubmission) {
            boardDecisionRepository.deleteBySeriesSeriesId(series.getSeriesId());
            series.setStatus(TANTOU_REVIEW_STATUS);
            mangaSeriesRepository.save(series);
            seriesHistoryService.record(
                    series,
                    mangaka,
                    "SERIES_RESUBMITTED",
                    previousStatus,
                    series.getStatus(),
                    null,
                    series.getSeriesId());
            notify(series.getTantouEditor(), "SERIES_RESUBMITTED", series.getSeriesId(),
                    "Mangaka " + series.getAuthor().getUsername() + " đã gửi lại hồ sơ series '"
                            + series.getTitle() + "' sau khi chỉnh sửa.");
            return toSeriesResponse(series);
        }

        Optional<User> assignedEditorCandidate = findEditorWithLeastWorkloadExcluding(
                Set.of(), series.getGenre());
        if (assignedEditorCandidate.isEmpty()) {
            series.setTantouEditor(null);
            series.setStatus(EDITOR_ASSIGNMENT_REQUIRED_STATUS);
            series.setEditorAssignmentLocked(false);
            series.setEditorAssignedAt(null);
            mangaSeriesRepository.save(series);
            seriesHistoryService.record(
                    series,
                    mangaka,
                    "SERIES_SUBMITTED_AWAITING_EDITOR",
                    previousStatus,
                    series.getStatus(),
                    "No active tantou editor is available for automatic assignment",
                    series.getSeriesId());
            notify(mangaka, "EDITOR_ASSIGNMENT_REQUIRED", series.getSeriesId(),
                    "Series \"" + series.getTitle()
                            + "\" is waiting for Editorial Board to assign an editor.");
            notifyEditorialBoard("EDITOR_ASSIGNMENT_REQUIRED", series.getSeriesId(),
                    "Series \"" + series.getTitle()
                            + "\" needs a forced tantou editor assignment because no editor is available.");
            return toSeriesResponse(series);
        }

        User assignedEditor = assignedEditorCandidate.get();
        series.setTantouEditor(assignedEditor);
        series.setStatus(PENDING_EDITOR_STATUS);
        series.setEditorAssignmentLocked(false);
        series.setEditorAssignedAt(LocalDateTime.now());

        mangaSeriesRepository.save(series);
        seriesHistoryService.record(
                series,
                mangaka,
                "SERIES_SUBMITTED",
                previousStatus,
                series.getStatus(),
                null,
                series.getSeriesId());

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

    private void notifyEditorialBoard(String type, Long referenceId, String message) {
        userRepository.findByRoleRoleNameAndStatusOrderByUsernameAsc("EDITORIAL_BOARD", ACTIVE_STATUS)
                .forEach(board -> notify(board, type, referenceId, message));
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
    public List<PageHistoryResponse> getPageHistory(Long pageId) {
        ChapterPage page = chapterPageRepository.findById(pageId)
                .orElseThrow(() -> notFound("Chapter page not found"));
        assertOwnedChapter(page.getChapter());
        return chapterPageHistoryRepository.findByPagePageIdOrderByCreatedAtDesc(pageId)
                .stream()
                .map(this::toPageHistoryResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ChapterResponse getChapter(Long chapterId) {
        Chapter chapter = ownedChapter(chapterId);
        return toChapterResponse(chapter);
    }

    @Transactional
    public ChapterResponse submitChapterToEditor(Long chapterId, List<MultipartFile> files) {
        Chapter chapter = ownedChapter(chapterId);
        String currentStatus = chapter.getStatus() == null
                ? ""
                : chapter.getStatus().trim().toUpperCase(Locale.ROOT);
        if (!CHAPTER_SUBMISSION_STATUSES.contains(currentStatus)) {
            throw conflict("Only draft chapters or chapters awaiting revision can be submitted");
        }

        MangaSeries series = chapter.getSeries();
        User editor = series == null ? null : series.getTantouEditor();
        if (editor == null) {
            throw conflict("This series has no assigned tantou editor");
        }

        List<MultipartFile> manuscriptFiles = files == null
                ? List.of()
                : files.stream()
                        .filter(file -> file != null && !file.isEmpty())
                        .toList();
        List<ChapterPage> chapterPages = chapterPageRepository
                .findByChapterChapterIdOrderByPageNumberAsc(chapterId);
        if (manuscriptFiles.isEmpty() && chapterPages.isEmpty()) {
            throw badRequest("Chapter must contain pages or uploaded manuscript files");
        }
        if (!manuscriptFiles.isEmpty()) {
            manuscriptFiles = requireChapterManuscriptFiles(manuscriptFiles);
        }

        int manuscriptRound = seriesFileRepository.findMaxRoundNumberByChapterAndPurpose(
                chapterId, CHAPTER_MANUSCRIPT_PURPOSE) + 1;
        seriesFileRepository.deactivateActiveChapterFiles(chapterId, CHAPTER_MANUSCRIPT_PURPOSE);
        if (!manuscriptFiles.isEmpty()) {
            storeChapterManuscriptFiles(
                    series,
                    chapter,
                    series.getAuthor(),
                    manuscriptFiles,
                    manuscriptRound);
        }
        String previousStatus = chapter.getStatus();
        chapter.setStatus(SUBMITTED_TO_EDITOR_STATUS);
        Chapter savedChapter = chapterRepository.save(chapter);
        seriesHistoryService.record(
                series,
                series.getAuthor(),
                "CHAPTER_SUBMITTED_TO_EDITOR",
                previousStatus,
                savedChapter.getStatus(),
                savedChapter.getTitle(),
                savedChapter.getChapterId());
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

    @Transactional(readOnly = true)
    public List<AssistantParticipationResponse> getSeriesAssistants(Long seriesId) {
        ownedSeries(seriesId);
        List<Task> tasks = taskRepository.findByChapterSeriesSeriesIdAndAssignedByEmailOrderByCreatedAtDesc(
                seriesId, currentEmail());
        return tasks.stream()
                .map(Task::getAssignedTo)
                .filter(Objects::nonNull)
                .distinct()
                .map(assistant -> toAssistantParticipationResponse(assistant, tasks))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UploadedFileResponse> getSeriesFiles(Long seriesId) {
        ownedSeries(seriesId);
        return seriesFileRepository
                .findBySeriesSeriesIdAndPurposeAndActiveTrueOrderByUploadedAtDesc(
                        seriesId,
                        SERIES_SUBMISSION_PURPOSE)
                .stream()
                .map(this::toUploadedFileResponse)
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
        validateTaskDeadline(request.dueDate());
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
        task.setOriginalFileUrl(pageAssetUrl(page.getImageUrl()));
        task.setTaskType(taskType);
        task.setAreaX(request.areaX());
        task.setAreaY(request.areaY());
        task.setAreaWidth(request.areaWidth());
        task.setAreaHeight(request.areaHeight());
        task.setDueDate(request.dueDate());
        task.setStatus("ASSIGNED");
        task.setRoundNumber(1);
        task.setCreatedAt(LocalDateTime.now());
        Task savedTask = taskRepository.save(task);
        if (hasFiles(request.originalFiles())) {
            taskFileStorageService.storeTaskFiles(
                    savedTask,
                    mangaka,
                    request.originalFiles(),
                    savedTask.getRoundNumber(),
                    TaskFileStorageService.TASK_ORIGINAL);
        }
        storeTaskMarkupPages(
                savedTask,
                savedTask.getRoundNumber(),
                request.markupImages(),
                request.markupCanvasData());
        notify(
                assistant,
                "TASK_ASSIGNED",
                savedTask.getTaskId(),
                "Bạn được giao nhiệm vụ \"" + savedTask.getTitle()
                        + "\" ở trang " + page.getPageNumber() + ".");
        return toTaskResponse(savedTask);
    }

    @Transactional
    public TaskResponse reviseTask(Long taskId, ReviseTaskRequest request) {
        Task task = ownedTask(taskId);
        if (!REVISION_REQUESTED_STATUS.equalsIgnoreCase(task.getStatus())) {
            throw conflict("Task can only be revised after the assistant submission was rejected");
        }

        int nextRound = currentRound(task) + 1;
        task.setRoundNumber(nextRound);
        task.setStatus("ASSIGNED");
        if (task.getPage() != null) {
            task.setOriginalFileUrl(pageAssetUrl(task.getPage().getImageUrl()));
        }
        Task savedTask = taskRepository.save(task);
        if (hasFiles(request.originalFiles())) {
            taskFileStorageService.storeTaskFiles(
                    savedTask,
                    currentUser(),
                    request.originalFiles(),
                    nextRound,
                    TaskFileStorageService.TASK_ORIGINAL);
        }
        storeTaskMarkupPages(
                savedTask,
                nextRound,
                request.markupImages(),
                request.markupCanvasData());
        notify(
                savedTask.getAssignedTo(),
                "TASK_REVISED",
                savedTask.getTaskId(),
                "Mangaka has sent revision round " + nextRound + " for task \"" + savedTask.getTitle() + "\".");
        return toTaskResponse(savedTask);
    }

    @Transactional(readOnly = true)
    public List<TaskMarkupPageResponse> getTaskMarkupPages(Long taskId) {
        Task task = ownedTask(taskId);
        return taskMarkupPageRepository
                .findByTaskTaskIdAndRoundNumberOrderByOrderIndexAscCreatedAtAsc(
                        taskId,
                        currentRound(task))
                .stream()
                .map(this::toTaskMarkupPageResponse)
                .toList();
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
        if (!"SUBMITTED".equalsIgnoreCase(submission.getStatus())) {
            throw conflict("Only a pending submission can be reviewed");
        }
        if (submission.getTask() != null
                && submissionRound(submission) != currentRound(submission.getTask())) {
            throw conflict("This submission belongs to an earlier task round");
        }

        User reviewer = currentUser();
        if ("APPROVED".equals(decision)) {
            replacePageWithApprovedSubmission(submission, reviewer);
        }

        submission.setStatus(decision);
        submission.setReviewNote(request.reviewNote());
        submission.setReviewedAt(LocalDateTime.now());
        submission.setReviewedBy(reviewer);
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

    private void replacePageWithApprovedSubmission(Submission submission, User reviewer) {
        Task task = submission.getTask();
        ChapterPage page = task == null ? null : task.getPage();
        if (task == null || task.getTaskId() == null || page == null || page.getPageId() == null) {
            throw conflict("Approved task submission is not linked to a chapter page");
        }

        SeriesFile approvedFile = seriesFileRepository
                .findByTaskTaskIdAndRoundNumberAndPurposeAndActiveTrueOrderByUploadedAtAsc(
                        task.getTaskId(),
                        submissionRound(submission),
                        TaskFileStorageService.TASK_SUBMISSION)
                .stream()
                .filter(this::isPageImageFile)
                .findFirst()
                .orElseThrow(() -> conflict(
                        "Approved page submission must contain at least one image file"));

        String previousImageUrl = page.getImageUrl();
        String newImageUrl = copySubmissionImageToPage(approvedFile, page, submission.getSubmissionId());

        ChapterPageHistory history = new ChapterPageHistory();
        history.setPage(page);
        history.setSubmission(submission);
        history.setApprovedBy(reviewer);
        history.setPreviousImageUrl(previousImageUrl);
        history.setNewImageUrl(newImageUrl);
        history.setCreatedAt(LocalDateTime.now());
        chapterPageHistoryRepository.save(history);

        page.setImageUrl(newImageUrl);
        page.setPageStatus("DRAWING_FINALIZED");
        chapterPageRepository.save(page);
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
                        ranking.getPeriodStart(),
                        ranking.getPeriodEnd(),
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

    private Task ownedTask(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> notFound("Task not found"));
        assertOwnedChapter(task.getChapter());
        return task;
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
                series.getDescription(), series.getStatus(),
                series.getSubmittedAt(), series.getRankingScore(),
                seriesFileRepository.findBySeriesSeriesIdAndPurposeAndActiveTrueOrderByUploadedAtDesc(
                        series.getSeriesId(),
                        SERIES_SUBMISSION_PURPOSE)
                        .stream()
                        .map(this::toUploadedFileResponse)
                        .toList());
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
                currentRound(task), task.getDueDate(), task.getAreaX(), task.getAreaY(),
                task.getAreaWidth(), task.getAreaHeight(),
                sourceFiles(task));
    }

    private AssistantResponse toAssistantResponse(User user) {
        return new AssistantResponse(
                user.getUserId(), user.getUsername(), user.getEmail(), user.getStatus(),
                user.getCreatedAt(), user.getAvatarUrl());
    }

    private AssistantParticipationResponse toAssistantParticipationResponse(User assistant, List<Task> seriesTasks) {
        List<Task> assistantTasks = seriesTasks.stream()
                .filter(task -> task.getAssignedTo() != null
                        && Objects.equals(task.getAssignedTo().getUserId(), assistant.getUserId()))
                .toList();
        return new AssistantParticipationResponse(
                assistant.getUserId(),
                assistant.getUsername(),
                assistant.getEmail(),
                assistant.getStatus(),
                assistant.getCreatedAt(),
                assistant.getAvatarUrl(),
                assistantTasks.size(),
                countTasks(assistantTasks, "ASSIGNED"),
                countTasks(assistantTasks, "IN_PROGRESS"),
                countTasks(assistantTasks, "SUBMITTED"),
                countTasks(assistantTasks, "APPROVED"),
                countTasks(assistantTasks, "REVISION_REQUESTED"));
    }

    private long countTasks(List<Task> tasks, String status) {
        return tasks.stream()
                .filter(task -> task.getStatus() != null && status.equalsIgnoreCase(task.getStatus()))
                .count();
    }

    private ChapterResponse toChapterResponse(Chapter chapter) {
        return new ChapterResponse(
                chapter.getChapterId(), chapter.getSeries().getSeriesId(), chapter.getChapterNumber(),
                chapter.getTitle(), chapterManuscriptFiles(chapter.getChapterId()),
                chapter.getStatus(), chapter.getReleaseDate(), chapter.getCreatedAt());
    }

    private ChapterRevisionNoteResponse toChapterRevisionNoteResponse(ChapterRevisionNote note) {
        Chapter chapter = note.getChapter();
        return new ChapterRevisionNoteResponse(
                note.getNoteId(),
                chapter == null ? null : chapter.getChapterId(),
                note.getImageUrl(),
                note.getDescription(),
                note.getRoundNumber(),
                note.getOrderIndex(),
                note.getCreatedAt());
    }

    private List<UploadedFileResponse> chapterManuscriptFiles(Long chapterId) {
        return seriesFileRepository
                .findByChapterChapterIdAndPurposeAndActiveTrueOrderByUploadedAtAsc(
                        chapterId,
                        CHAPTER_MANUSCRIPT_PURPOSE)
                .stream()
                .map(this::toUploadedFileResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UploadedFileResponse> getSeriesFileHistory(Long seriesId) {
        ownedSeries(seriesId);
        return seriesFileRepository
                .findBySeriesSeriesIdAndPurposeOrderByUploadedAtDesc(
                        seriesId,
                        SERIES_SUBMISSION_PURPOSE)
                .stream()
                .map(this::toUploadedFileResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UploadedFileResponse> getChapterFileHistory(Long chapterId) {
        ownedChapter(chapterId);
        return seriesFileRepository
                .findByChapterChapterIdAndPurposeOrderByUploadedAtDesc(
                        chapterId,
                        CHAPTER_MANUSCRIPT_PURPOSE)
                .stream()
                .map(this::toUploadedFileResponse)
                .toList();
    }

    private PageResponse toPageResponse(ChapterPage page) {
        return new PageResponse(
                page.getPageId(), page.getChapter().getChapterId(), page.getPageNumber(),
                page.getImageUrl(), page.getPageStatus());
    }

    private PageHistoryResponse toPageHistoryResponse(ChapterPageHistory history) {
        User approvedBy = history.getApprovedBy();
        return new PageHistoryResponse(
                history.getHistoryId(),
                history.getPage() == null ? null : history.getPage().getPageId(),
                history.getSubmission() == null ? null : history.getSubmission().getSubmissionId(),
                approvedBy == null ? null : approvedBy.getUserId(),
                approvedBy == null ? null : approvedBy.getUsername(),
                history.getPreviousImageUrl(),
                history.getNewImageUrl(),
                history.getCreatedAt());
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
                submissionRound(submission),
                submission.getReviewNote(), submission.getSubmittedAt(), submission.getReviewedAt(),
                resultFiles(submission));
    }

    private NotificationResponse toNotificationResponse(Notification notification) {
        return new NotificationResponse(
                notification.getNotificationId(), notification.getType(), notification.getReferenceId(),
                notification.getMessage(), notification.getIsRead(), notification.getCreatedAt());
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

    private void storeTaskMarkupPages(
            Task task,
            Integer roundNumber,
            List<MultipartFile> markupImages,
            String markupCanvasData) {
        List<MultipartFile> images = markupImages == null
                ? List.of()
                : markupImages.stream()
                        .filter(image -> image != null && !image.isEmpty())
                        .toList();
        List<String> canvasValues = parseMarkupCanvasData(markupCanvasData, images.size());
        if (images.isEmpty()) {
            return;
        }
        if (images.size() > MAX_SERIES_FILES_PER_SUBMISSION) {
            throw badRequest("A task revision can contain at most 20 markup images");
        }

        Path relativeFolder = Path.of(
                "task-" + task.getTaskId(),
                "round-" + roundNumber);
        Path uploadRoot = taskMarkupUploadRoot().resolve(relativeFolder).normalize();
        try {
            Files.createDirectories(uploadRoot);
        } catch (IOException exception) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Could not create task markup folder",
                    exception);
        }

        for (int index = 0; index < images.size(); index++) {
            MultipartFile image = images.get(index);
            validatePageImage(image);
            String extension = fileExtension(image.getOriginalFilename());
            String fileName = "task-" + task.getTaskId()
                    + "-round-" + roundNumber
                    + "-markup-" + (index + 1)
                    + "-" + UUID.randomUUID() + extension;
            Path target = uploadRoot.resolve(fileName).normalize();
            if (!target.startsWith(uploadRoot)) {
                throw badRequest("Invalid task markup file name");
            }

            try (InputStream inputStream = image.getInputStream()) {
                Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException exception) {
                throw new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Could not save task markup image",
                        exception);
            }

            TaskMarkupPage markupPage = new TaskMarkupPage();
            markupPage.setTask(task);
            markupPage.setRoundNumber(roundNumber);
            markupPage.setImageUrl(
                    "task-markups/" + relativeFolder.toString().replace('\\', '/') + "/" + fileName);
            markupPage.setCanvasData(canvasValues.isEmpty() ? null : canvasValues.get(index));
            markupPage.setOrderIndex(index + 1);
            markupPage.setCreatedAt(LocalDateTime.now());
            taskMarkupPageRepository.save(markupPage);
        }
    }

    private List<String> parseMarkupCanvasData(String rawCanvasData, int imageCount) {
        if (rawCanvasData == null || rawCanvasData.isBlank()) {
            return List.of();
        }
        if (imageCount == 0) {
            throw badRequest("markupCanvasData requires at least one markup image");
        }
        try {
            JsonNode root = objectMapper.readTree(rawCanvasData);
            if (root == null || !root.isArray() || root.size() != imageCount) {
                throw badRequest("markupCanvasData must be a JSON array matching the markup image count");
            }
            List<String> values = new ArrayList<>();
            root.forEach(value -> values.add(value.isTextual() ? value.asText() : value.toString()));
            return values;
        } catch (JsonProcessingException exception) {
            throw badRequest("markupCanvasData must be valid JSON");
        }
    }

    private int currentRound(Task task) {
        return task.getRoundNumber() == null ? 1 : task.getRoundNumber();
    }

    private int submissionRound(Submission submission) {
        return submission.getRoundNumber() == null ? 1 : submission.getRoundNumber();
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

    private ResponseStatusException notFound(String message) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, message);
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    private ResponseStatusException conflict(String message) {
        return new ResponseStatusException(HttpStatus.CONFLICT, message);
    }

    private void validatePeriodRange(LocalDateTime periodStart, LocalDateTime periodEnd) {
        if (periodStart == null || periodEnd == null) {
            throw badRequest("periodStart and periodEnd are required");
        }
        if (!periodStart.isBefore(periodEnd)) {
            throw badRequest("periodStart must be before periodEnd");
        }
    }

    private void validateTaskDeadline(LocalDateTime dueDate) {
        if (dueDate == null) {
            throw badRequest("Task deadline is required");
        }
        if (dueDate.isBefore(LocalDateTime.now().plusHours(MIN_TASK_DEADLINE_HOURS))) {
            throw badRequest("Task deadline must be at least 24 hours from now");
        }
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
        return countTantouEditorActiveWorkload(editor == null ? null : editor.getUserId());
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

    private List<SeriesFile> storeSeriesFiles(
            MangaSeries series,
            User uploadedBy,
            List<MultipartFile> files,
            String fileType,
            Integer roundNumber) {
        return storeSeriesFiles(series, null, uploadedBy, files, fileType, roundNumber);
    }

    private List<SeriesFile> storeChapterManuscriptFiles(
            MangaSeries series,
            Chapter chapter,
            User uploadedBy,
            List<MultipartFile> files,
            Integer roundNumber) {
        return storeSeriesFiles(
                series,
                chapter,
                uploadedBy,
                files,
                CHAPTER_MANUSCRIPT_PURPOSE,
                roundNumber);
    }

    private List<SeriesFile> storeSeriesFiles(
            MangaSeries series,
            Chapter chapter,
            User uploadedBy,
            List<MultipartFile> files,
            String fileType,
            Integer roundNumber) {
        if (files == null || files.isEmpty()) {
            return List.of();
        }

        List<MultipartFile> presentFiles = files.stream()
                .filter(file -> file != null && !file.isEmpty())
                .toList();
        if (presentFiles.isEmpty()) {
            return List.of();
        }

        presentFiles.forEach(this::validateSeriesFile);
        Path uploadRoot = seriesFileUploadRoot().resolve("series-" + series.getSeriesId()).normalize();
        try {
            Files.createDirectories(uploadRoot);
        } catch (IOException exception) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "Could not create series file folder", exception);
        }

        List<SeriesFile> savedFiles = new ArrayList<>();
        for (MultipartFile file : presentFiles) {
            String fileName = seriesFileName(series.getSeriesId(), file.getOriginalFilename());
            Path target = uploadRoot.resolve(fileName).normalize();
            if (!target.startsWith(uploadRoot)) {
                throw badRequest("Invalid series file name");
            }

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException exception) {
                throw new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR, "Could not save series file", exception);
            }

            SeriesFile seriesFile = new SeriesFile();
            seriesFile.setSeries(series);
            seriesFile.setChapter(chapter);
            seriesFile.setUploadedBy(uploadedBy);
            seriesFile.setFileName(fileName);
            seriesFile.setOriginalFileName(blankToNull(file.getOriginalFilename()));
            seriesFile.setFileUrl("series-files/series-" + series.getSeriesId() + "/" + fileName);
            seriesFile.setContentType(blankToNull(file.getContentType()));
            seriesFile.setFileSize(file.getSize());
            seriesFile.setFileType(fileType);
            seriesFile.setRoundNumber(roundNumber == null || roundNumber < 1 ? 1 : roundNumber);
            seriesFile.setPurpose(fileType);
            seriesFile.setActive(true);
            seriesFile.setUploadedAt(LocalDateTime.now());
            savedFiles.add(seriesFileRepository.save(seriesFile));
        }
        return savedFiles;
    }

    @Transactional
    public List<SeriesFile> storeSeriesWorkflowFiles(
            MangaSeries series,
            User uploadedBy,
            List<MultipartFile> files,
            String purpose,
            Integer roundNumber) {
        List<MultipartFile> presentFiles = files == null
                ? List.of()
                : files.stream()
                        .filter(file -> file != null && !file.isEmpty())
                        .toList();
        if (presentFiles.isEmpty()) {
            return List.of();
        }
        presentFiles = requireSeriesSubmissionFiles(presentFiles);
        return storeSeriesFiles(series, uploadedBy, presentFiles, purpose, roundNumber);
    }

    public int currentSeriesSubmissionRound(Long seriesId) {
        return Math.max(1, seriesFileRepository.findMaxRoundNumberBySeriesAndPurpose(
                seriesId, SERIES_SUBMISSION_PURPOSE));
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

    private void validateSeriesFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw badRequest("Series file cannot be empty");
        }

        String extension = fileExtension(file.getOriginalFilename());
        if (!SERIES_FILE_EXTENSIONS.contains(extension)) {
            throw badRequest("Only image, PDF, text, Word, or ZIP files are allowed");
        }

        long maximumSize = ".zip".equals(extension)
                ? MAX_ZIP_FILE_SIZE_BYTES
                : MAX_SERIES_FILE_SIZE_BYTES;
        if (file.getSize() > maximumSize) {
            throw new ResponseStatusException(
                    HttpStatus.PAYLOAD_TOO_LARGE,
                    ".zip".equals(extension)
                            ? "Each ZIP series file must be 100MB or smaller"
                            : "Each series file must be 20MB or smaller");
        }
    }

    private List<MultipartFile> requireSeriesSubmissionFiles(List<MultipartFile> files) {
        List<MultipartFile> presentFiles = files == null
                ? List.of()
                : files.stream()
                        .filter(file -> file != null && !file.isEmpty())
                        .toList();
        if (presentFiles.isEmpty()) {
            throw badRequest("Vui lòng đính kèm ít nhất 1 file hồ sơ");
        }
        if (presentFiles.size() > MAX_SERIES_FILES_PER_SUBMISSION) {
            throw badRequest("A series submission can contain at most 20 files");
        }
        presentFiles.forEach(this::validateSeriesFile);
        long totalSize = presentFiles.stream().mapToLong(MultipartFile::getSize).sum();
        if (totalSize > MAX_SERIES_SUBMISSION_SIZE_BYTES) {
            throw new ResponseStatusException(
                    HttpStatus.PAYLOAD_TOO_LARGE,
                    "A series submission must be 200MB or smaller in total");
        }
        return presentFiles;
    }

    private List<MultipartFile> requireChapterManuscriptFiles(List<MultipartFile> files) {
        List<MultipartFile> presentFiles = files == null
                ? List.of()
                : files.stream()
                        .filter(file -> file != null && !file.isEmpty())
                        .toList();
        if (presentFiles.isEmpty()) {
            throw badRequest("At least one chapter manuscript file is required");
        }
        if (presentFiles.size() > MAX_SERIES_FILES_PER_SUBMISSION) {
            throw badRequest("A chapter manuscript can contain at most 20 images");
        }

        presentFiles.forEach(this::validateSeriesFile);
        List<String> extensions = presentFiles.stream()
                .map(file -> fileExtension(file.getOriginalFilename()))
                .toList();
        boolean containsZip = extensions.stream().anyMatch(".zip"::equals);
        if (containsZip && (presentFiles.size() != 1 || !".zip".equals(extensions.get(0)))) {
            throw badRequest("Upload either one ZIP file or one or more image files");
        }
        if (!containsZip && extensions.stream().anyMatch(extension -> !PAGE_IMAGE_EXTENSIONS.contains(extension))) {
            throw badRequest("Chapter manuscripts only accept JPG, PNG, WEBP, GIF, or one ZIP file");
        }

        long totalSize = presentFiles.stream().mapToLong(MultipartFile::getSize).sum();
        if (totalSize > MAX_SERIES_SUBMISSION_SIZE_BYTES) {
            throw new ResponseStatusException(
                    HttpStatus.PAYLOAD_TOO_LARGE,
                    "A chapter manuscript must be 200MB or smaller in total");
        }
        return presentFiles;
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

    private String seriesFileName(Long seriesId, String originalFilename) {
        String extension = fileExtension(originalFilename);
        if (!SERIES_FILE_EXTENSIONS.contains(extension)) {
            throw badRequest("Only image, PDF, text, Word, or ZIP files are allowed");
        }
        return "series-" + seriesId + "-file-" + UUID.randomUUID() + extension;
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

    private boolean hasFiles(List<MultipartFile> files) {
        return files != null && files.stream().anyMatch(file -> file != null && !file.isEmpty());
    }

    private String pageAssetUrl(String imageUrl) {
        String value = blankToNull(imageUrl);
        if (value == null
                || value.startsWith("http://")
                || value.startsWith("https://")
                || value.startsWith("data:")
                || value.startsWith("/covers/")) {
            return value;
        }
        String normalized = value.replace('\\', '/');
        return normalized.startsWith("pages/")
                ? "/covers/" + normalized
                : normalized;
    }

    private boolean isPageImageFile(SeriesFile file) {
        if (file == null) {
            return false;
        }
        String contentType = blankToNull(file.getContentType());
        if (contentType != null
                && PAGE_IMAGE_CONTENT_TYPES.contains(contentType.toLowerCase(Locale.ROOT))) {
            return true;
        }
        String name = blankToNull(file.getOriginalFileName());
        if (name == null) {
            name = blankToNull(file.getFileName());
        }
        if (name == null) {
            return false;
        }
        String lowerName = name.toLowerCase(Locale.ROOT);
        return PAGE_IMAGE_EXTENSIONS.stream().anyMatch(lowerName::endsWith);
    }

    private String copySubmissionImageToPage(
            SeriesFile approvedFile,
            ChapterPage page,
            Long submissionId) {
        String storedUrl = blankToNull(approvedFile.getFileUrl());
        String normalizedUrl = storedUrl == null ? "" : storedUrl.replace('\\', '/');
        String prefix = "series-files/";
        if (!normalizedUrl.startsWith(prefix)) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Approved submission file path is invalid");
        }

        Path sourceRoot = seriesFileUploadRoot();
        Path source = sourceRoot.resolve(normalizedUrl.substring(prefix.length()))
                .toAbsolutePath()
                .normalize();
        if (!source.startsWith(sourceRoot) || !Files.isRegularFile(source)) {
            throw notFound("Approved submission image was not found on disk");
        }

        Chapter chapter = page.getChapter();
        if (chapter == null || chapter.getChapterId() == null) {
            throw conflict("Chapter page is not attached to a chapter");
        }
        String sourceName = blankToNull(approvedFile.getOriginalFileName());
        if (sourceName == null) {
            sourceName = approvedFile.getFileName();
        }
        String extension = fileExtension(sourceName);
        Path chapterDirectory = pageImageUploadRoot()
                .resolve("chapter-" + chapter.getChapterId())
                .normalize();
        try {
            Files.createDirectories(chapterDirectory);
            String fileName = "page-" + page.getPageNumber()
                    + "-submission-" + submissionId
                    + "-" + UUID.randomUUID() + extension;
            Path target = chapterDirectory.resolve(fileName).normalize();
            if (!target.startsWith(chapterDirectory)) {
                throw badRequest("Invalid approved page file name");
            }
            Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
            return "pages/chapter-" + chapter.getChapterId() + "/" + fileName;
        } catch (IOException exception) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Could not replace chapter page with the approved assistant file",
                    exception);
        }
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

    private Path seriesFileUploadRoot() {
        if (seriesFileUploadRootOverride != null && !seriesFileUploadRootOverride.isBlank()) {
            return Path.of(seriesFileUploadRootOverride).toAbsolutePath().normalize();
        }

        return Path.of("uploads/series-files").toAbsolutePath().normalize();
    }

    private Path taskMarkupUploadRoot() {
        if (taskMarkupUploadRootOverride != null && !taskMarkupUploadRootOverride.isBlank()) {
            return Path.of(taskMarkupUploadRootOverride).toAbsolutePath().normalize();
        }
        return Path.of("uploads/task-markups").toAbsolutePath().normalize();
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
        return getEditorWithLeastWorkloadExcluding(excludeEditorId == null ? Set.of() : Set.of(excludeEditorId));
    }

    public User getEditorWithLeastWorkloadExcluding(Set<Long> excludeEditorIds) {
        return findEditorWithLeastWorkloadExcluding(excludeEditorIds)
                .orElseThrow(() -> new RuntimeException("No active tantou editor is available."));
    }

    public Optional<User> findEditorWithLeastWorkloadExcluding(Set<Long> excludeEditorIds) {
        List<User> editors = new ArrayList<>(userRepository
                .findByRoleRoleNameAndStatusOrderByUsernameAsc("TANTOU_EDITOR", ACTIVE_STATUS));

        return selectEditorByWorkload(editors, excludeEditorIds);
    }

    public Optional<User> findEditorWithLeastWorkloadExcluding(
            Set<Long> excludeEditorIds,
            String seriesGenres) {
        Set<String> requiredGenres = normalizedTerms(seriesGenres);
        if (requiredGenres.isEmpty()) {
            return Optional.empty();
        }
        List<User> editors = new ArrayList<>(
                userRepository.findActiveTantouEditorsWithSpecialtyOrderByUsernameAsc());
        editors.removeIf(editor -> normalizedTerms(editor.getSpecialty()).stream()
                .noneMatch(requiredGenres::contains));
        return selectEditorByWorkload(editors, excludeEditorIds);
    }

    private Optional<User> selectEditorByWorkload(List<User> editors, Set<Long> excludeEditorIds) {

        if (excludeEditorIds != null && !excludeEditorIds.isEmpty()) {
            editors.removeIf(e -> excludeEditorIds.contains(e.getUserId()));
        }

        if (editors.isEmpty()) {
            return Optional.empty();
        }

        // Các trạng thái được tính là "đang có việc"
        long minWorkload = Long.MAX_VALUE;
        List<User> candidates = new ArrayList<>();

        for (User editor : editors) {
            long workload = countTantouEditorActiveWorkload(editor.getUserId());
            if (workload < minWorkload) {
                minWorkload = workload;
                candidates.clear();
                candidates.add(editor);
            } else if (workload == minWorkload) {
                candidates.add(editor);
            }
        }
        // Chọn ngẫu nhiên nếu có nhiều người cùng mức độ ưu tiên
        return Optional.of(candidates.get(ThreadLocalRandom.current().nextInt(candidates.size())));
    }

    private Set<String> normalizedTerms(String value) {
        if (value == null || value.isBlank()) {
            return Set.of();
        }
        String normalized = value.trim();
        if (normalized.startsWith("[") && normalized.endsWith("]")) {
            normalized = normalized.substring(1, normalized.length() - 1);
        }
        return Arrays.stream(normalized.split(","))
                .map(term -> term.trim().replace("\"", ""))
                .filter(term -> !term.isBlank())
                .map(term -> term.toLowerCase(Locale.ROOT))
                .collect(java.util.stream.Collectors.toSet());
    }

    public long countTantouEditorActiveWorkload(Long editorId) {
        if (editorId == null) {
            return 0L;
        }
        return mangaSeriesRepository.countByTantouEditorUserIdAndStatusIn(
                editorId,
                TANTOU_ACTIVE_WORKLOAD_STATUSES);
    }
}

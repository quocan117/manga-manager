package com.example.backend.service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Stream;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.backend.dto.TantouEditorDtos.BoardDecisionResponse;
import com.example.backend.dto.TantouEditorDtos.ChapterManuscriptResponse;
import com.example.backend.dto.TantouEditorDtos.CommentRequest;
import com.example.backend.dto.TantouEditorDtos.CommentResponse;
import com.example.backend.dto.TantouEditorDtos.DossierResponse;
import com.example.backend.dto.TantouEditorDtos.ManuscriptResponse;
import com.example.backend.dto.TantouEditorDtos.PageManuscriptResponse;
import com.example.backend.dto.TantouEditorDtos.ProgressResponse;
import com.example.backend.dto.TantouEditorDtos.ScheduleRequest;
import com.example.backend.dto.TantouEditorDtos.ScheduleResponse;
import com.example.backend.dto.TantouEditorDtos.SeriesSummaryResponse;
import com.example.backend.model.BoardDecision;
import com.example.backend.model.Chapter;
import com.example.backend.model.ChapterPage;
import com.example.backend.model.MangaSeries;
import com.example.backend.model.PublishSchedule;
import com.example.backend.model.ReviewComment;
import com.example.backend.model.Task;
import com.example.backend.model.User;
import com.example.backend.model.Notification;
import com.example.backend.repository.BoardDecisionRepository;
import com.example.backend.repository.ChapterPageRepository;
import com.example.backend.repository.ChapterRepository;
import com.example.backend.repository.MangaSeriesRepository;
import com.example.backend.repository.PublishScheduleRepository;
import com.example.backend.repository.ReviewCommentRepository;
import com.example.backend.repository.SubmissionRepository;
import com.example.backend.repository.TaskRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.repository.NotificationRepository;

@Service
public class TantouEditorService {
    private static final Set<String> COMMENT_TYPES = Set.of("CONTENT", "DIALOGUE", "SCRIPT", "OTHER");
    private static final Set<String> COMMENT_STATUSES = Set.of("OPEN", "RESOLVED");
    private static final Set<String> FINAL_PAGE_STATUSES = Set.of("DRAWING_FINALIZED", "FINALIZED", "PUBLISHED");
    private static final Set<String> DONE_TASK_STATUSES = Set.of("SUBMITTED", "APPROVED");
    private static final String TANTOU_REVIEW_STATUS = "TANTOU_REVIEW";
    private static final String BOARD_REVIEW_STATUS = "REVIEWING";
    private static final String REVISION_REQUESTED_STATUS = "REVISION_REQUESTED";

    private final MangaSeriesRepository mangaSeriesRepository;
    private final ChapterRepository chapterRepository;
    private final ChapterPageRepository pageRepository;
    private final ReviewCommentRepository commentRepository;
    private final PublishScheduleRepository scheduleRepository;
    private final BoardDecisionRepository boardDecisionRepository;
    private final TaskRepository taskRepository;
    private final SubmissionRepository submissionRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;

    public TantouEditorService(
            MangaSeriesRepository mangaSeriesRepository,
            ChapterRepository chapterRepository,
            ChapterPageRepository pageRepository,
            ReviewCommentRepository commentRepository,
            PublishScheduleRepository scheduleRepository,
            BoardDecisionRepository boardDecisionRepository,
            TaskRepository taskRepository,
            SubmissionRepository submissionRepository,
            UserRepository userRepository,
            NotificationRepository notificationRepository) {
        this.mangaSeriesRepository = mangaSeriesRepository;
        this.chapterRepository = chapterRepository;
        this.pageRepository = pageRepository;
        this.commentRepository = commentRepository;
        this.scheduleRepository = scheduleRepository;
        this.boardDecisionRepository = boardDecisionRepository;
        this.taskRepository = taskRepository;
        this.submissionRepository = submissionRepository;
        this.userRepository = userRepository;
        this.notificationRepository = notificationRepository;
    }

    @Transactional(readOnly = true)
    public List<SeriesSummaryResponse> getSeries() {
        return mangaSeriesRepository.findVisibleToTantouEditorOrderByCreatedAtDesc(currentEmail())
                .stream()
                .map(this::toSeriesSummary)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SeriesSummaryResponse> getPendingEditorialReviewSeries() {
        return mangaSeriesRepository.findVisibleToTantouEditorByStatusOrderBySubmittedAtDesc(
                currentEmail(), TANTOU_REVIEW_STATUS)
                .stream()
                .map(this::toSeriesSummary)
                .toList();
    }

    @Transactional(readOnly = true)
    public ManuscriptResponse getManuscript(Long seriesId) {
        MangaSeries series = series(seriesId);
        List<ChapterManuscriptResponse> chapters = chapterRepository
                .findBySeriesSeriesIdOrderByChapterNumberAsc(seriesId)
                .stream()
                .map(this::toChapterManuscript)
                .toList();
        return new ManuscriptResponse(
                toSeriesSummary(series),
                chapters,
                buildProgress(series),
                getSchedules(seriesId));
    }

    @Transactional(readOnly = true)
    public DossierResponse getDossier(Long seriesId) {
        MangaSeries series = series(seriesId);
        List<BoardDecisionResponse> boardDecisions = boardDecisionRepository
                .findBySeriesSeriesIdOrderByDecisionDateDesc(seriesId)
                .stream()
                .map(this::toBoardDecision)
                .toList();
        return new DossierResponse(
                toSeriesSummary(series),
                boardDecisionRepository.countBySeriesSeriesIdAndDecisionTypeIgnoreCase(seriesId, "APPROVE"),
                boardDecisionRepository.countBySeriesSeriesIdAndDecisionTypeIgnoreCase(seriesId, "REJECT"),
                boardDecisions,
                buildProgress(series),
                getSchedules(seriesId),
                commentRepository.findByPageChapterSeriesSeriesIdOrderByCreatedAtDesc(seriesId)
                        .stream()
                        .map(this::toCommentResponse)
                        .toList());
    }

    @Transactional
    public DossierResponse submitToEditorialBoard(Long seriesId, String note) {
        MangaSeries series = series(seriesId);
        if (!TANTOU_REVIEW_STATUS.equalsIgnoreCase(series.getStatus())) {
            throw conflict("Only series waiting for tantou editor review can be submitted to editorial board");
        }
        series.setStatus(BOARD_REVIEW_STATUS);
        mangaSeriesRepository.save(series);
        createSeriesLevelComment(series, note, "CONTENT", "RESOLVED");
        notify(series.getAuthor(), "SUBMITTED_TO_BOARD", seriesId,
                "Hồ sơ series \"" + series.getTitle() + "\" đã được trình lên Hội đồng Biên tập");
        return getDossier(seriesId);
    }

    @Transactional
    public DossierResponse requestRevision(Long seriesId, String note) {
        MangaSeries series = series(seriesId);
        if (!TANTOU_REVIEW_STATUS.equalsIgnoreCase(series.getStatus())) {
            throw conflict("Only series waiting for tantou editor review can be returned for revision");
        }
        series.setStatus(REVISION_REQUESTED_STATUS);
        mangaSeriesRepository.save(series);
        createSeriesLevelComment(series, note, "CONTENT", "OPEN");
        notify(series.getAuthor(), "REVISION_REQUESTED", seriesId,
                "Biên tập yêu cầu chỉnh sửa hồ sơ series \"" + series.getTitle() + "\"" + (note != null ? ": " + note : ""));
        return getDossier(seriesId);
    }

    private void notify(User user, String type, Long refId, String message) {
        if (user == null) return;
        Notification n = new Notification();
        n.setUser(user);
        n.setType(type);
        n.setReferenceId(refId);
        n.setMessage(message);
        n.setIsRead(false);
        n.setCreatedAt(LocalDateTime.now());
        notificationRepository.save(n);
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> getPageComments(Long pageId) {
        page(pageId);
        return commentRepository.findByPagePageIdOrderByCreatedAtDesc(pageId)
                .stream()
                .map(this::toCommentResponse)
                .toList();
    }

    @Transactional
    public CommentResponse createComment(Long pageId, CommentRequest request) {
        ChapterPage page = page(pageId);
        ReviewComment comment = new ReviewComment();
        comment.setPage(page);
        comment.setEditor(currentUser());
        applyCommentRequest(comment, request, "OPEN");
        comment.setCreatedAt(LocalDateTime.now());
        return toCommentResponse(commentRepository.save(comment));
    }

    @Transactional
    public CommentResponse updateComment(Long commentId, CommentRequest request) {
        ReviewComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> notFound("Comment not found"));
        applyCommentRequest(comment, request, comment.getStatus());
        return toCommentResponse(commentRepository.save(comment));
    }

    @Transactional
    public CommentResponse resolveComment(Long commentId) {
        ReviewComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> notFound("Comment not found"));
        comment.setStatus("RESOLVED");
        return toCommentResponse(commentRepository.save(comment));
    }

    @Transactional
    public void deleteComment(Long commentId) {
        ReviewComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> notFound("Comment not found"));
        commentRepository.delete(comment);
    }

    @Transactional(readOnly = true)
    public List<ScheduleResponse> getSchedules(Long seriesId) {
        if (seriesId != null) {
            series(seriesId);
            return scheduleRepository.findBySeriesSeriesIdOrderByPublishDateAsc(seriesId)
                    .stream()
                    .map(this::toScheduleResponse)
                    .toList();
        }
        return scheduleRepository.findAllByOrderByPublishDateAsc()
                .stream()
                .map(this::toScheduleResponse)
                .toList();
    }

    @Transactional
    public ScheduleResponse createSchedule(ScheduleRequest request) {
        PublishSchedule schedule = new PublishSchedule();
        applyScheduleRequest(schedule, request);
        return toScheduleResponse(scheduleRepository.save(schedule));
    }

    @Transactional
    public ScheduleResponse updateSchedule(Long scheduleId, ScheduleRequest request) {
        PublishSchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> notFound("Schedule not found"));
        applyScheduleRequest(schedule, request);
        return toScheduleResponse(scheduleRepository.save(schedule));
    }

    @Transactional
    public void deleteSchedule(Long scheduleId) {
        PublishSchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> notFound("Schedule not found"));
        scheduleRepository.delete(schedule);
    }

    @Transactional(readOnly = true)
    public List<ProgressResponse> getStudioProgress() {
        return mangaSeriesRepository.findVisibleToTantouEditorOrderByCreatedAtDesc(currentEmail())
                .stream()
                .map(this::buildProgress)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProgressResponse getSeriesProgress(Long seriesId) {
        return buildProgress(series(seriesId));
    }

    private ChapterManuscriptResponse toChapterManuscript(Chapter chapter) {
        List<PageManuscriptResponse> pages = pageRepository
                .findByChapterChapterIdOrderByPageNumberAsc(chapter.getChapterId())
                .stream()
                .map(this::toPageManuscript)
                .toList();
        return new ChapterManuscriptResponse(
                chapter.getChapterId(),
                chapter.getChapterNumber(),
                chapter.getTitle(),
                chapter.getStatus(),
                chapter.getReleaseDate(),
                pages);
    }

    private PageManuscriptResponse toPageManuscript(ChapterPage page) {
        return new PageManuscriptResponse(
                page.getPageId(),
                page.getPageNumber(),
                page.getImageUrl(),
                page.getPageStatus(),
                commentRepository.findByPagePageIdOrderByCreatedAtDesc(page.getPageId())
                        .stream()
                        .map(this::toCommentResponse)
                        .toList());
    }

    private void applyCommentRequest(ReviewComment comment, CommentRequest request, String defaultStatus) {
        comment.setCommentText(request.commentText().trim());
        comment.setCommentType(normalize(request.commentType(), COMMENT_TYPES, "Comment type", "CONTENT"));
        comment.setStatus(normalize(request.status(), COMMENT_STATUSES, "Comment status", defaultStatus));
        comment.setPositionX(request.positionX());
        comment.setPositionY(request.positionY());
    }

    private void applyScheduleRequest(PublishSchedule schedule, ScheduleRequest request) {
        schedule.setSeries(series(request.seriesId()));
        schedule.setPublishDate(request.publishDate());
        schedule.setFrequency(request.frequency().trim());
        schedule.setStatus(blankToDefault(request.status(), "PLANNED"));
    }

    private void createSeriesLevelComment(MangaSeries series, String note, String commentType, String status) {
        if (note == null || note.isBlank()) {
            return;
        }
        ChapterPage firstPage = firstPage(series.getSeriesId());
        if (firstPage == null) {
            return;
        }
        ReviewComment comment = new ReviewComment();
        comment.setPage(firstPage);
        comment.setEditor(currentUser());
        comment.setCommentText(note.trim());
        comment.setCommentType(commentType);
        comment.setStatus(status);
        comment.setCreatedAt(LocalDateTime.now());
        commentRepository.save(comment);
    }

    private ChapterPage firstPage(Long seriesId) {
        return pageRepository.findByChapterSeriesSeriesIdOrderByPageNumberAsc(seriesId)
                .stream()
                .findFirst()
                .orElse(null);
    }

    private ProgressResponse buildProgress(MangaSeries series) {
        Long seriesId = series.getSeriesId();
        List<Chapter> chapters = chapterRepository.findBySeriesSeriesIdOrderByChapterNumberAsc(seriesId);
        List<ChapterPage> pages = pageRepository.findByChapterSeriesSeriesIdOrderByPageNumberAsc(seriesId);
        List<Task> tasks = taskRepository.findByChapterSeriesSeriesIdOrderByDueDateAsc(seriesId);
        long openComments = commentRepository.findByPageChapterSeriesSeriesIdOrderByCreatedAtDesc(seriesId)
                .stream()
                .filter(comment -> comment.getStatus() == null || !"RESOLVED".equalsIgnoreCase(comment.getStatus()))
                .count();
        long finalizedPages = pages.stream()
                .filter(page -> page.getPageStatus() != null
                        && FINAL_PAGE_STATUSES.contains(page.getPageStatus().toUpperCase(Locale.ROOT)))
                .count();
        LocalDateTime now = LocalDateTime.now();
        long overdueTasks = tasks.stream()
                .filter(task -> task.getDueDate() != null && task.getDueDate().isBefore(now))
                .filter(task -> task.getStatus() == null
                        || !DONE_TASK_STATUSES.contains(task.getStatus().toUpperCase(Locale.ROOT)))
                .count();
        LocalDateTime nextDeadline = nextDeadline(
                tasks.stream().map(Task::getDueDate),
                scheduleRepository.findBySeriesSeriesIdOrderByPublishDateAsc(seriesId)
                        .stream()
                        .map(PublishSchedule::getPublishDate));
        double completionRate = pages.isEmpty() ? 0.0 : (finalizedPages * 100.0) / pages.size();

        return new ProgressResponse(
                seriesId,
                series.getTitle(),
                chapters.size(),
                pages.size(),
                finalizedPages,
                tasks.size(),
                countTasks(tasks, "ASSIGNED"),
                countTasks(tasks, "IN_PROGRESS"),
                countTasks(tasks, "SUBMITTED"),
                countTasks(tasks, "APPROVED"),
                countTasks(tasks, "REVISION_REQUESTED"),
                overdueTasks,
                openComments,
                nextDeadline,
                Math.round(completionRate * 100.0) / 100.0);
    }

    @SafeVarargs
    private LocalDateTime nextDeadline(Stream<LocalDateTime>... streams) {
        LocalDateTime now = LocalDateTime.now();
        return Stream.of(streams)
                .flatMap(stream -> stream)
                .filter(date -> date != null && !date.isBefore(now))
                .min(Comparator.naturalOrder())
                .orElse(null);
    }

    private long countTasks(List<Task> tasks, String status) {
        return tasks.stream()
                .filter(task -> task.getStatus() != null && status.equalsIgnoreCase(task.getStatus()))
                .count();
    }

    private SeriesSummaryResponse toSeriesSummary(MangaSeries series) {
        User author = series.getAuthor();
        return new SeriesSummaryResponse(
                series.getSeriesId(),
                series.getTitle(),
                author == null ? null : author.getUsername(),
                parseGenres(series.getGenre()),
                series.getCoverImage(),
                series.getDescription(),
                series.getStatus(),
                series.getCreatedAt(),
                series.getSubmittedAt());
    }

    private CommentResponse toCommentResponse(ReviewComment comment) {
        User editor = comment.getEditor();
        ChapterPage page = comment.getPage();
        return new CommentResponse(
                comment.getCommentId(),
                page == null ? null : page.getPageId(),
                editor == null ? null : editor.getUserId(),
                editor == null ? null : editor.getUsername(),
                comment.getCommentText(),
                comment.getCommentType(),
                comment.getStatus(),
                comment.getPositionX(),
                comment.getPositionY(),
                comment.getCreatedAt());
    }

    private ScheduleResponse toScheduleResponse(PublishSchedule schedule) {
        MangaSeries series = schedule.getSeries();
        return new ScheduleResponse(
                schedule.getScheduleId(),
                series == null ? null : series.getSeriesId(),
                series == null ? null : series.getTitle(),
                schedule.getPublishDate(),
                schedule.getFrequency(),
                schedule.getStatus());
    }

    private BoardDecisionResponse toBoardDecision(BoardDecision decision) {
        User boardMember = decision.getBoardMember();
        return new BoardDecisionResponse(
                decision.getDecisionId(),
                boardMember == null ? null : boardMember.getUserId(),
                boardMember == null ? null : boardMember.getUsername(),
                decision.getDecisionType(),
                decision.getReason(),
                decision.getDecisionDate());
    }

    private MangaSeries series(Long seriesId) {
        MangaSeries series = mangaSeriesRepository.findById(seriesId)
                .orElseThrow(() -> notFound("Manga series not found"));
        assertVisibleToCurrentEditor(series);
        return series;
    }

    private ChapterPage page(Long pageId) {
        ChapterPage page = pageRepository.findById(pageId)
                .orElseThrow(() -> notFound("Chapter page not found"));
        Chapter chapter = page.getChapter();
        if (chapter != null) {
            assertVisibleToCurrentEditor(chapter.getSeries());
        }
        return page;
    }

    private void assertVisibleToCurrentEditor(MangaSeries series) {
        User assignedEditor = series == null ? null : series.getTantouEditor();
        if (assignedEditor != null && !currentEmail().equals(assignedEditor.getEmail())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not assigned to this manga series");
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

    private String normalize(String value, Set<String> allowedValues, String label, String defaultValue) {
        String normalized = blankToDefault(value, defaultValue).toUpperCase(Locale.ROOT);
        if (!allowedValues.contains(normalized)) {
            throw badRequest(label + " must be one of " + String.join(", ", allowedValues));
        }
        return normalized;
    }

    private String blankToDefault(String value, String defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return value.trim();
    }

    private List<String> parseGenres(String genre) {
        if (genre == null || genre.isBlank()) {
            return List.of();
        }
        return Arrays.stream(genre.split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .toList();
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

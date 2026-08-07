package com.example.backend.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.example.backend.dto.ChapterRevisionNoteResponse;
import com.example.backend.dto.MangakaDtos.NotificationResponse;
import com.example.backend.dto.MangakaDtos.PageHistoryResponse;
import com.example.backend.dto.TantouEditorDtos.BoardDecisionResponse;
import com.example.backend.dto.TantouEditorDtos.ChapterManuscriptResponse;
import com.example.backend.dto.TantouEditorDtos.CommentRequest;
import com.example.backend.dto.TantouEditorDtos.CommentResponse;
import com.example.backend.dto.TantouEditorDtos.DossierResponse;
import com.example.backend.dto.TantouEditorDtos.ManuscriptResponse;
import com.example.backend.dto.TantouEditorDtos.PageManuscriptResponse;
import com.example.backend.dto.TantouEditorDtos.ProgressResponse;
import com.example.backend.dto.TantouEditorDtos.SeriesSummaryResponse;
import com.example.backend.dto.MangakaDtos.UploadedFileResponse;
import com.example.backend.model.BoardDecision;
import com.example.backend.model.Chapter;
import com.example.backend.model.ChapterPage;
import com.example.backend.model.ChapterPageHistory;
import com.example.backend.model.ChapterRevisionNote;
import com.example.backend.model.MangaSeries;
import com.example.backend.model.PublishSchedule;
import com.example.backend.model.ReviewComment;
import com.example.backend.model.SeriesEditorRejection;
import com.example.backend.model.SeriesFile;
import com.example.backend.model.Task;
import com.example.backend.model.User;
import com.example.backend.model.Notification;
import com.example.backend.repository.BoardDecisionRepository;
import com.example.backend.repository.ChapterPageRepository;
import com.example.backend.repository.ChapterPageHistoryRepository;
import com.example.backend.repository.ChapterRepository;
import com.example.backend.repository.ChapterRevisionNoteRepository;
import com.example.backend.repository.MangaSeriesRepository;
import com.example.backend.repository.PublishScheduleRepository;
import com.example.backend.repository.ReviewCommentRepository;
import com.example.backend.repository.SeriesEditorRejectionRepository;
import com.example.backend.repository.SeriesFileRepository;
import com.example.backend.repository.SubmissionRepository;
import com.example.backend.repository.TaskRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.repository.NotificationRepository;
import com.example.backend.repository.SeriesBoardAssignmentRepository;

@Service
public class TantouEditorService {
    private static final Set<String> COMMENT_TYPES = Set.of("CONTENT", "DIALOGUE", "SCRIPT", "OTHER");
    private static final Set<String> COMMENT_STATUSES = Set.of("OPEN", "RESOLVED", "DELETED");
    private static final Set<String> FINAL_PAGE_STATUSES = Set.of("DRAWING_FINALIZED", "FINALIZED", "PUBLISHED");
    private static final Set<String> DONE_TASK_STATUSES = Set.of("SUBMITTED", "APPROVED");
    private static final String TANTOU_REVIEW_STATUS = "TANTOU_REVIEW";
    private static final String BOARD_REVIEW_STATUS = "REVIEWING";
    private static final String REVISION_REQUESTED_STATUS = "REVISION_REQUESTED";
    private static final String SUBMITTED_TO_EDITOR_STATUS = "SUBMITTED_TO_EDITOR";
    private static final String APPROVED_CHAPTER_STATUS = "APPROVED";
    private static final String EDITOR_ASSIGNMENT_REQUIRED_STATUS = "EDITOR_ASSIGNMENT_REQUIRED";
    private static final String DROP_REQUESTED_STATUS = "DROP_REQUESTED";
    private static final String SERIES_SUBMISSION_PURPOSE = "SERIES_SUBMISSION";
    private static final String EDITOR_DOSSIER_PURPOSE = "EDITOR_DOSSIER";
    private static final String CHAPTER_MANUSCRIPT_PURPOSE = "CHAPTER_MANUSCRIPT";
    private static final int MAX_AUTOMATIC_EDITOR_REJECTIONS = 3;
    private static final long MAX_REVISION_NOTE_IMAGE_SIZE_BYTES = 5L * 1024 * 1024;
    private static final Set<String> REVISION_NOTE_IMAGE_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp", "image/gif");
    private static final Set<String> REVISION_NOTE_IMAGE_EXTENSIONS = Set.of(
            ".jpg", ".jpeg", ".png", ".webp", ".gif");

    private final MangaSeriesRepository mangaSeriesRepository;
    private final ChapterRepository chapterRepository;
    private final ChapterRevisionNoteRepository chapterRevisionNoteRepository;
    private final ChapterPageRepository pageRepository;
    private final ChapterPageHistoryRepository pageHistoryRepository;
    private final ReviewCommentRepository commentRepository;
    private final PublishScheduleRepository scheduleRepository;
    private final BoardDecisionRepository boardDecisionRepository;
    private final TaskRepository taskRepository;
    private final SubmissionRepository submissionRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final SeriesFileRepository seriesFileRepository;
    private final SeriesEditorRejectionRepository seriesEditorRejectionRepository;
    private final MangakaService mangakaService;
    private final EditorialBoardService editorialBoardService;
    private final SeriesHistoryService seriesHistoryService;
    private final SeriesBoardAssignmentRepository seriesBoardAssignmentRepository;

    @Value("${manga.upload.chapter-revision-note-root:}")
    private String chapterRevisionNoteUploadRootOverride;

    public TantouEditorService(
            MangaSeriesRepository mangaSeriesRepository,
            ChapterRepository chapterRepository,
            ChapterRevisionNoteRepository chapterRevisionNoteRepository,
            ChapterPageRepository pageRepository,
            ChapterPageHistoryRepository pageHistoryRepository,
            ReviewCommentRepository commentRepository,
            PublishScheduleRepository scheduleRepository,
            BoardDecisionRepository boardDecisionRepository,
            TaskRepository taskRepository,
            SubmissionRepository submissionRepository,
            UserRepository userRepository,
            NotificationRepository notificationRepository,
            SeriesFileRepository seriesFileRepository,
            SeriesEditorRejectionRepository seriesEditorRejectionRepository,
            SeriesBoardAssignmentRepository seriesBoardAssignmentRepository,
            MangakaService mangakaService,
            EditorialBoardService editorialBoardService,
            SeriesHistoryService seriesHistoryService) {
        this.mangaSeriesRepository = mangaSeriesRepository;
        this.chapterRepository = chapterRepository;
        this.chapterRevisionNoteRepository = chapterRevisionNoteRepository;
        this.pageRepository = pageRepository;
        this.pageHistoryRepository = pageHistoryRepository;
        this.commentRepository = commentRepository;
        this.scheduleRepository = scheduleRepository;
        this.boardDecisionRepository = boardDecisionRepository;
        this.taskRepository = taskRepository;
        this.submissionRepository = submissionRepository;
        this.userRepository = userRepository;
        this.notificationRepository = notificationRepository;
        this.seriesFileRepository = seriesFileRepository;
        this.seriesEditorRejectionRepository = seriesEditorRejectionRepository;
        this.mangakaService = mangakaService;
        this.editorialBoardService = editorialBoardService;
        this.seriesHistoryService = seriesHistoryService;
        this.seriesBoardAssignmentRepository = seriesBoardAssignmentRepository;
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
                buildProgress(series));
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
                commentRepository.findByPageChapterSeriesSeriesIdOrderByCreatedAtDesc(seriesId)
                        .stream()
                        .map(this::toCommentResponse)
                        .toList());
    }

    @Transactional
    public DossierResponse submitToEditorialBoard(Long seriesId, String note) {
        return submitToEditorialBoard(seriesId, note, List.of());
    }

    @Transactional
    public DossierResponse submitToEditorialBoard(
            Long seriesId,
            String note,
            List<MultipartFile> files) {
        MangaSeries series = series(seriesId);
        if (!TANTOU_REVIEW_STATUS.equalsIgnoreCase(series.getStatus())) {
            throw conflict("Only series waiting for tantou editor review can be submitted to editorial board");
        }
        User editor = currentUser();
        mangakaService.storeSeriesWorkflowFiles(
                series,
                editor,
                files,
                EDITOR_DOSSIER_PURPOSE,
                mangakaService.currentSeriesSubmissionRound(seriesId));
        boardDecisionRepository.deleteBySeriesSeriesId(seriesId);
        String previousStatus = series.getStatus();
        series.setStatus(BOARD_REVIEW_STATUS);
        mangaSeriesRepository.save(series);
        seriesHistoryService.record(
                series,
                editor,
                "EDITOR_SUBMITTED_TO_BOARD",
                previousStatus,
                series.getStatus(),
                note,
                seriesId);
        editorialBoardService.assignBoardPanel(series);
        seriesBoardAssignmentRepository
                .findBySeriesSeriesIdOrderByAssignedAtAsc(series.getSeriesId())
                .forEach(assignment -> {
                    notify(
                            assignment.getBoardMember(),
                            "SERIES_SUBMITTED_TO_BOARD",
                            series.getSeriesId(),
                            "Biên tập viên " + editor.getUsername()
                                    + " đã trình hồ sơ bảo vệ cho dự án \"" + series.getTitle() + "\"."
                    );
                });
        return getDossier(seriesId);
    }

    @Transactional
    public DossierResponse requestSeriesDrop(Long seriesId, String reason) {
        String requiredReason = blankToNull(reason);
        if (requiredReason == null) {
            throw badRequest("Drop request reason is required");
        }
        MangaSeries series = series(seriesId);
        if (!TANTOU_REVIEW_STATUS.equalsIgnoreCase(series.getStatus())) {
            throw conflict("Only a series under tantou review can request cancellation");
        }
        User editor = currentUser();
        String previousStatus = series.getStatus();
        series.setStatus(DROP_REQUESTED_STATUS);
        mangaSeriesRepository.save(series);
        seriesHistoryService.record(
                series,
                editor,
                "EDITOR_REQUESTED_SERIES_DROP",
                previousStatus,
                series.getStatus(),
                requiredReason,
                seriesId);
        editorialBoardService.assignBoardPanel(series);
        notify(series.getAuthor(), "SERIES_DROP_REQUESTED", seriesId,
                "The tantou editor requested cancellation review for series \""
                        + series.getTitle() + "\".");
        notifyEditorialBoard("SERIES_DROP_REQUESTED", seriesId,
                "A cancellation request requires review for series \""
                        + series.getTitle() + "\". Reason: " + requiredReason);
        return getDossier(seriesId);
    }

    @Transactional
    public DossierResponse requestRevision(Long seriesId, String note) {
        MangaSeries series = series(seriesId);
        if (!TANTOU_REVIEW_STATUS.equalsIgnoreCase(series.getStatus())) {
            throw conflict("Only series waiting for tantou editor review can be returned for revision");
        }
        String previousStatus = series.getStatus();
        series.setStatus(REVISION_REQUESTED_STATUS);
        mangaSeriesRepository.save(series);
        seriesHistoryService.record(
                series,
                currentUser(),
                "EDITOR_REQUESTED_REVISION",
                previousStatus,
                series.getStatus(),
                note,
                seriesId);
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
        comment.setStatus("DELETED");
        commentRepository.save(comment);
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

    @Transactional(readOnly = true)
    public List<ChapterManuscriptResponse> getPendingChapterReviews() {
        return chapterRepository.findBySeriesTantouEditorEmailAndStatusIgnoreCaseOrderByCreatedAtDesc(
                        currentEmail(), SUBMITTED_TO_EDITOR_STATUS)
                .stream()
                .map(this::toChapterManuscript)
                .toList();
    }

    @Transactional(readOnly = true)
    public ChapterManuscriptResponse getChapter(Long chapterId) {
        return toChapterManuscript(chapterForCurrentEditor(chapterId));
    }

    @Transactional(readOnly = true)
    public List<ChapterRevisionNoteResponse> getChapterRevisionNotes(Long chapterId) {
        return chapterRevisionNoteRepository.findByChapterChapterIdOrderByOrderIndexAscCreatedAtAsc(chapterId)
                .stream()
                .map(note -> new ChapterRevisionNoteResponse(
                        note.getNoteId(),
                        note.getChapter().getChapterId(),
                        note.getImageUrl(),
                        note.getDescription(),
                        note.getRoundNumber(),
                        note.getOrderIndex(),
                        note.getCreatedAt()))
                .toList();
    }

    @Transactional
    public ChapterRevisionNoteResponse createChapterRevisionNote(
            Long chapterId,
            MultipartFile image,
            String canvasData,
            String description,
            Integer orderIndex) {
        Chapter chapter = chapterForCurrentEditor(chapterId);
        validateRevisionNoteImage(image);
        String requiredDescription = blankToNull(description);
        if (requiredDescription == null) {
            throw badRequest("description is required");
        }
        if (orderIndex == null || orderIndex < 0) {
            throw badRequest("orderIndex must be zero or greater");
        }

        ChapterRevisionNote note = new ChapterRevisionNote();
        note.setChapter(chapter);
        note.setEditor(currentUser());
        note.setImageUrl(storeChapterRevisionNoteImage(chapterId, orderIndex, image));
        note.setCanvasData(blankToNull(canvasData));
        note.setDescription(requiredDescription);
        note.setRoundNumber(Math.max(
                1,
                seriesFileRepository.findMaxRoundNumberByChapterAndPurpose(
                        chapterId, CHAPTER_MANUSCRIPT_PURPOSE)));
        note.setOrderIndex(orderIndex);
        note.setCreatedAt(LocalDateTime.now());
        return toChapterRevisionNoteResponse(chapterRevisionNoteRepository.save(note));
    }

    @Transactional
    public ChapterManuscriptResponse requestChapterRevision(Long chapterId) {
        Chapter chapter = chapterForCurrentEditor(chapterId);
        if (!SUBMITTED_TO_EDITOR_STATUS.equalsIgnoreCase(chapter.getStatus())) {
            throw badRequest("Only submitted chapters can be returned for revision");
        }
        String previousStatus = chapter.getStatus();
        chapter.setStatus(REVISION_REQUESTED_STATUS);
        Chapter savedChapter = chapterRepository.save(chapter);
        MangaSeries series = savedChapter.getSeries();
        seriesHistoryService.record(
                series,
                currentUser(),
                "EDITOR_REQUESTED_CHAPTER_REVISION",
                previousStatus,
                savedChapter.getStatus(),
                savedChapter.getTitle(),
                savedChapter.getChapterId());
        notify(series == null ? null : series.getAuthor(), "CHAPTER_REVISION_REQUESTED", savedChapter.getChapterId(),
                "Chapter revision requested: " + savedChapter.getTitle());
        return toChapterManuscript(savedChapter);
    }

    @Transactional
    public ChapterManuscriptResponse approveAndReadyChapter(Long chapterId) {
        Chapter chapter = chapterForCurrentEditor(chapterId);
        if (!SUBMITTED_TO_EDITOR_STATUS.equalsIgnoreCase(chapter.getStatus())) {
            throw badRequest("Only chapters submitted to the Tantou Editor can be approved");
        }
        MangaSeries series = chapter.getSeries();
        if (series == null || series.getSeriesId() == null) {
            throw conflict("Chapter is not attached to a series");
        }

        String previousStatus = chapter.getStatus();
        chapter.setStatus(APPROVED_CHAPTER_STATUS);
        Chapter savedChapter = chapterRepository.save(chapter);
        seriesHistoryService.record(
                series,
                currentUser(),
                "EDITOR_APPROVED_CHAPTER",
                previousStatus,
                savedChapter.getStatus(),
                savedChapter.getTitle(),
                savedChapter.getChapterId());
        notify(series.getAuthor(), "CHAPTER_APPROVED", chapterId,
                "\"" + chapter.getTitle() + "\" was approved by the Tantou Editor.");
        notify(series.getPublicationCoordinator(), "CHAPTER_READY_FOR_SCHEDULE", chapterId,
                "\"" + chapter.getTitle() + "\" is ready for its publication schedule.");
        return toChapterManuscript(savedChapter);
    }

    private static final List<String> ASSIGNMENT_NOTIFICATION_TYPES =
            Arrays.asList("NEW_ASSIGNMENT", "SYSTEM_ASSIGNMENT", "FORCED_EDITOR_ASSIGNMENT");

    @Transactional
    public void acceptSeries(Long seriesId) {
        MangaSeries series = mangaSeriesRepository.findById(seriesId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy Series"));
        String email = currentEmail();

        if (!"PENDING_EDITOR".equalsIgnoreCase(series.getStatus())) {
            dismissAssignmentNotifications(seriesId, email);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Hồ sơ không ở trạng thái chờ xác nhận.");
        }
        if (series.getTantouEditor() == null
                || !series.getTantouEditor().getEmail().equalsIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Hồ sơ này không được giao cho bạn.");
        }

        String previousStatus = series.getStatus();
        series.setStatus("TANTOU_REVIEW");
        mangaSeriesRepository.save(series);
        seriesHistoryService.record(
                series,
                series.getTantouEditor(),
                "EDITOR_ACCEPTED_SERIES",
                previousStatus,
                series.getStatus(),
                null,
                seriesId);
        dismissAssignmentNotifications(seriesId, email);

        Notification notif = new Notification();
        notif.setUser(series.getAuthor());
        notif.setType("SYSTEM");
        notif.setMessage("Biên tập viên " + series.getTantouEditor().getUsername()
                + " đã bắt đầu kiểm tra hồ sơ series '" + series.getTitle() + "'.");
        notif.setCreatedAt(LocalDateTime.now());
        notif.setIsRead(false);
        notificationRepository.save(notif);
    }

    @Transactional
    public void rejectSeries(Long seriesId, String reason) {
        String requiredReason = blankToNull(reason);
        if (requiredReason == null) {
            throw badRequest("Vui lòng nhập lý do từ chối");
        }

        MangaSeries series = mangaSeriesRepository.findById(seriesId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy Series"));
        String email = currentEmail();

        if (Boolean.TRUE.equals(series.getEditorAssignmentLocked())) {
            dismissAssignmentNotifications(seriesId, email);
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "This series was assigned by Editorial Board and cannot be rejected");
        }
        if (!"PENDING_EDITOR".equalsIgnoreCase(series.getStatus())) {
            dismissAssignmentNotifications(seriesId, email);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Hồ sơ không ở trạng thái chờ xác nhận.");
        }
        if (series.getTantouEditor() == null
                || !series.getTantouEditor().getEmail().equalsIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Hồ sơ này không được giao cho bạn.");
        }

        User oldEditor = userRepository.findByIdForUpdate(series.getTantouEditor().getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Editor not found"));
        dismissAssignmentNotifications(seriesId, email);
        recordEditorRejection(series, oldEditor, requiredReason);
        Set<Long> rejectedEditorIds = rejectedEditorIds(seriesId);
        long rejectionCount = seriesEditorRejectionRepository.countBySeriesSeriesId(seriesId);

        if (rejectionCount >= MAX_AUTOMATIC_EDITOR_REJECTIONS) {
            moveToBoardAssignmentRequired(series, oldEditor, requiredReason);
            return;
        }

        List<User> activeEditors = userRepository.findByRoleRoleNameAndStatusOrderByUsernameAsc(
                "TANTOU_EDITOR", "ACTIVE");
        boolean allEditorsRejected = activeEditors.stream()
                .filter(editor -> editor.getUserId() != null)
                .allMatch(editor -> rejectedEditorIds.contains(editor.getUserId()));

        if (allEditorsRejected) {
            moveToBoardAssignmentRequired(series, oldEditor, requiredReason);
            return;
        }

        Optional<User> newEditorCandidate = mangakaService
                .findEditorWithLeastWorkloadExcluding(rejectedEditorIds, series.getGenre());
        if (newEditorCandidate.isEmpty()) {
            moveToBoardAssignmentRequired(series, oldEditor, requiredReason);
            return;
        }
        User newEditor = newEditorCandidate.get();

        series.setTantouEditor(newEditor);
        series.setEditorAssignedAt(LocalDateTime.now());
        mangaSeriesRepository.save(series);
        seriesHistoryService.record(
                series,
                oldEditor,
                "EDITOR_REJECTED_SERIES",
                "PENDING_EDITOR",
                series.getStatus(),
                requiredReason,
                seriesId);

        Notification toNewEditor = new Notification();
        toNewEditor.setUser(newEditor);
        toNewEditor.setType("SYSTEM_ASSIGNMENT");
        toNewEditor.setReferenceId(series.getSeriesId());
        toNewEditor.setMessage("Mangaka " + series.getAuthor().getUsername()
                + " vừa gửi hồ sơ series '" + series.getTitle()
                + "'. Vui lòng nhấn Nhận hồ sơ trong vòng 24h.");
        toNewEditor.setCreatedAt(LocalDateTime.now());
        toNewEditor.setIsRead(false);
        notificationRepository.save(toNewEditor);

        Notification toAuthor = new Notification();
        toAuthor.setUser(series.getAuthor());
        toAuthor.setType("SYSTEM");
        toAuthor.setReferenceId(series.getSeriesId());
        toAuthor.setMessage("Hồ sơ series '" + series.getTitle()
                + "' đã được chuyển sang một Biên tập viên phù hợp để tiếp tục kiểm tra.");
        toAuthor.setCreatedAt(LocalDateTime.now());
        toAuthor.setIsRead(false);
        notificationRepository.save(toAuthor);
    }

    private void moveToBoardAssignmentRequired(MangaSeries series, User rejectedBy, String reason) {
        String previousStatus = series.getStatus();
        series.setStatus(EDITOR_ASSIGNMENT_REQUIRED_STATUS);
        series.setTantouEditor(null);
        series.setEditorAssignmentLocked(false);
        series.setEditorAssignedAt(null);
        mangaSeriesRepository.save(series);
        seriesHistoryService.record(
                series,
                rejectedBy,
                "EDITOR_REJECTED_SERIES",
                previousStatus,
                series.getStatus(),
                reason,
                series.getSeriesId());
        notify(series.getAuthor(), "EDITOR_ASSIGNMENT_REQUIRED", series.getSeriesId(),
                "All eligible tantou editors rejected or are unavailable for series \""
                        + series.getTitle() + "\". Editorial Board will assign an editor directly.");
        notifyEditorialBoard("EDITOR_ASSIGNMENT_REQUIRED", series.getSeriesId(),
                "Series \"" + series.getTitle()
                        + "\" needs a forced tantou editor assignment. Review the rejection list before assigning.");
    }

    private void recordEditorRejection(MangaSeries series, User editor, String reason) {
        if (series == null || series.getSeriesId() == null || editor == null || editor.getUserId() == null) {
            return;
        }
        SeriesEditorRejection rejection = seriesEditorRejectionRepository
                .findBySeriesSeriesIdAndEditorUserId(series.getSeriesId(), editor.getUserId())
                .orElseGet(SeriesEditorRejection::new);
        rejection.setSeries(series);
        rejection.setEditor(editor);
        rejection.setReason(reason);
        rejection.setRejectedAt(LocalDateTime.now());
        seriesEditorRejectionRepository.save(rejection);
    }

    private Set<Long> rejectedEditorIds(Long seriesId) {
        return seriesEditorRejectionRepository.findBySeriesSeriesId(seriesId)
                .stream()
                .map(SeriesEditorRejection::getEditor)
                .filter(editor -> editor != null && editor.getUserId() != null)
                .map(User::getUserId)
                .collect(java.util.stream.Collectors.toSet());
    }

    private void notifyEditorialBoard(String type, Long referenceId, String message) {
        userRepository.findByRoleRoleNameAndStatusOrderByUsernameAsc("EDITORIAL_BOARD", "ACTIVE")
                .forEach(board -> notify(board, type, referenceId, message));
    }

    private void dismissAssignmentNotifications(Long seriesId, String editorEmail) {
        User editor = userRepository.findByEmail(editorEmail).orElse(null);
        if (editor == null) return;
        List<Notification> stale = notificationRepository
                .findByReferenceIdAndUserUserIdAndTypeInAndIsReadFalse(
                        seriesId, editor.getUserId(), ASSIGNMENT_NOTIFICATION_TYPES);
        stale.forEach(n -> n.setIsRead(true));
        notificationRepository.saveAll(stale);
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
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found"));
        notification.setIsRead(true);
        return toNotificationResponse(notificationRepository.save(notification));
    }

    private NotificationResponse toNotificationResponse(Notification notification) {
        return new NotificationResponse(
                notification.getNotificationId(), notification.getType(), notification.getReferenceId(),
                notification.getMessage(), notification.getIsRead(), notification.getCreatedAt());
    }

    private ChapterManuscriptResponse toChapterManuscript(Chapter chapter) {
        List<PageManuscriptResponse> pages = pageRepository
                .findByChapterChapterIdOrderByPageNumberAsc(chapter.getChapterId())
                .stream()
                .map(this::toPageManuscript)
                .toList();
        MangaSeries series = chapter.getSeries();
        return new ChapterManuscriptResponse(
                chapter.getChapterId(),
                chapter.getChapterNumber(),
                chapter.getTitle(),
                series == null ? null : series.getSeriesId(),
                series == null ? null : series.getTitle(),
                chapterManuscriptFiles(chapter.getChapterId()),
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
                        .toList(),
                pageHistoryRepository.findByPagePageIdOrderByCreatedAtDesc(page.getPageId())
                        .stream()
                        .map(this::toPageHistoryResponse)
                        .toList());
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

    private void applyCommentRequest(ReviewComment comment, CommentRequest request, String defaultStatus) {
        comment.setCommentText(request.commentText().trim());
        comment.setCommentType(normalize(request.commentType(), COMMENT_TYPES, "Comment type", "CONTENT"));
        comment.setStatus(normalize(request.status(), COMMENT_STATUSES, "Comment status", defaultStatus));
        comment.setPositionX(request.positionX());
        comment.setPositionY(request.positionY());
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
                .filter(comment -> comment.getStatus() == null
                        || (!"RESOLVED".equalsIgnoreCase(comment.getStatus())
                        && !"DELETED".equalsIgnoreCase(comment.getStatus())))
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
                series.getStatus(),
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
                series.getSubmittedAt(),
                seriesFileRepository.findBySeriesSeriesIdAndPurposeAndActiveTrueOrderByUploadedAtDesc(
                                series.getSeriesId(),
                                SERIES_SUBMISSION_PURPOSE)
                        .stream()
                        .map(this::toUploadedFileResponse)
                        .toList());
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

    private MangaSeries series(Long seriesId) {
        MangaSeries series = mangaSeriesRepository.findById(seriesId)
                .orElseThrow(() -> notFound("Manga series not found"));
        assertVisibleToCurrentEditor(series);
        return series;
    }

    private Chapter chapterForCurrentEditor(Long chapterId) {
        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> notFound("Chapter not found"));
        MangaSeries series = chapter.getSeries();
        User editor = series == null ? null : series.getTantouEditor();
        if (editor == null || !currentEmail().equalsIgnoreCase(editor.getEmail())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not assigned to this chapter");
        }
        return chapter;
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

    private String storeChapterRevisionNoteImage(Long chapterId, Integer orderIndex, MultipartFile image) {
        Path uploadRoot = chapterRevisionNoteUploadRoot().resolve("chapter-" + chapterId).normalize();
        try {
            Files.createDirectories(uploadRoot);
        } catch (IOException exception) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "Could not create chapter revision note folder", exception);
        }

        String fileName = chapterRevisionNoteImageFileName(chapterId, orderIndex, image.getOriginalFilename());
        Path target = uploadRoot.resolve(fileName).normalize();
        if (!target.startsWith(uploadRoot)) {
            throw badRequest("Invalid image file name");
        }

        try (InputStream inputStream = image.getInputStream()) {
            Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException exception) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "Could not save chapter revision note image", exception);
        }

        return "chapter-revision-notes/chapter-" + chapterId + "/" + fileName;
    }

    private void validateRevisionNoteImage(MultipartFile image) {
        if (image == null || image.isEmpty()) {
            throw badRequest("Image file cannot be empty");
        }
        if (image.getSize() > MAX_REVISION_NOTE_IMAGE_SIZE_BYTES) {
            throw new ResponseStatusException(
                    HttpStatus.PAYLOAD_TOO_LARGE, "Revision note image must be 5MB or smaller");
        }

        String contentType = image.getContentType();
        if (contentType == null
                || !REVISION_NOTE_IMAGE_CONTENT_TYPES.contains(contentType.toLowerCase(Locale.ROOT))) {
            throw badRequest("Only JPG, PNG, WEBP, or GIF images are allowed");
        }
    }

    private String chapterRevisionNoteImageFileName(Long chapterId, Integer orderIndex, String originalFilename) {
        String extension = fileExtension(originalFilename);
        if (!REVISION_NOTE_IMAGE_EXTENSIONS.contains(extension)) {
            throw badRequest("Only JPG, PNG, WEBP, or GIF images are allowed");
        }
        String indexPart = orderIndex == null ? "unindexed" : orderIndex.toString();
        return "chapter-" + chapterId + "-revision-" + indexPart + "-" + UUID.randomUUID() + extension;
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

    private Path chapterRevisionNoteUploadRoot() {
        if (chapterRevisionNoteUploadRootOverride != null && !chapterRevisionNoteUploadRootOverride.isBlank()) {
            return Path.of(chapterRevisionNoteUploadRootOverride).toAbsolutePath().normalize();
        }

        return Path.of("uploads/chapter-revision-notes").toAbsolutePath().normalize();
    }

    private void assertVisibleToCurrentEditor(MangaSeries series) {
        User assignedEditor = series == null ? null : series.getTantouEditor();
        if (assignedEditor == null || !currentEmail().equalsIgnoreCase(assignedEditor.getEmail())) {
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

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
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

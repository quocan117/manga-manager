package com.example.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import com.example.backend.dto.TantouEditorDtos.CommentRequest;
import com.example.backend.model.Chapter;
import com.example.backend.model.ChapterPage;
import com.example.backend.model.ChapterRevisionNote;
import com.example.backend.model.MangaSeries;
import com.example.backend.model.Notification;
import com.example.backend.model.PublishSchedule;
import com.example.backend.model.ReviewComment;
import com.example.backend.model.SeriesEditorRejection;
import com.example.backend.model.SeriesFile;
import com.example.backend.model.Task;
import com.example.backend.model.User;
import com.example.backend.repository.BoardDecisionRepository;
import com.example.backend.repository.ChapterPageRepository;
import com.example.backend.repository.ChapterPageHistoryRepository;
import com.example.backend.repository.ChapterRepository;
import com.example.backend.repository.ChapterRevisionNoteRepository;
import com.example.backend.repository.MangaSeriesRepository;
import com.example.backend.repository.NotificationRepository;
import com.example.backend.repository.PublishScheduleRepository;
import com.example.backend.repository.ReviewCommentRepository;
import com.example.backend.repository.SeriesBoardAssignmentRepository;
import com.example.backend.repository.SeriesEditorRejectionRepository;
import com.example.backend.repository.SeriesFileRepository;
import com.example.backend.repository.SubmissionRepository;
import com.example.backend.repository.TaskRepository;
import com.example.backend.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class TantouEditorServiceTests {
    private static final String EMAIL = "tantou@manga.test";

    @Mock
    private MangaSeriesRepository mangaSeriesRepository;
    @Mock
    private ChapterRepository chapterRepository;
    @Mock
    private ChapterRevisionNoteRepository chapterRevisionNoteRepository;
    @Mock
    private ChapterPageRepository pageRepository;
    @Mock
    private ChapterPageHistoryRepository pageHistoryRepository;
    @Mock
    private ReviewCommentRepository commentRepository;
    @Mock
    private PublishScheduleRepository scheduleRepository;
    @Mock
    private BoardDecisionRepository boardDecisionRepository;
    @Mock
    private TaskRepository taskRepository;
    @Mock
    private SubmissionRepository submissionRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private SeriesFileRepository seriesFileRepository;
    @Mock
    private SeriesEditorRejectionRepository seriesEditorRejectionRepository;
    @Mock
    private SeriesBoardAssignmentRepository seriesBoardAssignmentRepository;
    @Mock
    private MangakaService mangakaService;
    @Mock
    private EditorialBoardService editorialBoardService;
    @Mock
    private SeriesHistoryService seriesHistoryService;

    private TantouEditorService service;

    @BeforeEach
    void setUp() {
        service = new TantouEditorService(
                mangaSeriesRepository,
                chapterRepository,
                chapterRevisionNoteRepository,
                pageRepository,
                pageHistoryRepository,
                commentRepository,
                scheduleRepository,
                boardDecisionRepository,
                taskRepository,
                submissionRepository,
                userRepository,
                notificationRepository,
                seriesFileRepository,
                seriesEditorRejectionRepository,
                seriesBoardAssignmentRepository,
                mangakaService,
                editorialBoardService,
                seriesHistoryService);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(EMAIL, null, List.of()));
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void listsPendingChapterReviewsForCurrentEditor() {
        MangaSeries series = series(10L);
        Chapter chapter = chapter(11L, series);
        chapter.setStatus("SUBMITTED_TO_EDITOR");
        SeriesFile manuscript = new SeriesFile();
        manuscript.setFileId(50L);
        manuscript.setSeries(series);
        manuscript.setChapter(chapter);
        manuscript.setOriginalFileName("chapter-11.zip");
        manuscript.setPurpose("CHAPTER_MANUSCRIPT");
        manuscript.setActive(true);
        when(chapterRepository.findBySeriesTantouEditorEmailAndStatusIgnoreCaseOrderByCreatedAtDesc(
                EMAIL, "SUBMITTED_TO_EDITOR")).thenReturn(List.of(chapter));
        when(pageRepository.findByChapterChapterIdOrderByPageNumberAsc(11L)).thenReturn(List.of());
        when(seriesFileRepository
                .findByChapterChapterIdAndPurposeAndActiveTrueOrderByUploadedAtAsc(
                        11L,
                        "CHAPTER_MANUSCRIPT"))
                .thenReturn(List.of(manuscript));

        var response = service.getPendingChapterReviews();

        assertEquals(1, response.size());
        assertEquals(11L, response.get(0).id());
        assertEquals(1, response.get(0).manuscriptFiles().size());
        assertEquals("chapter-11.zip", response.get(0).manuscriptFiles().get(0).originalFileName());
    }

    @Test
    void createsChapterRevisionNoteWithUploadedImage(@TempDir Path tempDir) {
        ReflectionTestUtils.setField(service, "chapterRevisionNoteUploadRootOverride", tempDir.toString());
        User editor = user(1L, EMAIL);
        MangaSeries series = series(10L);
        Chapter chapter = chapter(11L, series);
        MockMultipartFile image = new MockMultipartFile(
                "image", "revision.png", "image/png", "revision image".getBytes(StandardCharsets.UTF_8));
        when(chapterRepository.findById(11L)).thenReturn(Optional.of(chapter));
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(editor));
        when(seriesFileRepository.findMaxRoundNumberByChapterAndPurpose(11L, "CHAPTER_MANUSCRIPT"))
                .thenReturn(2);
        when(chapterRevisionNoteRepository.save(any(ChapterRevisionNote.class))).thenAnswer(invocation -> {
            ChapterRevisionNote note = invocation.getArgument(0);
            note.setNoteId(90L);
            return note;
        });

        var response = service.createChapterRevisionNote(
                11L,
                image,
                "{\"objects\":[]}",
                "Sai lời thoại ở khung thứ hai",
                2);

        assertEquals(90L, response.id());
        assertEquals(11L, response.chapterId());
        assertEquals("Sai lời thoại ở khung thứ hai", response.description());
        assertEquals(2, response.roundNumber());
        assertEquals(2, response.orderIndex());
        assertTrue(response.imageUrl().startsWith("chapter-revision-notes/chapter-11/"));
        String savedFileName = Path.of(response.imageUrl()).getFileName().toString();
        assertTrue(Files.exists(tempDir.resolve("chapter-11").resolve(savedFileName)));
    }

    @Test
    void createChapterRevisionNoteRequiresDescription(@TempDir Path tempDir) {
        ReflectionTestUtils.setField(service, "chapterRevisionNoteUploadRootOverride", tempDir.toString());
        MangaSeries series = series(10L);
        Chapter chapter = chapter(11L, series);
        MockMultipartFile image = new MockMultipartFile(
                "image", "revision.png", "image/png", "revision image".getBytes(StandardCharsets.UTF_8));
        when(chapterRepository.findById(11L)).thenReturn(Optional.of(chapter));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.createChapterRevisionNote(11L, image, null, "  ", 0));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("description is required", exception.getReason());
        verify(chapterRevisionNoteRepository, never()).save(any(ChapterRevisionNote.class));
    }

    @Test
    void requestChapterRevisionNotifiesMangaka() {
        MangaSeries series = series(10L);
        Chapter chapter = chapter(11L, series);
        chapter.setStatus("SUBMITTED_TO_EDITOR");
        when(chapterRepository.findById(11L)).thenReturn(Optional.of(chapter));
        when(chapterRepository.save(chapter)).thenReturn(chapter);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(series.getTantouEditor()));
        when(pageRepository.findByChapterChapterIdOrderByPageNumberAsc(11L)).thenReturn(List.of());

        var response = service.requestChapterRevision(11L);

        assertEquals("REVISION_REQUESTED", response.status());
        ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(notificationCaptor.capture());
        Notification notification = notificationCaptor.getValue();
        assertEquals(series.getAuthor(), notification.getUser());
        assertEquals("CHAPTER_REVISION_REQUESTED", notification.getType());
        assertEquals(11L, notification.getReferenceId());
    }

    @Test
    void approveAndReadyChapterMarksChapterApproved() {
        MangaSeries series = series(10L);
        User coordinator = user(3L, "coordinator@manga.test");
        series.setPublicationCoordinator(coordinator);
        Chapter chapter = chapter(11L, series);
        chapter.setStatus("SUBMITTED_TO_EDITOR");
        when(chapterRepository.findById(11L)).thenReturn(Optional.of(chapter));
        when(chapterRepository.save(chapter)).thenReturn(chapter);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(series.getTantouEditor()));
        when(pageRepository.findByChapterChapterIdOrderByPageNumberAsc(11L)).thenReturn(List.of());

        var response = service.approveAndReadyChapter(11L);

        assertEquals("APPROVED", response.status());
        verify(chapterRepository).save(chapter);
        ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository, org.mockito.Mockito.times(2)).save(notificationCaptor.capture());
        assertEquals(
                List.of("CHAPTER_APPROVED", "CHAPTER_READY_FOR_SCHEDULE"),
                notificationCaptor.getAllValues().stream()
                        .map(Notification::getType)
                        .toList());
    }

    @Test
    void createsPageCommentWithEditorAndOpenStatus() {
        User editor = user(1L, EMAIL);
        MangaSeries series = series(10L);
        Chapter chapter = chapter(11L, series);
        ChapterPage page = page(20L, "DRAFT");
        page.setChapter(chapter);
        when(pageRepository.findById(20L)).thenReturn(Optional.of(page));
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(editor));
        when(commentRepository.save(any(ReviewComment.class))).thenAnswer(invocation -> {
            ReviewComment comment = invocation.getArgument(0);
            comment.setCommentId(30L);
            return comment;
        });

        var response = service.createComment(
                20L,
                new CommentRequest("Sửa thoại ở khung này", "DIALOGUE", 10.5F, 20F, null));

        assertEquals(30L, response.id());
        assertEquals(20L, response.pageId());
        assertEquals(1L, response.editorId());
        assertEquals("DIALOGUE", response.commentType());
        assertEquals("OPEN", response.status());
        verify(commentRepository).save(any(ReviewComment.class));
    }

    @Test
    void calculatesSeriesProgress() {
        MangaSeries series = series(10L);
        Chapter chapterOne = chapter(11L, series);
        Chapter chapterTwo = chapter(12L, series);
        ChapterPage draftPage = page(21L, "DRAFT");
        ChapterPage finalizedPage = page(22L, "DRAWING_FINALIZED");
        Task assigned = task(31L, chapterOne, "ASSIGNED", LocalDateTime.now().plusDays(2));
        Task overdue = task(32L, chapterTwo, "IN_PROGRESS", LocalDateTime.now().minusDays(1));
        Task approved = task(33L, chapterTwo, "APPROVED", LocalDateTime.now().minusDays(3));
        ReviewComment openComment = comment(41L, draftPage, "OPEN");
        ReviewComment resolvedComment = comment(42L, finalizedPage, "RESOLVED");
        PublishSchedule schedule = schedule(51L, series, LocalDateTime.now().plusDays(5));

        when(mangaSeriesRepository.findById(10L)).thenReturn(Optional.of(series));
        when(chapterRepository.findBySeriesSeriesIdOrderByChapterNumberAsc(10L))
                .thenReturn(List.of(chapterOne, chapterTwo));
        when(pageRepository.findByChapterSeriesSeriesIdOrderByPageNumberAsc(10L))
                .thenReturn(List.of(draftPage, finalizedPage));
        when(taskRepository.findByChapterSeriesSeriesIdOrderByDueDateAsc(10L))
                .thenReturn(List.of(assigned, overdue, approved));
        when(commentRepository.findByPageChapterSeriesSeriesIdOrderByCreatedAtDesc(10L))
                .thenReturn(List.of(openComment, resolvedComment));
        when(scheduleRepository.findBySeriesSeriesIdOrderByPublishDateAsc(10L))
                .thenReturn(List.of(schedule));

        var response = service.getSeriesProgress(10L);

        assertEquals(2, response.totalChapters());
        assertEquals(2, response.totalPages());
        assertEquals(1, response.finalizedPages());
        assertEquals(3, response.totalTasks());
        assertEquals(1, response.assignedTasks());
        assertEquals(1, response.inProgressTasks());
        assertEquals(1, response.approvedTasks());
        assertEquals(1, response.overdueTasks());
        assertEquals(1, response.openComments());
        assertEquals(50.0, response.completionRate());
    }

    @Test
    void submitToEditorialBoardMovesTantouReviewSeriesToReviewing() {
        MangaSeries series = series(10L);
        series.setStatus("TANTOU_REVIEW");
        Chapter chapter = chapter(11L, series);
        ChapterPage page = page(21L, "DRAFT");
        page.setChapter(chapter);
        when(mangaSeriesRepository.findById(10L)).thenReturn(Optional.of(series));
        when(pageRepository.findByChapterSeriesSeriesIdOrderByPageNumberAsc(10L)).thenReturn(List.of(page));
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user(1L, EMAIL)));
        when(chapterRepository.findBySeriesSeriesIdOrderByChapterNumberAsc(10L)).thenReturn(List.of(chapter));
        when(taskRepository.findByChapterSeriesSeriesIdOrderByDueDateAsc(10L)).thenReturn(List.of());
        when(commentRepository.findByPageChapterSeriesSeriesIdOrderByCreatedAtDesc(10L)).thenReturn(List.of());
        when(scheduleRepository.findBySeriesSeriesIdOrderByPublishDateAsc(10L)).thenReturn(List.of());
        when(boardDecisionRepository.findBySeriesSeriesIdOrderByDecisionDateDesc(10L)).thenReturn(List.of());
        when(boardDecisionRepository.countBySeriesSeriesIdAndDecisionTypeIgnoreCase(10L, "APPROVE")).thenReturn(0L);
        when(boardDecisionRepository.countBySeriesSeriesIdAndDecisionTypeIgnoreCase(10L, "REJECT")).thenReturn(0L);
        when(mangakaService.currentSeriesSubmissionRound(10L)).thenReturn(2);
        MockMultipartFile dossier = new MockMultipartFile(
                "files", "editor-report.pdf", "application/pdf", "report".getBytes(StandardCharsets.UTF_8));

        var response = service.submitToEditorialBoard(
                10L, "Đã biên tập sơ bộ", List.of(dossier));

        assertEquals("REVIEWING", series.getStatus());
        assertEquals("REVIEWING", response.series().status());
        verify(mangaSeriesRepository).save(series);
        verify(editorialBoardService).assignBoardPanel(series);
        verify(mangakaService).storeSeriesWorkflowFiles(
                eq(series),
                any(User.class),
                eq(List.of(dossier)),
                eq("EDITOR_DOSSIER"),
                eq(2));
    }

    @Test
    void rejectSeriesMovesToAnEditorWhoHasNotRejectedIt() {
        User editor = user(1L, EMAIL);
        User nextEditor = user(2L, "next-editor@manga.test");
        MangaSeries series = series(10L);
        series.setStatus("PENDING_EDITOR");
        series.setEditorAssignmentLocked(false);
        SeriesEditorRejection rejection = new SeriesEditorRejection();
        rejection.setSeries(series);
        rejection.setEditor(editor);
        when(mangaSeriesRepository.findById(10L)).thenReturn(Optional.of(series));
        when(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(editor));
        when(seriesEditorRejectionRepository.findBySeriesSeriesIdAndEditorUserId(10L, 1L))
                .thenReturn(Optional.empty());
        when(seriesEditorRejectionRepository.findBySeriesSeriesId(10L)).thenReturn(List.of(rejection));
        when(seriesEditorRejectionRepository.countBySeriesSeriesId(10L)).thenReturn(1L);
        when(userRepository.findByRoleRoleNameAndStatusOrderByUsernameAsc("TANTOU_EDITOR", "ACTIVE"))
                .thenReturn(List.of(editor, nextEditor));
        when(mangakaService.findEditorWithLeastWorkloadExcluding(any(), eq("Action, Drama")))
                .thenReturn(Optional.of(nextEditor));

        service.rejectSeries(10L, "Không phù hợp chuyên môn");

        assertEquals("PENDING_EDITOR", series.getStatus());
        assertEquals(nextEditor, series.getTantouEditor());
        verify(seriesEditorRejectionRepository).save(any(SeriesEditorRejection.class));
        verify(mangaSeriesRepository).save(series);
        verify(seriesHistoryService).record(
                series,
                editor,
                "EDITOR_REJECTED_SERIES",
                "PENDING_EDITOR",
                "PENDING_EDITOR",
                "Không phù hợp chuyên môn",
                10L);
    }

    @Test
    void rejectSeriesRequiresBoardAssignmentAfterAllActiveEditorsRejected() {
        User editor = user(1L, EMAIL);
        MangaSeries series = series(10L);
        series.setStatus("PENDING_EDITOR");
        series.setEditorAssignmentLocked(false);
        SeriesEditorRejection rejection = new SeriesEditorRejection();
        rejection.setSeries(series);
        rejection.setEditor(editor);
        when(mangaSeriesRepository.findById(10L)).thenReturn(Optional.of(series));
        when(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(editor));
        when(seriesEditorRejectionRepository.findBySeriesSeriesIdAndEditorUserId(10L, 1L))
                .thenReturn(Optional.empty());
        when(seriesEditorRejectionRepository.findBySeriesSeriesId(10L)).thenReturn(List.of(rejection));
        when(seriesEditorRejectionRepository.countBySeriesSeriesId(10L)).thenReturn(1L);
        when(userRepository.findByRoleRoleNameAndStatusOrderByUsernameAsc("TANTOU_EDITOR", "ACTIVE"))
                .thenReturn(List.of(editor));

        service.rejectSeries(10L, "Khối lượng hiện tại đã đầy");

        assertEquals("EDITOR_ASSIGNMENT_REQUIRED", series.getStatus());
        assertEquals(null, series.getTantouEditor());
        ArgumentCaptor<SeriesEditorRejection> rejectionCaptor = ArgumentCaptor.forClass(SeriesEditorRejection.class);
        verify(seriesEditorRejectionRepository).save(rejectionCaptor.capture());
        assertEquals("Khối lượng hiện tại đã đầy", rejectionCaptor.getValue().getReason());
        verify(mangakaService, never()).findEditorWithLeastWorkloadExcluding(any());
    }

    @Test
    void rejectSeriesStopsAutomaticRoutingAfterThreeEditors() {
        User currentEditor = user(1L, EMAIL);
        MangaSeries series = series(10L);
        series.setStatus("PENDING_EDITOR");
        series.setEditorAssignmentLocked(false);
        List<SeriesEditorRejection> rejections = List.of(
                rejection(series, currentEditor),
                rejection(series, user(2L, "editor2@manga.test")),
                rejection(series, user(3L, "editor3@manga.test")));
        when(mangaSeriesRepository.findById(10L)).thenReturn(Optional.of(series));
        when(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(currentEditor));
        when(seriesEditorRejectionRepository.findBySeriesSeriesIdAndEditorUserId(10L, 1L))
                .thenReturn(Optional.empty());
        when(seriesEditorRejectionRepository.findBySeriesSeriesId(10L)).thenReturn(rejections);
        when(seriesEditorRejectionRepository.countBySeriesSeriesId(10L)).thenReturn(3L);

        service.rejectSeries(10L, "Not available for this project");

        assertEquals("EDITOR_ASSIGNMENT_REQUIRED", series.getStatus());
        verify(mangakaService, never()).findEditorWithLeastWorkloadExcluding(any(), any());
    }

    @Test
    void requestSeriesDropCreatesBoardReviewFlow() {
        MangaSeries series = series(10L);
        series.setStatus("TANTOU_REVIEW");
        when(mangaSeriesRepository.findById(10L)).thenReturn(Optional.of(series));
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(series.getTantouEditor()));
        when(chapterRepository.findBySeriesSeriesIdOrderByChapterNumberAsc(10L)).thenReturn(List.of());
        when(pageRepository.findByChapterSeriesSeriesIdOrderByPageNumberAsc(10L)).thenReturn(List.of());
        when(taskRepository.findByChapterSeriesSeriesIdOrderByDueDateAsc(10L)).thenReturn(List.of());
        when(scheduleRepository.findBySeriesSeriesIdOrderByPublishDateAsc(10L)).thenReturn(List.of());
        when(boardDecisionRepository.findBySeriesSeriesIdOrderByDecisionDateDesc(10L)).thenReturn(List.of());
        when(commentRepository.findByPageChapterSeriesSeriesIdOrderByCreatedAtDesc(10L)).thenReturn(List.of());

        var response = service.requestSeriesDrop(10L, "Quality remains below the agreed target");

        assertEquals("DROP_REQUESTED", series.getStatus());
        assertEquals("DROP_REQUESTED", response.series().status());
        verify(editorialBoardService).assignBoardPanel(series);
        verify(seriesHistoryService).record(
                eq(series),
                eq(series.getTantouEditor()),
                eq("EDITOR_REQUESTED_SERIES_DROP"),
                eq("TANTOU_REVIEW"),
                eq("DROP_REQUESTED"),
                eq("Quality remains below the agreed target"),
                eq(10L));
    }

    @Test
    void rejectSeriesRequiresReason() {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.rejectSeries(10L, "  "));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Vui lòng nhập lý do từ chối", exception.getReason());
        verify(mangaSeriesRepository, never()).findById(any(Long.class));
    }

    @Test
    void editorialBoardAssignmentCannotBeRejectedOrOverrideTheLock() {
        MangaSeries series = series(10L);
        series.setStatus("PENDING_EDITOR");
        series.setEditorAssignmentLocked(true);
        when(mangaSeriesRepository.findById(10L)).thenReturn(Optional.of(series));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.rejectSeries(10L, "Không nhận hồ sơ"));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertTrue(series.getEditorAssignmentLocked());
        verify(userRepository, never()).findByIdForUpdate(any(Long.class));
        verify(seriesEditorRejectionRepository, never()).save(any(SeriesEditorRejection.class));
    }

    private MangaSeries series(Long id) {
        MangaSeries series = new MangaSeries();
        series.setSeriesId(id);
        series.setTitle("Series");
        series.setAuthor(user(2L, "author@manga.test"));
        series.setTantouEditor(user(1L, EMAIL));
        series.setGenre("Action, Drama");
        series.setStatus("Published");
        series.setCreatedAt(LocalDateTime.now().minusDays(10));
        return series;
    }

    private SeriesEditorRejection rejection(MangaSeries series, User editor) {
        SeriesEditorRejection rejection = new SeriesEditorRejection();
        rejection.setSeries(series);
        rejection.setEditor(editor);
        rejection.setReason("Rejected");
        rejection.setRejectedAt(LocalDateTime.now());
        return rejection;
    }

    private Chapter chapter(Long id, MangaSeries series) {
        Chapter chapter = new Chapter();
        chapter.setChapterId(id);
        chapter.setSeries(series);
        chapter.setChapterNumber(id.intValue());
        chapter.setTitle("Chapter " + id);
        chapter.setStatus("DRAFT");
        return chapter;
    }

    private ChapterPage page(Long id, String status) {
        ChapterPage page = new ChapterPage();
        page.setPageId(id);
        page.setPageNumber(id.intValue());
        page.setPageStatus(status);
        return page;
    }

    private Task task(Long id, Chapter chapter, String status, LocalDateTime dueDate) {
        Task task = new Task();
        task.setTaskId(id);
        task.setChapter(chapter);
        task.setStatus(status);
        task.setDueDate(dueDate);
        return task;
    }

    private ReviewComment comment(Long id, ChapterPage page, String status) {
        ReviewComment comment = new ReviewComment();
        comment.setCommentId(id);
        comment.setPage(page);
        comment.setCommentText("Comment " + id);
        comment.setCommentType("CONTENT");
        comment.setStatus(status);
        return comment;
    }

    private PublishSchedule schedule(Long id, MangaSeries series, LocalDateTime publishDate) {
        PublishSchedule schedule = new PublishSchedule();
        schedule.setScheduleId(id);
        schedule.setSeries(series);
        schedule.setPublishDate(publishDate);
        schedule.setFrequency("WEEKLY");
        schedule.setStatus("PLANNED");
        return schedule;
    }

    private User user(Long id, String email) {
        User user = new User();
        user.setUserId(id);
        user.setEmail(email);
        user.setUsername(email);
        return user;
    }
}

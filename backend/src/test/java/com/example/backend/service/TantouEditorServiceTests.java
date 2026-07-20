package com.example.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
import java.time.YearMonth;
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
import com.example.backend.dto.TantouEditorDtos.ScheduleRequest;
import com.example.backend.model.Chapter;
import com.example.backend.model.ChapterPage;
import com.example.backend.model.ChapterRevisionNote;
import com.example.backend.model.MangaSeries;
import com.example.backend.model.Notification;
import com.example.backend.model.PublishSchedule;
import com.example.backend.model.ReviewComment;
import com.example.backend.model.SeriesEditorRejection;
import com.example.backend.model.Task;
import com.example.backend.model.User;
import com.example.backend.repository.BoardDecisionRepository;
import com.example.backend.repository.ChapterPageRepository;
import com.example.backend.repository.ChapterRepository;
import com.example.backend.repository.ChapterRevisionNoteRepository;
import com.example.backend.repository.MangaSeriesRepository;
import com.example.backend.repository.NotificationRepository;
import com.example.backend.repository.PublishScheduleRepository;
import com.example.backend.repository.ReviewCommentRepository;
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
    private MangakaService mangakaService;

    private TantouEditorService service;

    @BeforeEach
    void setUp() {
        service = new TantouEditorService(
                mangaSeriesRepository,
                chapterRepository,
                chapterRevisionNoteRepository,
                pageRepository,
                commentRepository,
                scheduleRepository,
                boardDecisionRepository,
                taskRepository,
                submissionRepository,
                userRepository,
                notificationRepository,
                seriesFileRepository,
                seriesEditorRejectionRepository,
                mangakaService);
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
        chapter.setManuscriptUrl("drive/chapter-11.psd");
        chapter.setStatus("SUBMITTED_TO_EDITOR");
        when(chapterRepository.findBySeriesTantouEditorEmailAndStatusIgnoreCaseOrderByCreatedAtDesc(
                EMAIL, "SUBMITTED_TO_EDITOR")).thenReturn(List.of(chapter));
        when(pageRepository.findByChapterChapterIdOrderByPageNumberAsc(11L)).thenReturn(List.of());

        var response = service.getPendingChapterReviews();

        assertEquals(1, response.size());
        assertEquals(11L, response.get(0).id());
        assertEquals("drive/chapter-11.psd", response.get(0).manuscriptUrl());
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
        when(chapterRevisionNoteRepository.save(any(ChapterRevisionNote.class))).thenAnswer(invocation -> {
            ChapterRevisionNote note = invocation.getArgument(0);
            note.setNoteId(90L);
            return note;
        });

        var response = service.createChapterRevisionNote(11L, image, "{\"objects\":[]}", 2);

        assertEquals(90L, response.id());
        assertEquals(11L, response.chapterId());
        assertEquals(2, response.orderIndex());
        assertTrue(response.imageUrl().startsWith("chapter-revision-notes/chapter-11/"));
        String savedFileName = Path.of(response.imageUrl()).getFileName().toString();
        assertTrue(Files.exists(tempDir.resolve("chapter-11").resolve(savedFileName)));
    }

    @Test
    void requestChapterRevisionNotifiesMangaka() {
        MangaSeries series = series(10L);
        Chapter chapter = chapter(11L, series);
        chapter.setStatus("SUBMITTED_TO_EDITOR");
        when(chapterRepository.findById(11L)).thenReturn(Optional.of(chapter));
        when(chapterRepository.save(chapter)).thenReturn(chapter);
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
    void publishChapterSetsReleaseDateAndNotifiesMangaka() {
        MangaSeries series = series(10L);
        Chapter chapter = chapter(11L, series);
        chapter.setStatus("SUBMITTED_TO_EDITOR");
        when(chapterRepository.findById(11L)).thenReturn(Optional.of(chapter));
        when(chapterRepository.save(chapter)).thenReturn(chapter);
        when(pageRepository.findByChapterChapterIdOrderByPageNumberAsc(11L)).thenReturn(List.of());

        var response = service.publishChapter(11L);

        assertEquals("PUBLISHED", response.status());
        assertNotNull(chapter.getReleaseDate());
        ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(notificationCaptor.capture());
        Notification notification = notificationCaptor.getValue();
        assertEquals(series.getAuthor(), notification.getUser());
        assertEquals("CHAPTER_PUBLISHED", notification.getType());
        assertEquals(11L, notification.getReferenceId());
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
    void createPublishScheduleIsForbiddenForTantouEditor() {
        LocalDateTime publishDate = LocalDateTime.now().plusDays(7);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> service.createSchedule(new ScheduleRequest(10L, publishDate, "WEEKLY", null)));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    }

    @Test
    void updatePublishScheduleIsForbiddenForTantouEditor() {
        LocalDateTime newPublishDate = LocalDateTime.now().plusDays(14);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> service.updateSchedule(
                        60L,
                        new ScheduleRequest(10L, newPublishDate, "MONTHLY", "PLANNED")));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
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

        var response = service.submitToEditorialBoard(10L, "Đã biên tập sơ bộ");

        assertEquals("REVIEWING", series.getStatus());
        assertEquals("REVIEWING", response.series().status());
        verify(mangaSeriesRepository).save(series);
        verify(commentRepository).save(any(ReviewComment.class));
    }

    @Test
    void rejectSeriesIsBlockedAfterTwoRejectionsInCurrentMonth() {
        User editor = user(1L, EMAIL);
        MangaSeries series = series(10L);
        series.setStatus("PENDING_EDITOR");
        series.setEditorAssignmentLocked(false);
        when(mangaSeriesRepository.findById(10L)).thenReturn(Optional.of(series));
        when(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(editor));
        when(seriesEditorRejectionRepository
                .countByEditorUserIdAndRejectedAtGreaterThanEqualAndRejectedAtLessThan(
                        eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(2L);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.rejectSeries(10L));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertTrue(exception.getReason().contains("2 rejections"));
        verify(seriesEditorRejectionRepository, never()).save(any(SeriesEditorRejection.class));
        verify(mangaSeriesRepository, never()).save(any(MangaSeries.class));
    }

    @Test
    void rejectSeriesCountsOnlyTheCurrentCalendarMonth() {
        User editor = user(1L, EMAIL);
        MangaSeries series = series(10L);
        series.setStatus("PENDING_EDITOR");
        series.setEditorAssignmentLocked(false);
        SeriesEditorRejection rejection = new SeriesEditorRejection();
        rejection.setSeries(series);
        rejection.setEditor(editor);
        when(mangaSeriesRepository.findById(10L)).thenReturn(Optional.of(series));
        when(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(editor));
        when(seriesEditorRejectionRepository
                .countByEditorUserIdAndRejectedAtGreaterThanEqualAndRejectedAtLessThan(
                        eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(0L);
        when(seriesEditorRejectionRepository.findBySeriesSeriesIdAndEditorUserId(10L, 1L))
                .thenReturn(Optional.empty());
        when(seriesEditorRejectionRepository.findBySeriesSeriesId(10L)).thenReturn(List.of(rejection));
        when(userRepository.findByRoleRoleNameAndStatusOrderByUsernameAsc("TANTOU_EDITOR", "ACTIVE"))
                .thenReturn(List.of(editor));

        service.rejectSeries(10L);

        ArgumentCaptor<LocalDateTime> periodStart = ArgumentCaptor.forClass(LocalDateTime.class);
        ArgumentCaptor<LocalDateTime> periodEnd = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(seriesEditorRejectionRepository)
                .countByEditorUserIdAndRejectedAtGreaterThanEqualAndRejectedAtLessThan(
                        eq(1L), periodStart.capture(), periodEnd.capture());
        YearMonth currentMonth = YearMonth.now();
        assertEquals(currentMonth.atDay(1).atStartOfDay(), periodStart.getValue());
        assertEquals(currentMonth.plusMonths(1).atDay(1).atStartOfDay(), periodEnd.getValue());
        assertEquals("EDITOR_ASSIGNMENT_REQUIRED", series.getStatus());
        verify(seriesEditorRejectionRepository).save(any(SeriesEditorRejection.class));
    }

    @Test
    void editorialBoardAssignmentCannotBeRejectedOrOverrideTheLock() {
        MangaSeries series = series(10L);
        series.setStatus("PENDING_EDITOR");
        series.setEditorAssignmentLocked(true);
        when(mangaSeriesRepository.findById(10L)).thenReturn(Optional.of(series));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.rejectSeries(10L));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertTrue(series.getEditorAssignmentLocked());
        verify(userRepository, never()).findByIdForUpdate(any(Long.class));
        verify(seriesEditorRejectionRepository, never())
                .countByEditorUserIdAndRejectedAtGreaterThanEqualAndRejectedAtLessThan(
                        any(Long.class), any(LocalDateTime.class), any(LocalDateTime.class));
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

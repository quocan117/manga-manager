package com.example.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.example.backend.dto.MangakaDtos.AssignTaskRequest;
import com.example.backend.dto.MangakaDtos.CreateAssistantRequest;
import com.example.backend.dto.MangakaDtos.CreateSeriesRequest;
import com.example.backend.dto.MangakaDtos.ReviewSubmissionRequest;
import com.example.backend.dto.MangakaDtos.ReviseTaskRequest;
import com.example.backend.model.Chapter;
import com.example.backend.model.ChapterPage;
import com.example.backend.model.MangaSeries;
import com.example.backend.model.Notification;
import com.example.backend.model.Role;
import com.example.backend.model.SeriesFile;
import com.example.backend.model.Submission;
import com.example.backend.model.Task;
import com.example.backend.model.TaskMarkupPage;
import com.example.backend.model.User;
import com.example.backend.repository.ChapterPageRepository;
import com.example.backend.repository.ChapterRepository;
import com.example.backend.repository.ChapterRevisionNoteRepository;
import com.example.backend.repository.BoardDecisionRepository;
import com.example.backend.repository.MangaSeriesRepository;
import com.example.backend.repository.NotificationRepository;
import com.example.backend.repository.RoleRepository;
import com.example.backend.repository.SeriesFileRepository;
import com.example.backend.repository.SeriesRankingRepository;
import com.example.backend.repository.SubmissionRepository;
import com.example.backend.repository.TaskRepository;
import com.example.backend.repository.TaskMarkupPageRepository;
import com.example.backend.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class MangakaServiceTests {
    private static final String EMAIL = "mangaka@test.local";

    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private MangaSeriesRepository mangaSeriesRepository;
    @Mock
    private BoardDecisionRepository boardDecisionRepository;
    @Mock
    private ChapterRepository chapterRepository;
    @Mock
    private ChapterRevisionNoteRepository chapterRevisionNoteRepository;
    @Mock
    private ChapterPageRepository chapterPageRepository;
    @Mock
    private TaskRepository taskRepository;
    @Mock
    private SubmissionRepository submissionRepository;
    @Mock
    private SeriesRankingRepository seriesRankingRepository;
    @Mock
    private SeriesFileRepository seriesFileRepository;
    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private TaskMarkupPageRepository taskMarkupPageRepository;
    @Mock
    private TaskFileStorageService taskFileStorageService;
    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private MangakaService service;

    @BeforeEach
    void authenticateMangaka() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(EMAIL, null, List.of()));
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createSeriesAssignsAuthenticatedMangakaAndDraftStatus() {
        User mangaka = user(1L, EMAIL);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(mangaka));
        when(mangaSeriesRepository.save(any(MangaSeries.class))).thenAnswer(invocation -> {
            MangaSeries series = invocation.getArgument(0);
            series.setSeriesId(10L);
            return series;
        });

        var response = service.createSeries(new CreateSeriesRequest(
                "New series", List.of("Action", "Comedy"), null, "Description", null, null));

        assertEquals(10L, response.id());
        assertEquals("DRAFT", response.status());
        assertEquals(List.of("Action", "Comedy"), response.genres());
        verify(mangaSeriesRepository).save(any(MangaSeries.class));
    }

    @Test
    void createSeriesKeepsLongCoverUrl() {
        User mangaka = user(1L, EMAIL);
        String coverUrl = "https://cdn.example.test/covers/series-cover.jpg?token="
                + "a".repeat(300);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(mangaka));
        when(mangaSeriesRepository.save(any(MangaSeries.class))).thenAnswer(invocation -> {
            MangaSeries series = invocation.getArgument(0);
            series.setSeriesId(10L);
            return series;
        });

        var response = service.createSeries(new CreateSeriesRequest(
                "New series", List.of("Action"), "  " + coverUrl + "  ", null, null, null));

        ArgumentCaptor<MangaSeries> captor = ArgumentCaptor.forClass(MangaSeries.class);
        verify(mangaSeriesRepository).save(captor.capture());
        assertEquals(coverUrl, captor.getValue().getCoverImage());
        assertEquals(coverUrl, response.coverUrl());
    }

    @Test
    void createSeriesUploadsCoverImage(@TempDir Path tempDir) {
        ReflectionTestUtils.setField(service, "coverImageUploadRootOverride", tempDir.toString());
        User mangaka = user(1L, EMAIL);
        MockMultipartFile coverImage = new MockMultipartFile(
                "coverImage",
                "cover.png",
                "image/png",
                "cover".getBytes(StandardCharsets.UTF_8));
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(mangaka));
        when(mangaSeriesRepository.save(any(MangaSeries.class))).thenAnswer(invocation -> {
            MangaSeries series = invocation.getArgument(0);
            series.setSeriesId(10L);
            return series;
        });

        var response = service.createSeriesWithCoverUpload(
                "New series",
                List.of("Action, Comedy"),
                "https://cdn.example.test/ignored-cover.jpg",
                null,
                null,
                null,
                coverImage);

        ArgumentCaptor<MangaSeries> captor = ArgumentCaptor.forClass(MangaSeries.class);
        verify(mangaSeriesRepository).save(captor.capture());
        assertEquals(List.of("Action", "Comedy"), response.genres());
        assertTrue(response.coverUrl().startsWith("series/series-cover-"));
        assertTrue(response.coverUrl().endsWith(".png"));
        assertEquals(response.coverUrl(), captor.getValue().getCoverImage());
        assertTrue(Files.exists(tempDir.resolve(Path.of(response.coverUrl()).getFileName())));
    }

    @Test
    void submitSeriesRejectsSeriesOwnedByAnotherMangaka() {
        MangaSeries series = new MangaSeries();
        series.setSeriesId(20L);
        series.setAuthor(user(2L, "other@test.local"));
        series.setStatus("DRAFT");
        when(mangaSeriesRepository.findById(20L)).thenReturn(Optional.of(series));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> service.submitSeriesWithFiles(20L, List.of(seriesFile())));

        assertSame(HttpStatus.FORBIDDEN, exception.getStatusCode());
        verify(mangaSeriesRepository, never()).save(any());
    }

    @Test
    void submitSeriesMovesDraftToTantouReview(@TempDir Path tempDir) {
        ReflectionTestUtils.setField(service, "seriesFileUploadRootOverride", tempDir.toString());
        MangaSeries series = new MangaSeries();
        series.setSeriesId(20L);
        series.setAuthor(user(1L, EMAIL));
        series.setStatus("DRAFT");
        User editor = tantouEditor(3L, "tantou1@manga.test");
        when(mangaSeriesRepository.findById(20L)).thenReturn(Optional.of(series));
        when(userRepository.findByRoleRoleNameAndStatusOrderByUsernameAsc("TANTOU_EDITOR", "ACTIVE"))
                .thenReturn(List.of(editor));
        when(mangaSeriesRepository.countByTantouEditorUserIdAndStatusIn(eq(3L), any()))
                .thenReturn(0L);
        when(mangaSeriesRepository.save(series)).thenReturn(series);

        var response = service.submitSeriesWithFiles(20L, List.of(seriesFile()));

        assertEquals("PENDING_EDITOR", series.getStatus());
        assertEquals(editor, series.getTantouEditor());
        assertEquals("PENDING_EDITOR", response.status());
    }

    @Test
    void submitSeriesAssignsLeastLoadedTantouEditor(@TempDir Path tempDir) {
        ReflectionTestUtils.setField(service, "seriesFileUploadRootOverride", tempDir.toString());
        MangaSeries series = new MangaSeries();
        series.setSeriesId(20L);
        series.setAuthor(user(1L, EMAIL));
        series.setStatus("DRAFT");
        User busyEditor = tantouEditor(3L, "busy@manga.test");
        User availableEditor = tantouEditor(4L, "available@manga.test");
        when(mangaSeriesRepository.findById(20L)).thenReturn(Optional.of(series));
        when(userRepository.findByRoleRoleNameAndStatusOrderByUsernameAsc("TANTOU_EDITOR", "ACTIVE"))
                .thenReturn(List.of(busyEditor, availableEditor));
        when(mangaSeriesRepository.countByTantouEditorUserIdAndStatusIn(eq(3L), any()))
                .thenReturn(2L);
        when(mangaSeriesRepository.countByTantouEditorUserIdAndStatusIn(eq(4L), any()))
                .thenReturn(0L);
        when(mangaSeriesRepository.save(series)).thenReturn(series);

        service.submitSeriesWithFiles(20L, List.of(seriesFile()));

        assertEquals(availableEditor, series.getTantouEditor());
    }

    @Test
    void submitSeriesRandomlyAssignsAmongEquallyLoadedTantouEditors(@TempDir Path tempDir) {
        ReflectionTestUtils.setField(service, "seriesFileUploadRootOverride", tempDir.toString());
        MangaSeries series = new MangaSeries();
        series.setSeriesId(20L);
        series.setAuthor(user(1L, EMAIL));
        series.setStatus("DRAFT");
        User firstEditor = tantouEditor(3L, "first@manga.test");
        User secondEditor = tantouEditor(4L, "second@manga.test");
        when(mangaSeriesRepository.findById(20L)).thenReturn(Optional.of(series));
        when(userRepository.findByRoleRoleNameAndStatusOrderByUsernameAsc("TANTOU_EDITOR", "ACTIVE"))
                .thenReturn(List.of(firstEditor, secondEditor));
        when(mangaSeriesRepository.countByTantouEditorUserIdAndStatusIn(eq(3L), any()))
                .thenReturn(1L);
        when(mangaSeriesRepository.countByTantouEditorUserIdAndStatusIn(eq(4L), any()))
                .thenReturn(1L);
        when(mangaSeriesRepository.save(series)).thenReturn(series);

        service.submitSeriesWithFiles(20L, List.of(seriesFile()));

        assertTrue(List.of(firstEditor, secondEditor).contains(series.getTantouEditor()));
    }

    @Test
    void submitSeriesResubmitsRevisionToSameTantouEditor(@TempDir Path tempDir) {
        ReflectionTestUtils.setField(service, "seriesFileUploadRootOverride", tempDir.toString());
        MangaSeries series = new MangaSeries();
        series.setSeriesId(20L);
        series.setTitle("Revision Series");
        series.setAuthor(user(1L, EMAIL));
        series.setStatus("REVISION_REQUESTED");
        User editor = tantouEditor(3L, "tantou1@manga.test");
        series.setTantouEditor(editor);
        when(mangaSeriesRepository.findById(20L)).thenReturn(Optional.of(series));
        when(mangaSeriesRepository.save(series)).thenReturn(series);

        var response = service.submitSeriesWithFiles(20L, List.of(seriesFile()));

        assertEquals("TANTOU_REVIEW", series.getStatus());
        assertSame(editor, series.getTantouEditor());
        assertEquals("TANTOU_REVIEW", response.status());
        verify(seriesFileRepository).deactivateActiveFiles(20L, "SERIES_SUBMISSION");
        verify(userRepository, never())
                .findByRoleRoleNameAndStatusOrderByUsernameAsc(eq("TANTOU_EDITOR"), eq("ACTIVE"));

        ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(notificationCaptor.capture());
        Notification notification = notificationCaptor.getValue();
        assertEquals(editor, notification.getUser());
        assertEquals("SERIES_RESUBMITTED", notification.getType());
        assertEquals(20L, notification.getReferenceId());
    }

    @Test
    void submitSeriesRequiresAtLeastOneFile() {
        MangaSeries series = new MangaSeries();
        series.setSeriesId(20L);
        series.setAuthor(user(1L, EMAIL));
        series.setStatus("DRAFT");
        when(mangaSeriesRepository.findById(20L)).thenReturn(Optional.of(series));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> service.submitSeriesWithFiles(20L, List.of()));

        assertSame(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verify(mangaSeriesRepository, never()).save(any());
        verify(userRepository, never())
                .findByRoleRoleNameAndStatusOrderByUsernameAsc(eq("TANTOU_EDITOR"), eq("ACTIVE"));
    }

    @Test
    void submitSeriesRejectsMoreThanTwentyFiles() {
        MangaSeries series = new MangaSeries();
        series.setSeriesId(20L);
        series.setAuthor(user(1L, EMAIL));
        series.setStatus("DRAFT");
        when(mangaSeriesRepository.findById(20L)).thenReturn(Optional.of(series));
        List<MultipartFile> files = java.util.stream.IntStream.range(0, 21)
                .mapToObj(index -> (MultipartFile) seriesFile())
                .toList();

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.submitSeriesWithFiles(20L, files));

        assertSame(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verify(seriesFileRepository, never()).save(any());
    }

    @Test
    void submitSeriesRejectsOversizedNonZipFile() {
        MangaSeries series = new MangaSeries();
        series.setSeriesId(20L);
        series.setAuthor(user(1L, EMAIL));
        series.setStatus("DRAFT");
        MultipartFile file = sizedFile("storyboard.pdf", (20L * 1024 * 1024) + 1);
        when(mangaSeriesRepository.findById(20L)).thenReturn(Optional.of(series));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.submitSeriesWithFiles(20L, List.of(file)));

        assertSame(HttpStatus.PAYLOAD_TOO_LARGE, exception.getStatusCode());
        verify(seriesFileRepository, never()).save(any());
    }

    @Test
    void submitSeriesRejectsMoreThanTwoHundredMegabytesInTotal() {
        MangaSeries series = new MangaSeries();
        series.setSeriesId(20L);
        series.setAuthor(user(1L, EMAIL));
        series.setStatus("DRAFT");
        List<MultipartFile> files = List.of(
                sizedFile("part-1.zip", 70L * 1024 * 1024),
                sizedFile("part-2.zip", 70L * 1024 * 1024),
                sizedFile("part-3.zip", 70L * 1024 * 1024));
        when(mangaSeriesRepository.findById(20L)).thenReturn(Optional.of(series));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.submitSeriesWithFiles(20L, files));

        assertSame(HttpStatus.PAYLOAD_TOO_LARGE, exception.getStatusCode());
        verify(seriesFileRepository, never()).save(any());
    }

    @Test
    void getsChaptersForOwnedSeries() {
        MangaSeries series = new MangaSeries();
        series.setSeriesId(20L);
        series.setAuthor(user(1L, EMAIL));
        Chapter chapter = new Chapter();
        chapter.setChapterId(30L);
        chapter.setSeries(series);
        chapter.setChapterNumber(1);
        chapter.setTitle("Chapter 1");
        chapter.setStatus("DRAFT");
        when(mangaSeriesRepository.findById(20L)).thenReturn(Optional.of(series));
        when(chapterRepository.findBySeriesSeriesIdOrderByChapterNumberAsc(20L))
                .thenReturn(List.of(chapter));

        var response = service.getSeriesChapters(20L);

        assertEquals(1, response.size());
        assertEquals(30L, response.get(0).id());
        assertEquals(1, response.get(0).chapterNumber());
    }

    @Test
    void submitChapterToEditorStoresImageFilesAndNotifiesAssignedEditor(@TempDir Path tempDir) {
        ReflectionTestUtils.setField(service, "seriesFileUploadRootOverride", tempDir.toString());
        User mangaka = user(1L, EMAIL);
        User editor = tantouEditor(3L, "editor@manga.test");
        MangaSeries series = new MangaSeries();
        series.setSeriesId(20L);
        series.setAuthor(mangaka);
        series.setTantouEditor(editor);
        Chapter chapter = new Chapter();
        chapter.setChapterId(30L);
        chapter.setSeries(series);
        chapter.setTitle("Chapter 1");
        chapter.setStatus("DRAFT");

        when(chapterRepository.findById(30L)).thenReturn(Optional.of(chapter));
        when(chapterRepository.save(chapter)).thenReturn(chapter);
        List<SeriesFile> savedFiles = new ArrayList<>();
        when(seriesFileRepository.save(any(SeriesFile.class))).thenAnswer(invocation -> {
            SeriesFile file = invocation.getArgument(0);
            file.setFileId((long) savedFiles.size() + 1);
            savedFiles.add(file);
            return file;
        });
        when(seriesFileRepository
                .findByChapterChapterIdAndPurposeAndActiveTrueOrderByUploadedAtAsc(
                        30L,
                        "CHAPTER_MANUSCRIPT"))
                .thenAnswer(invocation -> List.copyOf(savedFiles));
        MockMultipartFile firstImage = new MockMultipartFile(
                "files", "page-1.png", "image/png", "page 1".getBytes(StandardCharsets.UTF_8));
        MockMultipartFile secondImage = new MockMultipartFile(
                "files", "page-2.jpg", "image/jpeg", "page 2".getBytes(StandardCharsets.UTF_8));

        var response = service.submitChapterToEditor(
                30L,
                List.of(firstImage, secondImage));

        assertEquals("SUBMITTED_TO_EDITOR", chapter.getStatus());
        assertEquals(2, response.manuscriptFiles().size());
        assertEquals("page-1.png", response.manuscriptFiles().get(0).originalFileName());
        assertEquals("page-2.jpg", response.manuscriptFiles().get(1).originalFileName());
        verify(seriesFileRepository).deactivateActiveChapterFiles(30L, "CHAPTER_MANUSCRIPT");
        verify(seriesFileRepository, times(2)).save(any(SeriesFile.class));
        assertTrue(savedFiles.stream().allMatch(file -> chapter.equals(file.getChapter())));
        assertTrue(savedFiles.stream().allMatch(file -> "CHAPTER_MANUSCRIPT".equals(file.getPurpose())));
        assertTrue(savedFiles.stream()
                .allMatch(file -> Files.exists(tempDir.resolve("series-20").resolve(file.getFileName()))));

        ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(notificationCaptor.capture());
        Notification notification = notificationCaptor.getValue();
        assertEquals(editor, notification.getUser());
        assertEquals("NEW_CHAPTER_SUBMISSION", notification.getType());
        assertEquals(30L, notification.getReferenceId());
    }

    @Test
    void submitChapterToEditorRejectsMixedZipAndImages() {
        User mangaka = user(1L, EMAIL);
        User editor = tantouEditor(3L, "editor@manga.test");
        MangaSeries series = new MangaSeries();
        series.setSeriesId(20L);
        series.setAuthor(mangaka);
        series.setTantouEditor(editor);
        Chapter chapter = new Chapter();
        chapter.setChapterId(30L);
        chapter.setSeries(series);
        chapter.setTitle("Chapter 1");
        chapter.setStatus("DRAFT");
        when(chapterRepository.findById(30L)).thenReturn(Optional.of(chapter));
        MockMultipartFile image = new MockMultipartFile(
                "files", "page.png", "image/png", "page".getBytes(StandardCharsets.UTF_8));
        MockMultipartFile zip = new MockMultipartFile(
                "files", "chapter.zip", "application/zip", "zip".getBytes(StandardCharsets.UTF_8));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> service.submitChapterToEditor(30L, List.of(image, zip)));

        assertSame(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Upload either one ZIP file or one or more image files", exception.getReason());
        verify(chapterRepository, never()).save(any());
        verify(seriesFileRepository, never()).save(any());
        verify(notificationRepository, never()).save(any());
    }

    @Test
    void submitChapterToEditorAcceptsOneZip(@TempDir Path tempDir) {
        ReflectionTestUtils.setField(service, "seriesFileUploadRootOverride", tempDir.toString());
        User mangaka = user(1L, EMAIL);
        MangaSeries series = new MangaSeries();
        series.setSeriesId(20L);
        series.setAuthor(mangaka);
        series.setTantouEditor(tantouEditor(3L, "editor@manga.test"));
        Chapter chapter = new Chapter();
        chapter.setChapterId(30L);
        chapter.setSeries(series);
        chapter.setTitle("Chapter 1");
        chapter.setStatus("DRAFT");
        when(chapterRepository.findById(30L)).thenReturn(Optional.of(chapter));
        when(chapterRepository.save(chapter)).thenReturn(chapter);
        when(seriesFileRepository.save(any(SeriesFile.class))).thenAnswer(invocation -> invocation.getArgument(0));
        MockMultipartFile zip = new MockMultipartFile(
                "files", "chapter.zip", "application/zip", "zip".getBytes(StandardCharsets.UTF_8));

        var response = service.submitChapterToEditor(30L, List.of(zip));

        assertEquals("SUBMITTED_TO_EDITOR", response.status());
        ArgumentCaptor<SeriesFile> fileCaptor = ArgumentCaptor.forClass(SeriesFile.class);
        verify(seriesFileRepository).save(fileCaptor.capture());
        assertEquals("chapter.zip", fileCaptor.getValue().getOriginalFileName());
        assertEquals("CHAPTER_MANUSCRIPT", fileCaptor.getValue().getPurpose());
    }

    @Test
    void submitChapterToEditorCannotBypassBoardReview() {
        User mangaka = user(1L, EMAIL);
        MangaSeries series = new MangaSeries();
        series.setSeriesId(20L);
        series.setAuthor(mangaka);
        series.setTantouEditor(tantouEditor(3L, "editor@manga.test"));
        Chapter chapter = new Chapter();
        chapter.setChapterId(30L);
        chapter.setSeries(series);
        chapter.setStatus("SUBMITTED_TO_BOARD");
        when(chapterRepository.findById(30L)).thenReturn(Optional.of(chapter));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.submitChapterToEditor(
                        30L, List.of(seriesFile())));

        assertSame(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(chapterRepository, never()).save(any());
    }

    @Test
    void getsPagesForOwnedChapter() {
        MangaSeries series = new MangaSeries();
        series.setSeriesId(20L);
        series.setAuthor(user(1L, EMAIL));
        Chapter chapter = new Chapter();
        chapter.setChapterId(30L);
        chapter.setSeries(series);
        ChapterPage page = new ChapterPage();
        page.setPageId(40L);
        page.setChapter(chapter);
        page.setPageNumber(1);
        page.setImageUrl("/static/covers/page-1.png");
        page.setPageStatus("DRAFT");
        when(chapterRepository.findById(30L)).thenReturn(Optional.of(chapter));
        when(chapterPageRepository.findByChapterChapterIdOrderByPageNumberAsc(30L))
                .thenReturn(List.of(page));

        var response = service.getChapterPages(30L);

        assertEquals(1, response.size());
        assertEquals(40L, response.get(0).id());
        assertEquals(30L, response.get(0).chapterId());
        assertEquals(1, response.get(0).pageNumber());
        assertEquals("/static/covers/page-1.png", response.get(0).imageUrl());
    }

    @Test
    void uploadsImagesAsChapterPagesForOwnedChapter(@TempDir Path tempDir) {
        ReflectionTestUtils.setField(service, "pageImageUploadRootOverride", tempDir.toString());
        MangaSeries series = new MangaSeries();
        series.setSeriesId(20L);
        series.setAuthor(user(1L, EMAIL));
        Chapter chapter = new Chapter();
        chapter.setChapterId(30L);
        chapter.setSeries(series);
        MockMultipartFile image = new MockMultipartFile(
                "images", "page-1.png", "image/png", "page image".getBytes(StandardCharsets.UTF_8));
        when(chapterRepository.findById(30L)).thenReturn(Optional.of(chapter));
        when(chapterPageRepository.findByChapterChapterIdOrderByPageNumberAsc(30L))
                .thenReturn(List.of());
        when(chapterPageRepository.save(any(ChapterPage.class))).thenAnswer(invocation -> {
            ChapterPage page = invocation.getArgument(0);
            page.setPageId(40L);
            return page;
        });

        var response = service.uploadChapterPages(30L, List.of(image));

        assertEquals(1, response.size());
        assertEquals(40L, response.get(0).id());
        assertEquals(1, response.get(0).pageNumber());
        assertTrue(response.get(0).imageUrl().startsWith("pages/chapter-30/chapter-30-page-1-"));
        String savedFileName = Path.of(response.get(0).imageUrl()).getFileName().toString();
        assertTrue(Files.exists(tempDir.resolve("chapter-30").resolve(savedFileName)));
    }

    @Test
    void rejectsOversizedPageImage(@TempDir Path tempDir) {
        ReflectionTestUtils.setField(service, "pageImageUploadRootOverride", tempDir.toString());
        MangaSeries series = new MangaSeries();
        series.setSeriesId(20L);
        series.setAuthor(user(1L, EMAIL));
        Chapter chapter = new Chapter();
        chapter.setChapterId(30L);
        chapter.setSeries(series);
        byte[] content = new byte[(5 * 1024 * 1024) + 1];
        MockMultipartFile image = new MockMultipartFile(
                "images", "page-1.png", "image/png", content);
        when(chapterRepository.findById(30L)).thenReturn(Optional.of(chapter));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> service.uploadChapterPages(30L, List.of(image)));

        assertSame(HttpStatus.PAYLOAD_TOO_LARGE, exception.getStatusCode());
        verify(chapterPageRepository, never()).save(any());
    }

    @Test
    void assignTaskStoresOriginalFilesAndMarkupAndNotifiesAssistant(@TempDir Path tempDir) {
        ReflectionTestUtils.setField(service, "taskMarkupUploadRootOverride", tempDir.toString());
        User mangaka = user(1L, EMAIL);
        User assistant = user(2L, "assistant@manga.test");
        assistant.setStatus("ACTIVE");
        assistant.setRole(role("ASSISTANT"));
        MangaSeries series = new MangaSeries();
        series.setSeriesId(20L);
        series.setAuthor(mangaka);
        Chapter chapter = new Chapter();
        chapter.setChapterId(30L);
        chapter.setSeries(series);
        ChapterPage page = new ChapterPage();
        page.setPageId(40L);
        page.setPageNumber(2);
        page.setChapter(chapter);

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(mangaka));
        when(chapterPageRepository.findById(40L)).thenReturn(Optional.of(page));
        when(userRepository.findById(2L)).thenReturn(Optional.of(assistant));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task saved = invocation.getArgument(0);
            saved.setTaskId(60L);
            return saved;
        });
        when(taskMarkupPageRepository.save(any(TaskMarkupPage.class))).thenAnswer(invocation -> {
            TaskMarkupPage saved = invocation.getArgument(0);
            saved.setMarkupPageId(80L);
            return saved;
        });

        MockMultipartFile markupImage = new MockMultipartFile(
                "markupImages",
                "note.png",
                "image/png",
                "markup".getBytes(StandardCharsets.UTF_8));
        MockMultipartFile originalFile = new MockMultipartFile(
                "originalFiles",
                "source.zip",
                "application/zip",
                "source".getBytes(StandardCharsets.UTF_8));

        var response = service.assignTask(new AssignTaskRequest(
                40L,
                2L,
                "BACKGROUND",
                "Draw background",
                "City background",
                LocalDateTime.now().plusDays(2),
                0f,
                0f,
                100f,
                100f,
                List.of(markupImage),
                "[{\"objects\":[{\"type\":\"rect\"}]}]",
                List.of(originalFile)));

        ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository).save(taskCaptor.capture());
        Task savedTask = taskCaptor.getValue();
        assertEquals(1, savedTask.getRoundNumber());
        assertEquals(1, response.roundNumber());
        verify(taskFileStorageService).storeTaskFiles(
                savedTask,
                mangaka,
                List.of(originalFile),
                1,
                TaskFileStorageService.TASK_ORIGINAL);

        ArgumentCaptor<TaskMarkupPage> markupCaptor = ArgumentCaptor.forClass(TaskMarkupPage.class);
        verify(taskMarkupPageRepository).save(markupCaptor.capture());
        TaskMarkupPage markupPage = markupCaptor.getValue();
        assertEquals(1, markupPage.getRoundNumber());
        assertEquals(1, markupPage.getOrderIndex());
        assertTrue(markupPage.getCanvasData().contains("\"type\":\"rect\""));
        Path storedMarkup = tempDir.resolve(
                markupPage.getImageUrl().substring("task-markups/".length()));
        assertTrue(Files.exists(storedMarkup));

        ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(notificationCaptor.capture());
        Notification notification = notificationCaptor.getValue();
        assertEquals(assistant, notification.getUser());
        assertEquals("TASK_ASSIGNED", notification.getType());
        assertEquals(60L, notification.getReferenceId());
    }

    @Test
    void reviseTaskIncrementsRoundAndKeepsAssignedAssistant() {
        User mangaka = user(1L, EMAIL);
        User assistant = user(2L, "assistant@manga.test");
        MangaSeries series = new MangaSeries();
        series.setSeriesId(20L);
        series.setAuthor(mangaka);
        Chapter chapter = new Chapter();
        chapter.setChapterId(30L);
        chapter.setSeries(series);
        Task task = new Task();
        task.setTaskId(60L);
        task.setChapter(chapter);
        task.setAssignedBy(mangaka);
        task.setAssignedTo(assistant);
        task.setTitle("Background");
        task.setStatus("REVISION_REQUESTED");
        task.setRoundNumber(1);
        MockMultipartFile originalFile = new MockMultipartFile(
                "originalFiles",
                "revision.zip",
                "application/zip",
                "revision".getBytes(StandardCharsets.UTF_8));

        when(taskRepository.findById(60L)).thenReturn(Optional.of(task));
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(mangaka));
        when(taskRepository.save(task)).thenReturn(task);

        var response = service.reviseTask(
                60L,
                new ReviseTaskRequest(List.of(), null, List.of(originalFile)));

        assertEquals(2, task.getRoundNumber());
        assertEquals("ASSIGNED", task.getStatus());
        assertEquals(2, response.roundNumber());
        assertEquals(assistant.getUserId(), response.assistantId());
        verify(taskFileStorageService).storeTaskFiles(
                task,
                mangaka,
                List.of(originalFile),
                2,
                TaskFileStorageService.TASK_ORIGINAL);
        ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(notificationCaptor.capture());
        assertEquals("TASK_REVISED", notificationCaptor.getValue().getType());
    }

    @Test
    void reviewSubmissionNotifiesAssistantWithDecision() {
        User mangaka = user(1L, EMAIL);
        User assistant = user(2L, "assistant@manga.test");
        MangaSeries series = new MangaSeries();
        series.setSeriesId(20L);
        series.setAuthor(mangaka);
        Chapter chapter = new Chapter();
        chapter.setChapterId(30L);
        chapter.setSeries(series);
        Task task = new Task();
        task.setTaskId(60L);
        task.setStatus("SUBMITTED");
        Submission submission = new Submission();
        submission.setSubmissionId(70L);
        submission.setTask(task);
        submission.setChapter(chapter);
        submission.setSubmittedBy(assistant);
        submission.setStatus("SUBMITTED");

        when(submissionRepository.findById(70L)).thenReturn(Optional.of(submission));
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(mangaka));
        when(submissionRepository.save(submission)).thenReturn(submission);

        var response = service.reviewSubmission(70L, new ReviewSubmissionRequest("APPROVED", "Good"));

        assertEquals("APPROVED", response.status());
        assertEquals("APPROVED", task.getStatus());
        verify(taskRepository).save(task);

        ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(notificationCaptor.capture());
        Notification notification = notificationCaptor.getValue();
        assertEquals(assistant, notification.getUser());
        assertEquals("TASK_APPROVED", notification.getType());
        assertEquals(60L, notification.getReferenceId());
    }

    @Test
    void reviewSubmissionRejectsEarlierTaskRound() {
        User mangaka = user(1L, EMAIL);
        MangaSeries series = new MangaSeries();
        series.setSeriesId(20L);
        series.setAuthor(mangaka);
        Chapter chapter = new Chapter();
        chapter.setChapterId(30L);
        chapter.setSeries(series);
        Task task = new Task();
        task.setTaskId(60L);
        task.setChapter(chapter);
        task.setRoundNumber(2);
        Submission submission = new Submission();
        submission.setSubmissionId(70L);
        submission.setTask(task);
        submission.setChapter(chapter);
        submission.setStatus("SUBMITTED");
        submission.setRoundNumber(1);
        when(submissionRepository.findById(70L)).thenReturn(Optional.of(submission));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.reviewSubmission(
                        70L,
                        new ReviewSubmissionRequest("APPROVED", null)));

        assertSame(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(submissionRepository, never()).save(any());
    }

    @Test
    void createsAssistantAccountForAuthenticatedMangaka() {
        User mangaka = user(1L, EMAIL);
        Role assistantRole = role("ASSISTANT");
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(mangaka));
        when(userRepository.existsByUsername("Assistant One")).thenReturn(false);
        when(userRepository.existsByEmail("assistant1@manga.test")).thenReturn(false);
        when(roleRepository.findByRoleName("ASSISTANT")).thenReturn(Optional.of(assistantRole));
        when(passwordEncoder.encode("Assistant@123")).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            saved.setUserId(50L);
            return saved;
        });

        var response = service.createAssistant(new CreateAssistantRequest(
                "Assistant One",
                "assistant1@manga.test",
                "Assistant@123",
                "avatar.png"));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();
        assertEquals(50L, response.id());
        assertEquals("Assistant One", response.username());
        assertEquals("assistant1@manga.test", response.email());
        assertEquals("hashed-password", saved.getPassword());
        assertEquals("ACTIVE", saved.getStatus());
        assertEquals("ASSISTANT", saved.getRole().getRoleName());
        assertEquals(mangaka, saved.getCreatedBy());
    }

    private User user(Long id, String email) {
        User user = new User();
        user.setUserId(id);
        user.setEmail(email);
        user.setUsername(email);
        return user;
    }

    private MockMultipartFile seriesFile() {
        return new MockMultipartFile(
                "files",
                "storyboard.pdf",
                "application/pdf",
                "series dossier".getBytes(StandardCharsets.UTF_8));
    }

    private MultipartFile sizedFile(String originalFilename, long size) {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn(originalFilename);
        when(file.getSize()).thenReturn(size);
        return file;
    }

    private Role role(String name) {
        Role role = new Role();
        role.setRoleName(name);
        return role;
    }

    private User tantouEditor(Long id, String email) {
        User editor = user(id, email);
        editor.setStatus("ACTIVE");
        editor.setRole(role("TANTOU_EDITOR"));
        return editor;
    }
}

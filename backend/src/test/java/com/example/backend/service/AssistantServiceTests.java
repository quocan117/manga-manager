package com.example.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import com.example.backend.dto.AssistantDtos.SubmitTaskRequest;
import com.example.backend.dto.DrawingDtos.SaveDrawingRequest;
import com.example.backend.model.Chapter;
import com.example.backend.model.ChapterPage;
import com.example.backend.model.MangaSeries;
import com.example.backend.model.Notification;
import com.example.backend.model.PageDrawing;
import com.example.backend.model.Submission;
import com.example.backend.model.Task;
import com.example.backend.model.User;
import com.example.backend.repository.NotificationRepository;
import com.example.backend.repository.PageDrawingRepository;
import com.example.backend.repository.PageDrawingRevisionRepository;
import com.example.backend.repository.SeriesFileRepository;
import com.example.backend.repository.SubmissionRepository;
import com.example.backend.repository.TaskRepository;
import com.example.backend.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class AssistantServiceTests {
    private static final String EMAIL = "assistant@manga.test";

    @Mock
    private TaskRepository taskRepository;
    @Mock
    private SubmissionRepository submissionRepository;
    @Mock
    private PageDrawingRepository drawingRepository;
    @Mock
    private PageDrawingRevisionRepository revisionRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private SeriesFileRepository seriesFileRepository;

    private AssistantService service;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        service = new AssistantService(
                taskRepository,
                submissionRepository,
                drawingRepository,
                revisionRepository,
                userRepository,
                notificationRepository,
                seriesFileRepository,
                objectMapper);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(EMAIL, null, List.of()));
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void acceptsAssignedTask() {
        Task task = task(10L, user(1L, EMAIL), "ASSIGNED");
        when(taskRepository.findById(10L)).thenReturn(Optional.of(task));
        when(taskRepository.save(task)).thenReturn(task);
        when(submissionRepository.findByTaskTaskIdOrderBySubmittedAtDesc(10L)).thenReturn(List.of());

        var response = service.acceptTask(10L);

        assertEquals("IN_PROGRESS", task.getStatus());
        assertEquals("IN_PROGRESS", response.status());
        verify(taskRepository).save(task);
    }

    @Test
    void rejectsTaskAssignedToAnotherAssistant() {
        Task task = task(10L, user(2L, "other@manga.test"), "ASSIGNED");
        when(taskRepository.findById(10L)).thenReturn(Optional.of(task));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.acceptTask(10L));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        verify(taskRepository, never()).save(any());
    }

    @Test
    void savesTaskDrawingAndMovesTaskInProgress() throws Exception {
        User assistant = user(1L, EMAIL);
        Task task = task(10L, assistant, "ASSIGNED");
        when(taskRepository.findById(10L)).thenReturn(Optional.of(task));
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(assistant));
        when(drawingRepository.findByTaskTaskIdAndOwnerEmail(10L, EMAIL)).thenReturn(Optional.empty());
        when(drawingRepository.saveAndFlush(any(PageDrawing.class))).thenAnswer(invocation -> {
            PageDrawing drawing = invocation.getArgument(0);
            drawing.setDrawingId(20L);
            drawing.setVersion(0L);
            return drawing;
        });

        var response = service.saveDrawing(
                10L,
                new SaveDrawingRequest(
                        objectMapper.readTree("{\"objects\":[{\"type\":\"image\"}]}"),
                        "preview.png",
                        null,
                        null));

        assertEquals(20L, response.id());
        assertEquals(10L, response.taskId());
        assertEquals("IN_PROGRESS", task.getStatus());
        assertEquals(1, response.canvasData().get("objects").size());
        verify(taskRepository).save(task);
        verify(revisionRepository).save(any());
    }

    @Test
    void submitsTaskUsingDrawingPreview() {
        User assistant = user(1L, EMAIL);
        Task task = task(10L, assistant, "IN_PROGRESS");
        PageDrawing drawing = drawing(task, assistant, 3L, "preview.png");
        when(taskRepository.findById(10L)).thenReturn(Optional.of(task));
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(assistant));
        when(drawingRepository.findByTaskTaskIdAndOwnerEmail(10L, EMAIL)).thenReturn(Optional.of(drawing));
        when(submissionRepository.save(any(Submission.class))).thenAnswer(invocation -> {
            Submission submission = invocation.getArgument(0);
            submission.setSubmissionId(30L);
            return submission;
        });

        var response = service.submitTask(10L, new SubmitTaskRequest(null, "source-final.psd", "Done", 3L));

        assertEquals(30L, response.id());
        assertEquals("SUBMITTED", response.status());
        assertEquals("preview.png", response.artifactUrl());
        assertEquals("source-final.psd", response.originalFileUrl());
        assertEquals("SUBMITTED", task.getStatus());
        verify(taskRepository).save(task);
        ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(notificationCaptor.capture());
        Notification notification = notificationCaptor.getValue();
        assertEquals(task.getAssignedBy(), notification.getUser());
        assertEquals("TASK_SUBMITTED", notification.getType());
        assertEquals(10L, notification.getReferenceId());
    }

    @Test
    void getsNotificationsForCurrentAssistant() {
        Notification notification = notification(70L, user(1L, EMAIL), false);
        when(notificationRepository.findByUserEmailOrderByCreatedAtDesc(EMAIL))
                .thenReturn(List.of(notification));

        var response = service.getNotifications();

        assertEquals(1, response.size());
        assertEquals(70L, response.get(0).id());
        assertEquals("TASK_ASSIGNED", response.get(0).type());
        assertEquals(false, response.get(0).isRead());
    }

    @Test
    void marksNotificationReadForCurrentAssistant() {
        Notification notification = notification(70L, user(1L, EMAIL), false);
        when(notificationRepository.findByNotificationIdAndUserEmail(70L, EMAIL))
                .thenReturn(Optional.of(notification));
        when(notificationRepository.save(notification)).thenReturn(notification);

        var response = service.markNotificationRead(70L);

        assertEquals(true, response.isRead());
        verify(notificationRepository).save(notification);
    }

    private Task task(Long id, User assistant, String status) {
        MangaSeries series = new MangaSeries();
        series.setSeriesId(40L);
        series.setTitle("Series");
        Chapter chapter = new Chapter();
        chapter.setChapterId(50L);
        chapter.setChapterNumber(1);
        chapter.setTitle("Chapter 1");
        chapter.setSeries(series);
        ChapterPage page = new ChapterPage();
        page.setPageId(60L);
        page.setPageNumber(1);
        page.setChapter(chapter);

        Task task = new Task();
        task.setTaskId(id);
        task.setAssignedTo(assistant);
        task.setAssignedBy(user(3L, "mangaka@manga.test"));
        task.setChapter(chapter);
        task.setPage(page);
        task.setTaskType("BACKGROUND");
        task.setTitle("Draw background");
        task.setDescription("Draw the city");
        task.setStatus(status);
        task.setDueDate(LocalDateTime.now().plusDays(3));
        task.setCreatedAt(LocalDateTime.now());
        return task;
    }

    private PageDrawing drawing(Task task, User assistant, Long version, String previewImageUrl) {
        PageDrawing drawing = new PageDrawing();
        drawing.setDrawingId(20L);
        drawing.setTask(task);
        drawing.setPage(task.getPage());
        drawing.setOwner(assistant);
        drawing.setCanvasData("{}");
        drawing.setPreviewImageUrl(previewImageUrl);
        drawing.setStatus("FINALIZED");
        drawing.setVersion(version);
        return drawing;
    }

    private Notification notification(Long id, User user, boolean isRead) {
        Notification notification = new Notification();
        notification.setNotificationId(id);
        notification.setUser(user);
        notification.setType("TASK_ASSIGNED");
        notification.setReferenceId(10L);
        notification.setMessage("New task assigned");
        notification.setIsRead(isRead);
        notification.setCreatedAt(LocalDateTime.now());
        return notification;
    }

    private User user(Long id, String email) {
        User user = new User();
        user.setUserId(id);
        user.setEmail(email);
        user.setUsername(email);
        return user;
    }
}

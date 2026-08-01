package com.example.backend.scheduler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.backend.model.MangaSeries;
import com.example.backend.model.Notification;
import com.example.backend.model.SeriesEditorRejection;
import com.example.backend.model.User;
import com.example.backend.repository.MangaSeriesRepository;
import com.example.backend.repository.NotificationRepository;
import com.example.backend.repository.SeriesEditorRejectionRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.service.MangakaService;
import com.example.backend.service.SeriesHistoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@ExtendWith(MockitoExtension.class)
class EditorAssignmentJobTests {

    @Mock
    private MangaSeriesRepository mangaSeriesRepository;
    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private SeriesEditorRejectionRepository seriesEditorRejectionRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private MangakaService mangakaService;
    @Mock
    private SeriesHistoryService seriesHistoryService;

    private EditorAssignmentJob job;

    @BeforeEach
    void setUp() {
        job = new EditorAssignmentJob(
                mangaSeriesRepository,
                notificationRepository,
                seriesEditorRejectionRepository,
                userRepository,
                mangakaService,
                seriesHistoryService);
    }

    @Test
    void reassignsOverdueSeriesWithoutUsingCurrentOrRejectedEditors() {
        User oldEditor = user(2L, "old-editor@manga.test");
        User rejectedEditor = user(3L, "rejected-editor@manga.test");
        User nextEditor = user(4L, "next-editor@manga.test");
        MangaSeries series = overdueSeries(oldEditor);
        SeriesEditorRejection rejection = rejection(series, rejectedEditor);

        when(mangaSeriesRepository.findOverdueUnlockedSeries(eq("PENDING_EDITOR"), any(LocalDateTime.class)))
                .thenReturn(List.of(series));
        when(seriesEditorRejectionRepository.findBySeriesSeriesId(10L)).thenReturn(List.of(rejection));
        when(notificationRepository.findByReferenceIdAndUserUserIdAndTypeInAndIsReadFalse(
                eq(10L), eq(2L), any())).thenReturn(List.of());
        when(mangakaService.findEditorWithLeastWorkloadExcluding(Set.of(2L, 3L)))
                .thenReturn(Optional.of(nextEditor));

        job.revokeAndReassignOverdueSeries();

        assertEquals(nextEditor, series.getTantouEditor());
        assertEquals("PENDING_EDITOR", series.getStatus());
        verify(mangakaService).findEditorWithLeastWorkloadExcluding(Set.of(2L, 3L));
        verify(seriesHistoryService).record(
                series,
                oldEditor,
                "AUTO_EDITOR_REASSIGNED",
                "PENDING_EDITOR",
                "PENDING_EDITOR",
                "The previous editor did not accept within 24 hours; reassigned to Next Editor",
                4L);
        verify(notificationRepository, times(3)).save(any(Notification.class));
    }

    @Test
    void movesSeriesToBoardAssignmentWhenNoEligibleEditorRemains() {
        User oldEditor = user(2L, "old-editor@manga.test");
        User rejectedEditor = user(3L, "rejected-editor@manga.test");
        User board = user(9L, "board@manga.test");
        MangaSeries series = overdueSeries(oldEditor);
        SeriesEditorRejection rejection = rejection(series, rejectedEditor);

        when(mangaSeriesRepository.findOverdueUnlockedSeries(eq("PENDING_EDITOR"), any(LocalDateTime.class)))
                .thenReturn(List.of(series));
        when(seriesEditorRejectionRepository.findBySeriesSeriesId(10L)).thenReturn(List.of(rejection));
        when(notificationRepository.findByReferenceIdAndUserUserIdAndTypeInAndIsReadFalse(
                eq(10L), eq(2L), any())).thenReturn(List.of());
        when(mangakaService.findEditorWithLeastWorkloadExcluding(Set.of(2L, 3L)))
                .thenReturn(Optional.empty());
        when(userRepository.findByRoleRoleNameAndStatusOrderByUsernameAsc("EDITORIAL_BOARD", "ACTIVE"))
                .thenReturn(List.of(board));

        job.revokeAndReassignOverdueSeries();

        assertEquals("EDITOR_ASSIGNMENT_REQUIRED", series.getStatus());
        assertNull(series.getTantouEditor());
        assertNull(series.getEditorAssignedAt());
        assertFalse(Boolean.TRUE.equals(series.getEditorAssignmentLocked()));
        verify(seriesHistoryService).record(
                series,
                oldEditor,
                "AUTO_EDITOR_ASSIGNMENT_REQUIRED",
                "PENDING_EDITOR",
                "EDITOR_ASSIGNMENT_REQUIRED",
                "No eligible editor remains after excluding timed-out and previously rejected editors",
                10L);
        verify(notificationRepository, times(3)).save(any(Notification.class));
    }

    private MangaSeries overdueSeries(User oldEditor) {
        MangaSeries series = new MangaSeries();
        series.setSeriesId(10L);
        series.setTitle("Series");
        series.setStatus("PENDING_EDITOR");
        series.setAuthor(user(1L, "author@manga.test"));
        series.setTantouEditor(oldEditor);
        series.setEditorAssignedAt(LocalDateTime.now().minusHours(25));
        series.setEditorAssignmentLocked(false);
        return series;
    }

    private SeriesEditorRejection rejection(MangaSeries series, User editor) {
        SeriesEditorRejection rejection = new SeriesEditorRejection();
        rejection.setSeries(series);
        rejection.setEditor(editor);
        rejection.setReason("Previously rejected");
        rejection.setRejectedAt(LocalDateTime.now().minusDays(1));
        return rejection;
    }

    private User user(Long id, String email) {
        User user = new User();
        user.setUserId(id);
        user.setEmail(email);
        user.setUsername(id == 4L ? "Next Editor" : "User " + id);
        user.setStatus("ACTIVE");
        return user;
    }
}

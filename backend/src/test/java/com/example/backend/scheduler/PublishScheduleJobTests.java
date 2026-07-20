package com.example.backend.scheduler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.backend.model.Chapter;
import com.example.backend.model.MangaSeries;
import com.example.backend.model.Notification;
import com.example.backend.model.PublishSchedule;
import com.example.backend.model.User;
import com.example.backend.repository.ChapterRepository;
import com.example.backend.repository.NotificationRepository;
import com.example.backend.repository.PublishScheduleRepository;
import com.example.backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class PublishScheduleJobTests {

    @Mock
    private PublishScheduleRepository publishScheduleRepository;
    @Mock
    private ChapterRepository chapterRepository;
    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private UserRepository userRepository;

    @Test
    void publishesEarliestApprovedChapterAndMovesWeeklySchedule() {
        PublishScheduleJob job = new PublishScheduleJob(
                publishScheduleRepository, chapterRepository, notificationRepository, userRepository);
        LocalDateTime dueDate = LocalDateTime.now().minusDays(1);
        MangaSeries series = series(10L);
        PublishSchedule schedule = schedule(20L, series, dueDate, "WEEKLY");
        Chapter chapter = chapter(30L, series, 1);

        when(publishScheduleRepository.findByStatusIgnoreCaseAndPublishDateLessThanEqualOrderByPublishDateAsc(
                eq("PLANNED"), any(LocalDateTime.class))).thenReturn(List.of(schedule));
        when(chapterRepository.findFirstBySeriesSeriesIdAndStatusIgnoreCaseOrderByChapterNumberAsc(
                10L, "APPROVED")).thenReturn(Optional.of(chapter));

        job.publishDueChapters();

        assertEquals("PUBLISHED", chapter.getStatus());
        assertNotNull(chapter.getReleaseDate());
        assertEquals(dueDate.plusDays(7), schedule.getPublishDate());
        verify(chapterRepository).save(chapter);
        verify(publishScheduleRepository).save(schedule);

        ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(notificationCaptor.capture());
        Notification notification = notificationCaptor.getValue();
        assertEquals(series.getAuthor(), notification.getUser());
        assertEquals("CHAPTER_PUBLISHED", notification.getType());
        assertEquals(30L, notification.getReferenceId());
    }

    @Test
    void skipsDueScheduleWhenNoChapterIsReady() {
        PublishScheduleJob job = new PublishScheduleJob(
                publishScheduleRepository, chapterRepository, notificationRepository, userRepository);
        LocalDateTime dueDate = LocalDateTime.now().minusDays(1);
        MangaSeries series = series(10L);
        PublishSchedule schedule = schedule(20L, series, dueDate, "MONTHLY");

        when(publishScheduleRepository.findByStatusIgnoreCaseAndPublishDateLessThanEqualOrderByPublishDateAsc(
                eq("PLANNED"), any(LocalDateTime.class))).thenReturn(List.of(schedule));
        when(chapterRepository.findFirstBySeriesSeriesIdAndStatusIgnoreCaseOrderByChapterNumberAsc(
                10L, "APPROVED")).thenReturn(Optional.empty());

        job.publishDueChapters();

        assertEquals(dueDate, schedule.getPublishDate());
        assertTrue(schedule.getOverdueNotified());
        verify(chapterRepository, never()).save(any());
        verify(publishScheduleRepository).save(schedule);

        ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(notificationCaptor.capture());
        Notification notification = notificationCaptor.getValue();
        assertEquals(series.getTantouEditor(), notification.getUser());
        assertEquals("SCHEDULE_OVERDUE", notification.getType());
        assertEquals(20L, notification.getReferenceId());
    }

    private MangaSeries series(Long id) {
        MangaSeries series = new MangaSeries();
        series.setSeriesId(id);
        series.setTitle("Series " + id);
        series.setAuthor(user(1L, "author@manga.test"));
        series.setTantouEditor(user(2L, "tantou@manga.test"));
        return series;
    }

    private PublishSchedule schedule(Long id, MangaSeries series, LocalDateTime publishDate, String frequency) {
        PublishSchedule schedule = new PublishSchedule();
        schedule.setScheduleId(id);
        schedule.setSeries(series);
        schedule.setPublishDate(publishDate);
        schedule.setFrequency(frequency);
        schedule.setStatus("PLANNED");
        return schedule;
    }

    private Chapter chapter(Long id, MangaSeries series, Integer chapterNumber) {
        Chapter chapter = new Chapter();
        chapter.setChapterId(id);
        chapter.setSeries(series);
        chapter.setChapterNumber(chapterNumber);
        chapter.setTitle("Chapter " + chapterNumber);
        chapter.setStatus("APPROVED");
        return chapter;
    }

    private User user(Long id, String email) {
        User user = new User();
        user.setUserId(id);
        user.setEmail(email);
        user.setUsername(email);
        return user;
    }
}

package com.example.backend.scheduler;

import com.example.backend.model.Chapter;
import com.example.backend.model.MangaSeries;
import com.example.backend.model.Notification;
import com.example.backend.model.PublishSchedule;
import com.example.backend.model.User;
import com.example.backend.repository.ChapterRepository;
import com.example.backend.repository.NotificationRepository;
import com.example.backend.repository.PublishScheduleRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Component
public class PublishScheduleJob {
    private static final String PLANNED_STATUS = "PLANNED";
    private static final String READY_CHAPTER_STATUS = "APPROVED";
    private static final String PUBLISHED_CHAPTER_STATUS = "PUBLISHED";

    private final PublishScheduleRepository publishScheduleRepository;
    private final ChapterRepository chapterRepository;
    private final NotificationRepository notificationRepository;

    public PublishScheduleJob(
            PublishScheduleRepository publishScheduleRepository,
            ChapterRepository chapterRepository,
            NotificationRepository notificationRepository) {
        this.publishScheduleRepository = publishScheduleRepository;
        this.chapterRepository = chapterRepository;
        this.notificationRepository = notificationRepository;
    }

    @Scheduled(fixedRateString = "${manga.publish-schedule.scan-rate-ms:60000}")
    @Transactional
    public void publishDueChapters() {
        LocalDateTime now = LocalDateTime.now();
        List<PublishSchedule> dueSchedules = publishScheduleRepository
                .findByStatusIgnoreCaseAndPublishDateLessThanEqualOrderByPublishDateAsc(PLANNED_STATUS, now);

        for (PublishSchedule schedule : dueSchedules) {
            MangaSeries series = schedule.getSeries();
            if (series == null || series.getSeriesId() == null) {
                continue;
            }

            LocalDateTime nextPublishDate = nextPublishDate(schedule.getPublishDate(), schedule.getFrequency(), now);
            if (nextPublishDate == null) {
                continue;
            }

            Optional<Chapter> readyChapter = chapterRepository
                    .findFirstBySeriesSeriesIdAndStatusIgnoreCaseOrderByChapterNumberAsc(
                            series.getSeriesId(), READY_CHAPTER_STATUS);
            if (readyChapter.isEmpty()) {
                continue;
            }

            Chapter chapter = readyChapter.get();
            chapter.setStatus(PUBLISHED_CHAPTER_STATUS);
            chapter.setReleaseDate(now);
            chapterRepository.save(chapter);

            schedule.setPublishDate(nextPublishDate);
            publishScheduleRepository.save(schedule);

            notify(series.getAuthor(), "CHAPTER_PUBLISHED", chapter.getChapterId(),
                    "Chapter published by schedule: " + chapter.getTitle());
        }
    }

    private LocalDateTime nextPublishDate(LocalDateTime publishDate, String frequency, LocalDateTime now) {
        if (publishDate == null || frequency == null || frequency.isBlank()) {
            return null;
        }

        LocalDateTime next = publishDate;
        do {
            next = switch (frequency.trim().toUpperCase(Locale.ROOT)) {
                case "DAILY" -> next.plusDays(1);
                case "WEEKLY" -> next.plusDays(7);
                case "MONTHLY" -> next.plusDays(30);
                default -> null;
            };
        } while (next != null && !next.isAfter(now));

        return next;
    }

    private void notify(User user, String type, Long referenceId, String message) {
        if (user == null) {
            return;
        }

        Notification notification = new Notification();
        notification.setUser(user);
        notification.setType(type);
        notification.setReferenceId(referenceId);
        notification.setMessage(message);
        notification.setIsRead(false);
        notification.setCreatedAt(LocalDateTime.now());
        notificationRepository.save(notification);
    }
}

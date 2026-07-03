package com.example.backend.scheduler;

import com.example.backend.model.MangaSeries;
import com.example.backend.model.Notification;
import com.example.backend.model.User;
import com.example.backend.repository.MangaSeriesRepository;
import com.example.backend.repository.NotificationRepository;
import com.example.backend.service.MangakaService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Component
public class EditorAssignmentJob {
    private static final String ASSIGNMENT_TYPE = "SYSTEM_ASSIGNMENT";
    private static final List<String> ASSIGNMENT_TYPES = Arrays.asList("NEW_ASSIGNMENT", "SYSTEM_ASSIGNMENT");

    private final MangaSeriesRepository mangaSeriesRepository;
    private final NotificationRepository notificationRepository;
    private final MangakaService mangakaService;

    public EditorAssignmentJob(MangaSeriesRepository mangaSeriesRepository,
                               NotificationRepository notificationRepository,
                               MangakaService mangakaService) {
        this.mangaSeriesRepository = mangaSeriesRepository;
        this.notificationRepository = notificationRepository;
        this.mangakaService = mangakaService;
    }

    @Scheduled(fixedRate = 900000)
    @Transactional
    public void revokeAndReassignOverdueSeries() {
        LocalDateTime timeLimit = LocalDateTime.now().minusHours(24);
        List<MangaSeries> overdueSeries = mangaSeriesRepository
                .findByStatusIgnoreCaseAndEditorAssignedAtBefore("PENDING_EDITOR", timeLimit);

        for (MangaSeries series : overdueSeries) {
            User oldEditor = series.getTantouEditor();
            Long oldEditorId = oldEditor != null ? oldEditor.getUserId() : null;

            User newEditor = mangakaService.getEditorWithLeastWorkload(oldEditorId);

            if (oldEditor != null) {
                dismissAssignmentNotifications(series.getSeriesId(), oldEditor.getUserId());
            }

            series.setTantouEditor(newEditor);
            series.setEditorAssignedAt(LocalDateTime.now());
            mangaSeriesRepository.save(series);

            createNotification(series.getAuthor(), "SYSTEM", series.getSeriesId(),
                    "Hồ sơ series '" + series.getTitle() + "' đã được chuyển tự động sang Biên tập viên khác (" + newEditor.getUsername() + ") do người trước không phản hồi kịp.");

            createNotification(newEditor, ASSIGNMENT_TYPE, series.getSeriesId(),
                    "Bạn được hệ thống tự động điều phối hồ sơ series mới: '" + series.getTitle() + "'. Vui lòng nhận hồ sơ trong 24h.");

            if (oldEditor != null) {
                createNotification(oldEditor, "SYSTEM", series.getSeriesId(),
                        "Hồ sơ series '" + series.getTitle() + "' đã bị hệ thống thu hồi do quá 24h không tiếp nhận.");
            }
        }
    }

    private void dismissAssignmentNotifications(Long seriesId, Long userId) {
        List<Notification> stale = notificationRepository
                .findByReferenceIdAndUserUserIdAndTypeInAndIsReadFalse(seriesId, userId, ASSIGNMENT_TYPES);
        stale.forEach(n -> n.setIsRead(true));
        notificationRepository.saveAll(stale);
    }

    private void createNotification(User user, String type, Long referenceId, String message) {
        Notification notif = new Notification();
        notif.setUser(user);
        notif.setType(type);
        notif.setReferenceId(referenceId);
        notif.setMessage(message);
        notif.setCreatedAt(LocalDateTime.now());
        notif.setIsRead(false);
        notificationRepository.save(notif);
    }
}
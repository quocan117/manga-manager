package com.example.backend.scheduler;

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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component
public class EditorAssignmentJob {
    private static final String ASSIGNMENT_TYPE = "SYSTEM_ASSIGNMENT";
    private static final List<String> ASSIGNMENT_TYPES = Arrays.asList("NEW_ASSIGNMENT", "SYSTEM_ASSIGNMENT");
    private static final int MAX_AUTOMATIC_EDITOR_REJECTIONS = 3;

    private final MangaSeriesRepository mangaSeriesRepository;
    private final NotificationRepository notificationRepository;
    private final SeriesEditorRejectionRepository seriesEditorRejectionRepository;
    private final UserRepository userRepository;
    private final MangakaService mangakaService;
    private final SeriesHistoryService seriesHistoryService;

    public EditorAssignmentJob(MangaSeriesRepository mangaSeriesRepository,
                               NotificationRepository notificationRepository,
                               SeriesEditorRejectionRepository seriesEditorRejectionRepository,
                               UserRepository userRepository,
                               MangakaService mangakaService,
                               SeriesHistoryService seriesHistoryService) {
        this.mangaSeriesRepository = mangaSeriesRepository;
        this.notificationRepository = notificationRepository;
        this.seriesEditorRejectionRepository = seriesEditorRejectionRepository;
        this.userRepository = userRepository;
        this.mangakaService = mangakaService;
        this.seriesHistoryService = seriesHistoryService;
    }

    @Scheduled(fixedRate = 900000)
    @Transactional
    public void revokeAndReassignOverdueSeries() {
        LocalDateTime timeLimit = LocalDateTime.now().minusHours(24);
        List<MangaSeries> overdueSeries = mangaSeriesRepository
                .findOverdueUnlockedSeries("PENDING_EDITOR", timeLimit);

        for (MangaSeries series : overdueSeries) {
            User oldEditor = series.getTantouEditor();
            Long oldEditorId = oldEditor != null ? oldEditor.getUserId() : null;
            if (oldEditor != null) {
                recordTimedOutEditor(series, oldEditor);
            }
            Set<Long> excludedEditorIds = rejectedEditorIds(series.getSeriesId());
            if (oldEditorId != null) {
                excludedEditorIds.add(oldEditorId);
            }
            long rejectionCount = seriesEditorRejectionRepository
                    .countBySeriesSeriesId(series.getSeriesId());

            if (oldEditor != null) {
                dismissAssignmentNotifications(series.getSeriesId(), oldEditor.getUserId());
            }

            if (rejectionCount >= MAX_AUTOMATIC_EDITOR_REJECTIONS) {
                moveToBoardAssignmentRequired(series, oldEditor);
                continue;
            }

            Optional<User> newEditorCandidate = mangakaService
                    .findEditorWithLeastWorkloadExcluding(excludedEditorIds, series.getGenre());
            if (newEditorCandidate.isEmpty()) {
                moveToBoardAssignmentRequired(series, oldEditor);
                continue;
            }

            User newEditor = newEditorCandidate.get();
            series.setTantouEditor(newEditor);
            series.setEditorAssignedAt(LocalDateTime.now());
            mangaSeriesRepository.save(series);
            seriesHistoryService.record(
                    series,
                    oldEditor,
                    "AUTO_EDITOR_REASSIGNED",
                    "PENDING_EDITOR",
                    series.getStatus(),
                    "The previous editor did not accept within 24 hours; reassigned to "
                            + newEditor.getUsername(),
                    newEditor.getUserId());

            createNotification(series.getAuthor(), "SYSTEM", series.getSeriesId(),
                    "Series '" + series.getTitle()
                            + "' was automatically reassigned to another suitable editor because the previous editor did not respond in time.");

            createNotification(newEditor, ASSIGNMENT_TYPE, series.getSeriesId(),
                    "You were automatically assigned series '" + series.getTitle()
                            + "'. Please accept it within 24 hours.");

            if (oldEditor != null) {
                createNotification(oldEditor, "SYSTEM", series.getSeriesId(),
                        "Series '" + series.getTitle()
                                + "' was withdrawn because it was not accepted within 24 hours.");
            }
        }
    }

    private Set<Long> rejectedEditorIds(Long seriesId) {
        Set<Long> rejectedEditorIds = new HashSet<>();
        for (SeriesEditorRejection rejection : seriesEditorRejectionRepository.findBySeriesSeriesId(seriesId)) {
            User editor = rejection.getEditor();
            if (editor != null && editor.getUserId() != null) {
                rejectedEditorIds.add(editor.getUserId());
            }
        }
        return rejectedEditorIds;
    }

    private void recordTimedOutEditor(MangaSeries series, User editor) {
        SeriesEditorRejection rejection = seriesEditorRejectionRepository
                .findBySeriesSeriesIdAndEditorUserId(series.getSeriesId(), editor.getUserId())
                .orElseGet(SeriesEditorRejection::new);
        rejection.setSeries(series);
        rejection.setEditor(editor);
        rejection.setReason("Assignment was not accepted within 24 hours");
        rejection.setRejectedAt(LocalDateTime.now());
        seriesEditorRejectionRepository.save(rejection);
    }

    private void moveToBoardAssignmentRequired(MangaSeries series, User oldEditor) {
        series.setStatus("EDITOR_ASSIGNMENT_REQUIRED");
        series.setTantouEditor(null);
        series.setEditorAssignmentLocked(false);
        series.setEditorAssignedAt(null);
        mangaSeriesRepository.save(series);
        seriesHistoryService.record(
                series,
                oldEditor,
                "AUTO_EDITOR_ASSIGNMENT_REQUIRED",
                "PENDING_EDITOR",
                series.getStatus(),
                "No eligible editor remains after excluding timed-out and previously rejected editors",
                series.getSeriesId());

        createNotification(series.getAuthor(), "EDITOR_ASSIGNMENT_REQUIRED", series.getSeriesId(),
                "Series '" + series.getTitle()
                        + "' is waiting for Editorial Board to assign an editor.");
        userRepository.findByRoleRoleNameAndStatusOrderByUsernameAsc("EDITORIAL_BOARD", "ACTIVE")
                .forEach(board -> createNotification(
                        board,
                        "EDITOR_ASSIGNMENT_REQUIRED",
                        series.getSeriesId(),
                        "Series '" + series.getTitle()
                                + "' needs a forced editor assignment. Review its rejection history first."));
        createNotification(oldEditor, "SYSTEM", series.getSeriesId(),
                "Series '" + series.getTitle()
                        + "' was withdrawn after the 24-hour acceptance period expired.");
    }

    private void dismissAssignmentNotifications(Long seriesId, Long userId) {
        List<Notification> stale = notificationRepository
                .findByReferenceIdAndUserUserIdAndTypeInAndIsReadFalse(seriesId, userId, ASSIGNMENT_TYPES);
        stale.forEach(n -> n.setIsRead(true));
        notificationRepository.saveAll(stale);
    }

    private void createNotification(User user, String type, Long referenceId, String message) {
        if (user == null) {
            return;
        }
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

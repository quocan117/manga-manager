package com.example.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

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

import com.example.backend.model.MangaSeries;
import com.example.backend.model.Role;
import com.example.backend.model.SeriesFile;
import com.example.backend.model.SeriesReviewHistory;
import com.example.backend.model.User;
import com.example.backend.repository.MangaSeriesRepository;
import com.example.backend.repository.SeriesBoardAssignmentRepository;
import com.example.backend.repository.SeriesEditorRejectionRepository;
import com.example.backend.repository.SeriesFileRepository;
import com.example.backend.repository.SeriesReviewHistoryRepository;
import com.example.backend.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class DossierArchiveServiceTests {
    private static final String EMAIL = "author@manga.test";

    @Mock
    private MangaSeriesRepository mangaSeriesRepository;
    @Mock
    private SeriesFileRepository seriesFileRepository;
    @Mock
    private SeriesReviewHistoryRepository seriesReviewHistoryRepository;
    @Mock
    private SeriesEditorRejectionRepository seriesEditorRejectionRepository;
    @Mock
    private SeriesBoardAssignmentRepository seriesBoardAssignmentRepository;
    @Mock
    private UserRepository userRepository;

    private DossierArchiveService service;

    @BeforeEach
    void setUp() {
        service = new DossierArchiveService(
                mangaSeriesRepository,
                seriesFileRepository,
                seriesReviewHistoryRepository,
                seriesEditorRejectionRepository,
                seriesBoardAssignmentRepository,
                userRepository);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(EMAIL, null, List.of()));
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void groupsActiveAndInactiveDossierFilesByRound() {
        User author = user(1L, EMAIL, "MANGAKA");
        User editor = user(2L, "editor@manga.test", "TANTOU_EDITOR");
        MangaSeries series = series(author);
        LocalDateTime firstUpload = LocalDateTime.of(2026, 7, 1, 10, 0);
        LocalDateTime secondUpload = LocalDateTime.of(2026, 7, 3, 10, 0);
        SeriesFile first = file(11L, series, 1, firstUpload, false);
        SeriesFile second = file(12L, series, 2, secondUpload, true);
        SeriesReviewHistory firstReview = history(
                series,
                editor,
                "EDITOR_REQUESTED_REVISION",
                "Revise the synopsis",
                firstUpload.plusHours(2));

        when(mangaSeriesRepository.findById(5L)).thenReturn(Optional.of(series));
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(author));
        when(seriesFileRepository.findBySeriesSeriesIdOrderByRoundNumberAscUploadedAtAsc(5L))
                .thenReturn(List.of(first, second));
        when(seriesReviewHistoryRepository.findBySeriesSeriesIdOrderByCreatedAtAsc(5L))
                .thenReturn(List.of(firstReview));
        when(seriesEditorRejectionRepository.findBySeriesSeriesId(5L)).thenReturn(List.of());

        var response = service.getArchiveHistory(5L);

        assertEquals(2, response.size());
        assertEquals(1, response.get(0).roundNumber());
        assertEquals("EDITOR_REQUESTED_REVISION", response.get(0).decision());
        assertEquals(editor.getUsername(), response.get(0).reviewedBy());
        assertFalse(response.get(0).submittedFiles().get(0).active());
        assertEquals(2, response.get(1).roundNumber());
    }

    @Test
    void rejectsBoardMemberOutsideAssignedPanel() {
        User author = user(1L, "author@manga.test", "MANGAKA");
        User board = user(9L, EMAIL, "EDITORIAL_BOARD");
        MangaSeries series = series(author);
        when(mangaSeriesRepository.findById(5L)).thenReturn(Optional.of(series));
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(board));
        when(seriesBoardAssignmentRepository
                .existsBySeriesSeriesIdAndBoardMemberUserId(5L, 9L))
                .thenReturn(false);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.getArchiveHistory(5L));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    }

    private MangaSeries series(User author) {
        MangaSeries series = new MangaSeries();
        series.setSeriesId(5L);
        series.setTitle("Archive Series");
        series.setAuthor(author);
        return series;
    }

    private SeriesFile file(
            Long id,
            MangaSeries series,
            int round,
            LocalDateTime uploadedAt,
            boolean active) {
        SeriesFile file = new SeriesFile();
        file.setFileId(id);
        file.setSeries(series);
        file.setOriginalFileName("round-" + round + ".pdf");
        file.setContentType("application/pdf");
        file.setRoundNumber(round);
        file.setPurpose("SERIES_SUBMISSION");
        file.setActive(active);
        file.setUploadedAt(uploadedAt);
        return file;
    }

    private SeriesReviewHistory history(
            MangaSeries series,
            User actor,
            String action,
            String reason,
            LocalDateTime createdAt) {
        SeriesReviewHistory history = new SeriesReviewHistory();
        history.setSeries(series);
        history.setActor(actor);
        history.setAction(action);
        history.setReason(reason);
        history.setCreatedAt(createdAt);
        return history;
    }

    private User user(Long id, String email, String roleName) {
        User user = new User();
        user.setUserId(id);
        user.setEmail(email);
        user.setUsername(email);
        Role role = new Role();
        role.setRoleName(roleName);
        user.setRole(role);
        return user;
    }
}

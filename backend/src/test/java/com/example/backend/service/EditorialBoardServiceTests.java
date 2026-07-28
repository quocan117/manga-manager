package com.example.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.mockito.ArgumentCaptor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.example.backend.dto.EditorialBoardDtos.BoardDecisionRequest;
import com.example.backend.dto.EditorialBoardDtos.CreateUserRequest;
import com.example.backend.dto.EditorialBoardDtos.ImportReaderFeedbackRequest;
import com.example.backend.dto.EditorialBoardDtos.ScheduleRequest;
import com.example.backend.model.BoardDecision;
import com.example.backend.model.Chapter;
import com.example.backend.model.MangaSeries;
import com.example.backend.model.Notification;
import com.example.backend.model.PublishSchedule;
import com.example.backend.model.ReaderFeedbackImport;
import com.example.backend.model.Role;
import com.example.backend.model.SeriesBoardAssignment;
import com.example.backend.model.SeriesEditorRejection;
import com.example.backend.model.SeriesFile;
import com.example.backend.model.SeriesRanking;
import com.example.backend.model.User;
import com.example.backend.repository.BoardDecisionRepository;
import com.example.backend.repository.ChapterLikeLogRepository;
import com.example.backend.repository.ChapterRepository;
import com.example.backend.repository.MangaSeriesRepository;
import com.example.backend.repository.NotificationRepository;
import com.example.backend.repository.PublishScheduleRepository;
import com.example.backend.repository.ReaderFeedbackImportRepository;
import com.example.backend.repository.RegistrationRequestRepository;
import com.example.backend.repository.RoleRepository;
import com.example.backend.repository.SeriesBoardAssignmentRepository;
import com.example.backend.repository.SeriesBoardAssignmentRepository.BoardMemberAssignmentCount;
import com.example.backend.repository.SeriesEditorRejectionRepository;
import com.example.backend.repository.SeriesFileRepository;
import com.example.backend.repository.SeriesRankingRepository;
import com.example.backend.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class EditorialBoardServiceTests {
        private static final String BOARD_EMAIL = "editorial1@manga.test";

        @Mock
        private RegistrationRequestRepository requestRepository;
        @Mock
        private UserRepository userRepository;
        @Mock
        private RoleRepository roleRepository;
        @Mock
        private PasswordEncoder passwordEncoder;
        @Mock
        private MangaSeriesRepository mangaSeriesRepository;
        @Mock
        private ChapterRepository chapterRepository;
        @Mock
        private BoardDecisionRepository boardDecisionRepository;
        @Mock
        private NotificationRepository notificationRepository;
        @Mock
        private PublishScheduleRepository publishScheduleRepository;
        @Mock
        private ChapterLikeLogRepository chapterLikeLogRepository;
        @Mock
        private SeriesRankingRepository seriesRankingRepository;
        @Mock
        private ReaderFeedbackImportRepository readerFeedbackImportRepository;
        @Mock
        private SeriesFileRepository seriesFileRepository;
        @Mock
        private SeriesEditorRejectionRepository seriesEditorRejectionRepository;
        @Mock
        private SeriesBoardAssignmentRepository seriesBoardAssignmentRepository;
        @Mock
        private MangakaService mangakaService;

        @InjectMocks
        private EditorialBoardService service;

        @BeforeEach
        void setUp() {
                SecurityContextHolder.getContext().setAuthentication(
                                new UsernamePasswordAuthenticationToken(BOARD_EMAIL, null));
        }

        @AfterEach
        void tearDown() {
                SecurityContextHolder.clearContext();
        }

        @Test
        void createsManagedUserAccount() {
                User board = user(1L, "Editorial Board One", BOARD_EMAIL, role("EDITORIAL_BOARD"));
                Role mangakaRole = role("MANGAKA");

                when(userRepository.existsByEmail("new@manga.test")).thenReturn(false);
                when(userRepository.existsByUsername("New Mangaka")).thenReturn(false);
                when(roleRepository.findByRoleName("MANGAKA")).thenReturn(Optional.of(mangakaRole));
                when(userRepository.findByEmail(BOARD_EMAIL)).thenReturn(Optional.of(board));
                when(passwordEncoder.encode("Secret123")).thenReturn("hashed-password");
                when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
                        User saved = invocation.getArgument(0);
                        saved.setUserId(10L);
                        return saved;
                });

                var response = service.createUser(new CreateUserRequest(
                                "New Mangaka",
                                "new@manga.test",
                                "Secret123",
                                "mangaka",
                                null,
                                null));

                assertEquals(10L, response.id());
                assertEquals("New Mangaka", response.username());
                assertEquals("new@manga.test", response.email());
                assertEquals("MANGAKA", response.role());
                assertEquals("ACTIVE", response.status());
                assertEquals(1L, response.createdById());
                assertNotNull(response.createdAt());
        }

        @Test
        void editorAssignmentRequiredResponseIncludesRejectedEditorDetails() {
                User board = user(1L, "Editorial Board One", BOARD_EMAIL, role("EDITORIAL_BOARD"));
                User rejectedEditor = user(7L, "Tantou Seven", "tantou7@manga.test", role("TANTOU_EDITOR"));
                MangaSeries series = series(5L, "Needs Editor", "EDITOR_ASSIGNMENT_REQUIRED");
                LocalDateTime rejectedAt = LocalDateTime.now().minusHours(2);
                SeriesEditorRejection rejection = new SeriesEditorRejection();
                rejection.setSeries(series);
                rejection.setEditor(rejectedEditor);
                rejection.setReason("Không phù hợp với thể loại của series");
                rejection.setRejectedAt(rejectedAt);

                when(userRepository.findByEmail(BOARD_EMAIL)).thenReturn(Optional.of(board));
                when(mangaSeriesRepository.findByStatusIgnoreCaseOrderBySubmittedAtDesc("EDITOR_ASSIGNMENT_REQUIRED"))
                                .thenReturn(List.of(series));
                when(seriesBoardAssignmentRepository.findBySeriesSeriesIdOrderByAssignedAtAsc(5L))
                                .thenReturn(List.of());
                when(seriesEditorRejectionRepository.findBySeriesSeriesId(5L))
                                .thenReturn(List.of(rejection));
                when(mangakaService.countTantouEditorActiveWorkload(7L)).thenReturn(3L);

                var response = service.getEditorAssignmentRequiredSeries();

                assertEquals(1, response.size());
                assertEquals(1, response.get(0).editorRejectCount());
                assertEquals(1, response.get(0).rejectedEditors().size());
                var rejectedEditorResponse = response.get(0).rejectedEditors().get(0);
                assertEquals(7L, rejectedEditorResponse.editorId());
                assertEquals("Tantou Seven", rejectedEditorResponse.name());
                assertEquals("tantou7@manga.test", rejectedEditorResponse.email());
                assertEquals(3L, rejectedEditorResponse.currentTaskCount());
                assertEquals("Không phù hợp với thể loại của series", rejectedEditorResponse.reason());
                assertEquals(rejectedAt, rejectedEditorResponse.rejectedAt());
        }

        @Test
        void deleteUserMarksAccountDeleted() {
                User board = user(1L, "Editorial Board One", BOARD_EMAIL, role("EDITORIAL_BOARD"));
                User target = user(2L, "Assistant", "assistant@manga.test", role("ASSISTANT"));
                target.setStatus("ACTIVE");

                when(userRepository.findById(2L)).thenReturn(Optional.of(target));
                when(userRepository.findByEmail(BOARD_EMAIL)).thenReturn(Optional.of(board));
                when(userRepository.save(target)).thenReturn(target);

                var response = service.deleteUser(2L);

                assertEquals("DELETED", response.status());
                verify(userRepository).save(target);
        }

        @Test
        void voteMovesSeriesToPendingScheduleAndAssignsApproverAsCoordinator() {
                User board = user(1L, "Editorial Board One", BOARD_EMAIL, role("EDITORIAL_BOARD"));
                MangaSeries series = series(5L, "Submitted Series", "REVIEWING");
                BoardDecision decision = new BoardDecision();
                decision.setDecisionId(9L);
                decision.setSeries(series);
                decision.setBoardMember(board);
                decision.setDecisionType("APPROVE");
                SeriesBoardAssignment assignment = new SeriesBoardAssignment();
                assignment.setSeries(series);
                assignment.setBoardMember(board);
                assignment.setAssignedAt(LocalDateTime.now());

                when(userRepository.findByEmail(BOARD_EMAIL)).thenReturn(Optional.of(board));
                when(mangaSeriesRepository.findByIdForUpdate(5L)).thenReturn(Optional.of(series));
                when(seriesBoardAssignmentRepository.findBySeriesSeriesIdOrderByAssignedAtAsc(5L))
                                .thenReturn(List.of(), List.of(assignment));
                when(userRepository.findByRoleRoleNameAndStatusOrderByUsernameAsc("EDITORIAL_BOARD", "ACTIVE"))
                                .thenReturn(List.of(board));
                when(seriesBoardAssignmentRepository.save(any(SeriesBoardAssignment.class))).thenReturn(assignment);
                when(seriesBoardAssignmentRepository.findBySeriesSeriesIdAndBoardMemberUserId(5L, 1L))
                                .thenReturn(Optional.of(assignment));
                when(seriesBoardAssignmentRepository.countBySeriesSeriesId(5L)).thenReturn(1L);
                when(boardDecisionRepository.findBySeriesSeriesIdAndBoardMemberUserId(5L, 1L))
                                .thenReturn(Optional.empty());
                when(boardDecisionRepository.save(any(BoardDecision.class))).thenAnswer(invocation -> {
                        BoardDecision saved = invocation.getArgument(0);
                        saved.setDecisionId(9L);
                        return saved;
                });
                when(userRepository.countByRoleRoleNameAndStatus("EDITORIAL_BOARD", "ACTIVE")).thenReturn(1L);
                when(userRepository.countByRoleRoleNameAndStatus("TANTOU_EDITOR", "ACTIVE")).thenReturn(1L);
                when(boardDecisionRepository.countPanelDecisionsBySeriesIdAndDecisionType(5L, "APPROVE"))
                                .thenReturn(1L);
                when(boardDecisionRepository.countPanelDecisionsBySeriesIdAndDecisionType(5L, "REJECT"))
                                .thenReturn(0L);
                when(mangaSeriesRepository.save(series)).thenReturn(series);
                when(boardDecisionRepository.findPanelDecisionsBySeriesIdOrderByDecisionDateDesc(5L))
                                .thenReturn(List.of(decision));

                var response = service.voteSeries(5L, new BoardDecisionRequest("APPROVE", "Ready"));

                assertEquals("PENDING_SCHEDULE", series.getStatus());
                assertEquals("PENDING_SCHEDULE", response.status());
                assertEquals(board, series.getPublicationCoordinator());
                assertNotNull(series.getCoordinatorAssignedAt());
                assertEquals(1L, response.approveVotes());
                assertEquals(1L, response.requiredVotes());
                assertEquals("APPROVE", response.currentUserDecision());
                assertTrue(response.currentUserAssigned());
                verify(mangaSeriesRepository).save(series);
        }

        @Test
        void assignsThreeBoardMembersWithTheLowestPreviousWorkload() {
                MangaSeries series = series(5L, "Submitted Series", "REVIEWING");
                User boardOne = user(1L, "Board One", "board1@manga.test", role("EDITORIAL_BOARD"));
                User boardTwo = user(2L, "Board Two", "board2@manga.test", role("EDITORIAL_BOARD"));
                User boardThree = user(3L, "Board Three", "board3@manga.test", role("EDITORIAL_BOARD"));
                User boardFour = user(4L, "Board Four", "board4@manga.test", role("EDITORIAL_BOARD"));
                User boardFive = user(5L, "Board Five", "board5@manga.test", role("EDITORIAL_BOARD"));

                when(seriesBoardAssignmentRepository.findBySeriesSeriesIdOrderByAssignedAtAsc(5L))
                                .thenReturn(List.of());
                when(userRepository.findByRoleRoleNameAndStatusOrderByUsernameAsc("EDITORIAL_BOARD", "ACTIVE"))
                                .thenReturn(List.of(boardOne, boardTwo, boardThree, boardFour, boardFive));
                when(seriesBoardAssignmentRepository.countAssignmentsByBoardMemberIds(any()))
                                .thenReturn(List.of(
                                                assignmentCount(1L, 5L),
                                                assignmentCount(2L, 0L),
                                                assignmentCount(3L, 1L),
                                                assignmentCount(4L, 0L),
                                                assignmentCount(5L, 2L)));
                when(seriesBoardAssignmentRepository.save(any(SeriesBoardAssignment.class)))
                                .thenAnswer(invocation -> invocation.getArgument(0));

                List<SeriesBoardAssignment> panel = service.assignBoardPanel(series);

                assertEquals(3, panel.size());
                Set<Long> selectedIds = panel.stream()
                                .map(assignment -> assignment.getBoardMember().getUserId())
                                .collect(Collectors.toSet());
                assertEquals(Set.of(2L, 3L, 4L), selectedIds);
                verify(seriesBoardAssignmentRepository, times(3)).save(any(SeriesBoardAssignment.class));
        }

        @Test
        void boardMemberOutsideAssignedPanelCannotVote() {
                User currentBoard = user(1L, "Board One", BOARD_EMAIL, role("EDITORIAL_BOARD"));
                User assignedBoard = user(2L, "Board Two", "board2@manga.test", role("EDITORIAL_BOARD"));
                MangaSeries series = series(5L, "Submitted Series", "REVIEWING");
                SeriesBoardAssignment assignment = new SeriesBoardAssignment();
                assignment.setSeries(series);
                assignment.setBoardMember(assignedBoard);
                assignment.setAssignedAt(LocalDateTime.now());

                when(userRepository.findByEmail(BOARD_EMAIL)).thenReturn(Optional.of(currentBoard));
                when(mangaSeriesRepository.findByIdForUpdate(5L)).thenReturn(Optional.of(series));
                when(seriesBoardAssignmentRepository.findBySeriesSeriesIdOrderByAssignedAtAsc(5L))
                                .thenReturn(List.of(assignment));
                when(seriesBoardAssignmentRepository.findBySeriesSeriesIdAndBoardMemberUserId(5L, 1L))
                                .thenReturn(Optional.empty());

                ResponseStatusException exception = assertThrows(
                                ResponseStatusException.class,
                                () -> service.voteSeries(5L, new BoardDecisionRequest("APPROVE", "Ready")));

                assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        }

        @Test
        void rejectVoteRequiresReason() {
                User board = user(1L, "Board One", BOARD_EMAIL, role("EDITORIAL_BOARD"));
                MangaSeries series = series(5L, "Submitted Series", "REVIEWING");
                SeriesBoardAssignment assignment = new SeriesBoardAssignment();
                assignment.setSeries(series);
                assignment.setBoardMember(board);

                when(userRepository.findByEmail(BOARD_EMAIL)).thenReturn(Optional.of(board));
                when(mangaSeriesRepository.findByIdForUpdate(5L)).thenReturn(Optional.of(series));
                when(seriesBoardAssignmentRepository.findBySeriesSeriesIdOrderByAssignedAtAsc(5L))
                                .thenReturn(List.of(assignment));
                when(seriesBoardAssignmentRepository.findBySeriesSeriesIdAndBoardMemberUserId(5L, 1L))
                                .thenReturn(Optional.of(assignment));

                ResponseStatusException exception = assertThrows(
                                ResponseStatusException.class,
                                () -> service.voteSeries(5L, new BoardDecisionRequest("REJECT", "  ")));

                assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
                assertEquals("Bắt buộc nhập lý do khi từ chối hồ sơ", exception.getReason());
                verify(boardDecisionRepository, never()).save(any(BoardDecision.class));
        }

        @Test
        void rejectionNotificationContainsAnonymousReasons() {
                User board = user(1L, "Board One", BOARD_EMAIL, role("EDITORIAL_BOARD"));
                User editor = user(3L, "Editor Name", "editor@manga.test", role("TANTOU_EDITOR"));
                MangaSeries series = series(5L, "Submitted Series", "REVIEWING");
                series.setTantouEditor(editor);
                SeriesBoardAssignment assignment = new SeriesBoardAssignment();
                assignment.setSeries(series);
                assignment.setBoardMember(board);
                BoardDecision rejectedDecision = new BoardDecision();
                rejectedDecision.setSeries(series);
                rejectedDecision.setBoardMember(board);
                rejectedDecision.setDecisionType("REJECT");
                rejectedDecision.setReason("Kịch bản chưa nhất quán");

                when(userRepository.findByEmail(BOARD_EMAIL)).thenReturn(Optional.of(board));
                when(mangaSeriesRepository.findByIdForUpdate(5L)).thenReturn(Optional.of(series));
                when(seriesBoardAssignmentRepository.findBySeriesSeriesIdOrderByAssignedAtAsc(5L))
                                .thenReturn(List.of(assignment));
                when(seriesBoardAssignmentRepository.findBySeriesSeriesIdAndBoardMemberUserId(5L, 1L))
                                .thenReturn(Optional.of(assignment));
                when(seriesBoardAssignmentRepository.countBySeriesSeriesId(5L)).thenReturn(1L);
                when(boardDecisionRepository.findBySeriesSeriesIdAndBoardMemberUserId(5L, 1L))
                                .thenReturn(Optional.empty());
                when(boardDecisionRepository.countPanelDecisionsBySeriesIdAndDecisionType(5L, "APPROVE"))
                                .thenReturn(0L);
                when(boardDecisionRepository.countPanelDecisionsBySeriesIdAndDecisionType(5L, "REJECT"))
                                .thenReturn(1L);
                when(boardDecisionRepository.findPanelDecisionsBySeriesIdOrderByDecisionDateDesc(5L))
                                .thenReturn(List.of(rejectedDecision));
                when(userRepository.countByRoleRoleNameAndStatus("EDITORIAL_BOARD", "ACTIVE")).thenReturn(1L);
                when(userRepository.countByRoleRoleNameAndStatus("TANTOU_EDITOR", "ACTIVE")).thenReturn(1L);

                var response = service.voteSeries(
                                5L,
                                new BoardDecisionRequest("REJECT", "Kịch bản chưa nhất quán"));

                assertEquals("REVISION_REQUESTED", response.status());
                ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
                verify(notificationRepository, times(2)).save(notificationCaptor.capture());
                notificationCaptor.getAllValues().forEach(notification -> {
                        assertEquals("SERIES_REJECTED", notification.getType());
                        assertTrue(notification.getMessage().contains("- Kịch bản chưa nhất quán"));
                        assertTrue(!notification.getMessage().contains(board.getUsername()));
                });
        }

        @Test
        void nonCoordinatorCannotCreatePublishSchedule() {
                User currentBoard = user(1L, "Board One", BOARD_EMAIL, role("EDITORIAL_BOARD"));
                User coordinator = user(2L, "Board Two", "board2@manga.test", role("EDITORIAL_BOARD"));
                MangaSeries series = series(5L, "Approved Series", "COMING_SOON");
                series.setPublicationCoordinator(coordinator);
                LocalDateTime publishDate = LocalDateTime.now().plusDays(7);

                when(userRepository.findByEmail(BOARD_EMAIL)).thenReturn(Optional.of(currentBoard));
                when(mangaSeriesRepository.findById(5L)).thenReturn(Optional.of(series));

                ResponseStatusException exception = assertThrows(
                                ResponseStatusException.class,
                                () -> service.createSchedule(
                                                new ScheduleRequest(5L, publishDate, "WEEKLY", null)));

                assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
                verify(publishScheduleRepository, never()).save(any(PublishSchedule.class));
        }

        @Test
        void createScheduleActivatesComingSoonAndNotifiesAuthorAndEditor() {
                User coordinator = user(1L, "Board One", BOARD_EMAIL, role("EDITORIAL_BOARD"));
                User editor = user(3L, "Editor Name", "editor@manga.test", role("TANTOU_EDITOR"));
                MangaSeries series = series(5L, "Approved Series", "PENDING_SCHEDULE");
                series.setPublicationCoordinator(coordinator);
                series.setTantouEditor(editor);
                LocalDateTime publishDate = LocalDateTime.of(2026, 8, 1, 9, 0);

                when(userRepository.findByEmail(BOARD_EMAIL)).thenReturn(Optional.of(coordinator));
                when(mangaSeriesRepository.findById(5L)).thenReturn(Optional.of(series));
                when(publishScheduleRepository.save(any(PublishSchedule.class))).thenAnswer(invocation -> {
                        PublishSchedule schedule = invocation.getArgument(0);
                        schedule.setScheduleId(10L);
                        return schedule;
                });
                when(userRepository.findByRoleRoleNameAndStatusOrderByUsernameAsc(
                                "EDITORIAL_BOARD", "ACTIVE")).thenReturn(List.of());

                var response = service.createSchedule(
                                new ScheduleRequest(5L, publishDate, "WEEKLY", null));

                assertEquals("COMING_SOON", series.getStatus());
                assertEquals(10L, response.id());
                verify(mangaSeriesRepository).save(series);
                ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
                verify(notificationRepository, times(2)).save(notificationCaptor.capture());
                assertEquals(
                                List.of(series.getAuthor(), editor),
                                notificationCaptor.getAllValues().stream()
                                                .map(Notification::getUser)
                                                .toList());
                notificationCaptor.getAllValues().forEach(notification -> {
                        assertEquals("SERIES_APPROVED_AND_SCHEDULED", notification.getType());
                        assertTrue(notification.getMessage().contains("WEEKLY"));
                        assertTrue(notification.getMessage().contains(publishDate.toString()));
                });
        }

        @Test
        void approvedSeriesManagementIncludesSubmissionFiles() {
                User board = user(1L, "Board One", BOARD_EMAIL, role("EDITORIAL_BOARD"));
                MangaSeries series = series(5L, "Approved Series", "PENDING_SCHEDULE");
                series.setPublicationCoordinator(board);
                SeriesFile file = new SeriesFile();
                file.setFileId(40L);
                file.setSeries(series);
                file.setOriginalFileName("series-dossier.zip");
                file.setContentType("application/zip");
                file.setPurpose("SERIES_SUBMISSION");
                file.setActive(true);

                when(userRepository.findByEmail(BOARD_EMAIL)).thenReturn(Optional.of(board));
                when(mangaSeriesRepository.findByStatusesOrderByCreatedAtDesc(any()))
                                .thenReturn(List.of(series));
                when(chapterRepository.findBySeriesSeriesIdOrderByChapterNumberAsc(5L))
                                .thenReturn(List.of());
                when(publishScheduleRepository.findFirstBySeriesSeriesIdOrderByPublishDateAsc(5L))
                                .thenReturn(Optional.empty());
                when(seriesBoardAssignmentRepository.findBySeriesSeriesIdOrderByAssignedAtAsc(5L))
                                .thenReturn(List.of());
                when(seriesFileRepository
                                .findBySeriesSeriesIdAndPurposeAndActiveTrueOrderByUploadedAtDesc(
                                                5L,
                                                "SERIES_SUBMISSION"))
                                .thenReturn(List.of(file));

                var response = service.getApprovedSeries();

                assertEquals(1, response.size());
                assertEquals("PENDING_SCHEDULE", response.get(0).status());
                assertEquals(1, response.get(0).uploadedFiles().size());
                assertEquals("series-dossier.zip",
                                response.get(0).uploadedFiles().get(0).originalFileName());
        }

        @Test
        void existingBoardPanelIsKeptUnchanged() {
                MangaSeries series = series(5L, "Submitted Series", "REVIEWING");
                User assignedBoard = user(2L, "Board Two", "board2@manga.test", role("EDITORIAL_BOARD"));
                SeriesBoardAssignment assignment = new SeriesBoardAssignment();
                assignment.setSeries(series);
                assignment.setBoardMember(assignedBoard);
                assignment.setAssignedAt(LocalDateTime.now());
                when(seriesBoardAssignmentRepository.findBySeriesSeriesIdOrderByAssignedAtAsc(5L))
                                .thenReturn(List.of(assignment));

                List<SeriesBoardAssignment> panel = service.assignBoardPanel(series);

                assertEquals(List.of(assignment), panel);
                verify(userRepository, never())
                                .findByRoleRoleNameAndStatusOrderByUsernameAsc("EDITORIAL_BOARD", "ACTIVE");
                verify(seriesBoardAssignmentRepository, never()).save(any(SeriesBoardAssignment.class));
        }

        @Test
        void importReaderFeedbackCalculatesRankingForAssignedSeries() {
                User board = user(1L, "Editorial Board One", BOARD_EMAIL, role("EDITORIAL_BOARD"));
                MangaSeries assignedSeries = series(5L, "Five Votes", "Published");
                LocalDateTime from = LocalDateTime.of(2026, 7, 1, 0, 0);
                LocalDateTime to = LocalDateTime.of(2026, 7, 8, 0, 0);

                when(userRepository.findByEmail(BOARD_EMAIL)).thenReturn(Optional.of(board));
                when(seriesBoardAssignmentRepository
                                .existsBySeriesSeriesIdAndBoardMemberUserId(5L, board.getUserId()))
                                .thenReturn(true);
                when(mangaSeriesRepository.findById(5L)).thenReturn(Optional.of(assignedSeries));
                when(chapterLikeLogRepository
                                .countByChapterSeriesSeriesIdAndLikedAtBetween(5L, from, to))
                                .thenReturn(5L);
                when(readerFeedbackImportRepository
                                .findBySeriesSeriesIdAndPeriodStartAndPeriodEnd(5L, from, to))
                                .thenReturn(Optional.empty());
                when(seriesRankingRepository
                                .findBySeriesSeriesIdAndPeriodStartAndPeriodEnd(5L, from, to))
                                .thenReturn(Optional.empty());
                when(seriesRankingRepository.findForPositionRecalculation(from, to))
                                .thenReturn(List.of());
                when(readerFeedbackImportRepository.save(any(ReaderFeedbackImport.class))).thenAnswer(invocation -> {
                        ReaderFeedbackImport saved = invocation.getArgument(0);
                        saved.setImportId(saved.getSeries().getSeriesId() + 100L);
                        return saved;
                });

                var response = service.importReaderFeedback(new ImportReaderFeedbackRequest(
                                5L, from, to));

                assertEquals(5L, response.seriesId());
                assertEquals(5, response.voteCount());
                assertEquals(from, response.periodStart());
                assertEquals(to, response.periodEnd());

                ArgumentCaptor<SeriesRanking> rankingCaptor = ArgumentCaptor.forClass(SeriesRanking.class);
                verify(seriesRankingRepository).save(rankingCaptor.capture());
                SeriesRanking ranking = rankingCaptor.getValue();
                assertEquals(5L, ranking.getSeries().getSeriesId());
                assertEquals(1, ranking.getRankingPosition());
                assertEquals(5, ranking.getVoteCount());
                assertEquals(from, ranking.getPeriodStart());
                assertEquals(to, ranking.getPeriodEnd());
        }

        @Test
        void importReaderFeedbackRejectsSeriesOutsideCurrentBoardPanel() {
                User board = user(1L, "Editorial Board One", BOARD_EMAIL, role("EDITORIAL_BOARD"));
                LocalDateTime from = LocalDateTime.of(2026, 7, 1, 0, 0);
                LocalDateTime to = LocalDateTime.of(2026, 7, 8, 0, 0);

                when(userRepository.findByEmail(BOARD_EMAIL)).thenReturn(Optional.of(board));
                when(seriesBoardAssignmentRepository
                                .existsBySeriesSeriesIdAndBoardMemberUserId(5L, board.getUserId()))
                                .thenReturn(false);

                ResponseStatusException exception = assertThrows(
                                ResponseStatusException.class,
                                () -> service.importReaderFeedback(new ImportReaderFeedbackRequest(5L, from, to)));

                assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
                assertEquals("Bạn không được phân công xử lý Series này", exception.getReason());
                verify(chapterLikeLogRepository, never())
                                .countByChapterSeriesSeriesIdAndLikedAtBetween(5L, from, to);
                verify(readerFeedbackImportRepository, never()).save(any(ReaderFeedbackImport.class));
        }

        @Test
        void getMyAssignedSeriesReturnsDropdownData() {
                User board = user(1L, "Editorial Board One", BOARD_EMAIL, role("EDITORIAL_BOARD"));
                MangaSeries assignedSeries = series(5L, "Assigned Series", "REVIEWING");
                assignedSeries.setCoverImage("/covers/assigned.jpg");
                SeriesBoardAssignment assignment = new SeriesBoardAssignment();
                assignment.setSeries(assignedSeries);
                assignment.setBoardMember(board);

                when(userRepository.findByEmail(BOARD_EMAIL)).thenReturn(Optional.of(board));
                when(seriesBoardAssignmentRepository
                                .findByBoardMemberUserIdOrderByAssignedAtDesc(board.getUserId()))
                                .thenReturn(List.of(assignment));

                var response = service.getMyAssignedSeries();

                assertEquals(1, response.size());
                assertEquals(5L, response.get(0).id());
                assertEquals("Assigned Series", response.get(0).title());
                assertEquals("/covers/assigned.jpg", response.get(0).coverUrl());
        }

        @Test
        void getSeriesFeedbackImportsReturnsSeriesHistory() {
                LocalDateTime firstStart = LocalDateTime.of(2026, 7, 1, 0, 0);
                LocalDateTime firstEnd = LocalDateTime.of(2026, 7, 8, 0, 0);
                ReaderFeedbackImport feedbackImport = new ReaderFeedbackImport();
                feedbackImport.setImportId(11L);
                feedbackImport.setPeriodStart(firstStart);
                feedbackImport.setPeriodEnd(firstEnd);
                feedbackImport.setVoteCount(25);

                when(mangaSeriesRepository.existsById(5L)).thenReturn(true);
                when(readerFeedbackImportRepository.findBySeriesSeriesIdOrderByImportedAtDesc(5L))
                                .thenReturn(List.of(feedbackImport));

                var response = service.getSeriesFeedbackImports(5L);

                assertEquals(1, response.size());
                assertEquals(11L, response.get(0).importId());
                assertEquals(firstStart, response.get(0).periodStart());
                assertEquals(firstEnd, response.get(0).periodEnd());
                assertEquals(25, response.get(0).voteCount());
        }

        private User user(Long id, String username, String email, Role role) {
                User user = new User();
                user.setUserId(id);
                user.setUsername(username);
                user.setEmail(email);
                user.setRole(role);
                return user;
        }

        private Role role(String name) {
                Role role = new Role();
                role.setRoleName(name);
                return role;
        }

        private MangaSeries series(Long id, String title, String status) {
                MangaSeries series = new MangaSeries();
                series.setSeriesId(id);
                series.setTitle(title);
                series.setStatus(status);
                series.setGenre("Action, Drama");
                series.setAuthor(user(20L, "Author", "author@manga.test", role("MANGAKA")));
                return series;
        }

        private Chapter chapter(Long id, MangaSeries series, String status) {
                Chapter chapter = new Chapter();
                chapter.setChapterId(id);
                chapter.setSeries(series);
                chapter.setTitle("Chapter " + id);
                chapter.setChapterNumber(id.intValue());
                chapter.setStatus(status);
                return chapter;
        }

        private ChapterLikeLogRepository.SeriesVoteCount voteCount(Long seriesId, String title, Long count) {
                return new ChapterLikeLogRepository.SeriesVoteCount() {
                        @Override
                        public Long getSeriesId() {
                                return seriesId;
                        }

                        @Override
                        public String getSeriesTitle() {
                                return title;
                        }

                        @Override
                        public Long getVoteCount() {
                                return count;
                        }
                };
        }

        private BoardMemberAssignmentCount assignmentCount(Long boardMemberId, long count) {
                return new BoardMemberAssignmentCount() {
                        @Override
                        public Long getBoardMemberId() {
                                return boardMemberId;
                        }

                        @Override
                        public long getAssignmentCount() {
                                return count;
                        }
                };
        }
}

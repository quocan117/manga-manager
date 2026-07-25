package com.example.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
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
import com.example.backend.dto.EditorialBoardDtos.ChapterBoardReviewRequest;
import com.example.backend.dto.EditorialBoardDtos.CreateUserRequest;
import com.example.backend.dto.EditorialBoardDtos.ImportReaderFeedbackRequest;
import com.example.backend.dto.EditorialBoardDtos.ScheduleRequest;
import com.example.backend.model.BoardDecision;
import com.example.backend.model.Chapter;
import com.example.backend.model.ChapterBoardReview;
import com.example.backend.model.MangaSeries;
import com.example.backend.model.PublishSchedule;
import com.example.backend.model.ReaderFeedbackImport;
import com.example.backend.model.Role;
import com.example.backend.model.SeriesBoardAssignment;
import com.example.backend.model.SeriesEditorRejection;
import com.example.backend.model.SeriesRanking;
import com.example.backend.model.User;
import com.example.backend.repository.BoardDecisionRepository;
import com.example.backend.repository.ChapterBoardReviewRepository;
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
    private ChapterBoardReviewRepository chapterBoardReviewRepository;
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
    void voteMovesSeriesToComingSoonAndAssignsApproverAsCoordinator() {
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

        assertEquals("COMING_SOON", series.getStatus());
        assertEquals("COMING_SOON", response.status());
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
    void fullChapterPanelConfirmationMarksChapterApproved() {
        User board = user(1L, "Board One", BOARD_EMAIL, role("EDITORIAL_BOARD"));
        MangaSeries series = series(5L, "Approved Series", "COMING_SOON");
        series.setPublicationCoordinator(board);
        Chapter chapter = chapter(8L, series, "SUBMITTED_TO_BOARD");
        ChapterBoardReview review = chapterReview(chapter, board);

        when(userRepository.findByEmail(BOARD_EMAIL)).thenReturn(Optional.of(board));
        when(chapterRepository.findByIdForUpdate(8L)).thenReturn(Optional.of(chapter));
        when(chapterBoardReviewRepository.findByChapterChapterIdAndBoardMemberUserId(8L, 1L))
                .thenReturn(Optional.of(review));
        when(chapterBoardReviewRepository.countByChapterChapterId(8L)).thenReturn(3L);
        when(chapterBoardReviewRepository.countByChapterChapterIdAndConfirmedTrue(8L)).thenReturn(3L);
        when(chapterBoardReviewRepository.existsByChapterChapterIdAndConfirmedFalse(8L)).thenReturn(false);
        when(chapterBoardReviewRepository.findByChapterChapterIdOrderByBoardMemberUsernameAsc(8L))
                .thenReturn(List.of(review));

        var response = service.reviewChapter(
                8L, new ChapterBoardReviewRequest(true, "Confirmed"));

        assertEquals("APPROVED", chapter.getStatus());
        assertEquals("APPROVED", response.status());
        verify(chapterRepository).save(chapter);
    }

    @Test
    void oneChapterRejectionImmediatelyRequestsRevision() {
        User board = user(1L, "Board One", BOARD_EMAIL, role("EDITORIAL_BOARD"));
        MangaSeries series = series(5L, "Approved Series", "COMING_SOON");
        Chapter chapter = chapter(8L, series, "SUBMITTED_TO_BOARD");
        ChapterBoardReview review = chapterReview(chapter, board);

        when(userRepository.findByEmail(BOARD_EMAIL)).thenReturn(Optional.of(board));
        when(chapterRepository.findByIdForUpdate(8L)).thenReturn(Optional.of(chapter));
        when(chapterBoardReviewRepository.findByChapterChapterIdAndBoardMemberUserId(8L, 1L))
                .thenReturn(Optional.of(review));
        when(chapterBoardReviewRepository.findByChapterChapterIdOrderByBoardMemberUsernameAsc(8L))
                .thenReturn(List.of(review));

        var response = service.reviewChapter(
                8L, new ChapterBoardReviewRequest(false, "Fix the dialogue"));

        assertEquals("REVISION_REQUESTED", chapter.getStatus());
        assertEquals("REVISION_REQUESTED", response.status());
        verify(chapterRepository).save(chapter);
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
    void importReaderFeedbackCalculatesRankingsFromReaderLikes() {
        User board = user(1L, "Editorial Board One", BOARD_EMAIL, role("EDITORIAL_BOARD"));
        MangaSeries firstSeries = series(5L, "Five Votes", "Published");
        MangaSeries secondSeries = series(6L, "Ten Votes", "Published");
        LocalDateTime from = LocalDateTime.of(2026, 7, 1, 0, 0);
        LocalDateTime to = LocalDateTime.of(2026, 7, 8, 0, 0);

        when(userRepository.findByEmail(BOARD_EMAIL)).thenReturn(Optional.of(board));
        when(chapterLikeLogRepository.countVotesBySeriesBetween(from, to))
                .thenReturn(List.of(voteCount(5L, "Five Votes", 5L), voteCount(6L, "Ten Votes", 10L)));
        when(mangaSeriesRepository.findById(5L)).thenReturn(Optional.of(firstSeries));
        when(mangaSeriesRepository.findById(6L)).thenReturn(Optional.of(secondSeries));
        when(readerFeedbackImportRepository.findBySeriesSeriesIdAndPeriod(anyLong(), anyString()))
                .thenReturn(Optional.empty());
        when(seriesRankingRepository.findBySeriesSeriesIdAndPeriod(anyLong(), anyString()))
                .thenReturn(Optional.empty());
        when(readerFeedbackImportRepository.save(any(ReaderFeedbackImport.class))).thenAnswer(invocation -> {
            ReaderFeedbackImport saved = invocation.getArgument(0);
            saved.setImportId(saved.getSeries().getSeriesId() + 100L);
            return saved;
        });

        var response = service.importReaderFeedback(new ImportReaderFeedbackRequest(
                "2026-W27", from, to));

        assertEquals(2, response.size());
        assertEquals(6L, response.get(0).seriesId());
        assertEquals(10, response.get(0).voteCount());
        assertEquals(5L, response.get(1).seriesId());
        assertEquals(5, response.get(1).voteCount());

        ArgumentCaptor<SeriesRanking> rankingCaptor = ArgumentCaptor.forClass(SeriesRanking.class);
        verify(seriesRankingRepository, org.mockito.Mockito.times(2)).save(rankingCaptor.capture());
        List<SeriesRanking> rankings = rankingCaptor.getAllValues();
        assertEquals(6L, rankings.get(0).getSeries().getSeriesId());
        assertEquals(1, rankings.get(0).getRankingPosition());
        assertEquals(10, rankings.get(0).getVoteCount());
        assertEquals(5L, rankings.get(1).getSeries().getSeriesId());
        assertEquals(2, rankings.get(1).getRankingPosition());
        assertEquals(5, rankings.get(1).getVoteCount());
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

    private ChapterBoardReview chapterReview(Chapter chapter, User boardMember) {
        ChapterBoardReview review = new ChapterBoardReview();
        review.setReviewId(50L);
        review.setChapter(chapter);
        review.setBoardMember(boardMember);
        return review;
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

package com.example.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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

import com.example.backend.dto.EditorialBoardDtos.BoardDecisionRequest;
import com.example.backend.dto.EditorialBoardDtos.CreateUserRequest;
import com.example.backend.dto.EditorialBoardDtos.ImportReaderFeedbackRequest;
import com.example.backend.model.BoardDecision;
import com.example.backend.model.MangaSeries;
import com.example.backend.model.ReaderFeedbackImport;
import com.example.backend.model.Role;
import com.example.backend.model.SeriesRanking;
import com.example.backend.model.User;
import com.example.backend.repository.BoardDecisionRepository;
import com.example.backend.repository.ChapterLikeLogRepository;
import com.example.backend.repository.MangaSeriesRepository;
import com.example.backend.repository.NotificationRepository;
import com.example.backend.repository.PublishScheduleRepository;
import com.example.backend.repository.ReaderFeedbackImportRepository;
import com.example.backend.repository.RegistrationRequestRepository;
import com.example.backend.repository.RoleRepository;
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
    void votePublishesSeriesWhenApproveVotesReachMajority() {
        User board = user(1L, "Editorial Board One", BOARD_EMAIL, role("EDITORIAL_BOARD"));
        MangaSeries series = series(5L, "Submitted Series", "REVIEWING");
        BoardDecision decision = new BoardDecision();
        decision.setDecisionId(9L);
        decision.setSeries(series);
        decision.setBoardMember(board);
        decision.setDecisionType("APPROVE");

        when(userRepository.findByEmail(BOARD_EMAIL)).thenReturn(Optional.of(board));
        when(mangaSeriesRepository.findById(5L)).thenReturn(Optional.of(series));
        when(boardDecisionRepository.findBySeriesSeriesIdAndBoardMemberUserId(5L, 1L))
                .thenReturn(Optional.empty());
        when(boardDecisionRepository.save(any(BoardDecision.class))).thenAnswer(invocation -> {
            BoardDecision saved = invocation.getArgument(0);
            saved.setDecisionId(9L);
            return saved;
        });
        when(userRepository.countByRoleRoleNameAndStatus("EDITORIAL_BOARD", "ACTIVE")).thenReturn(1L);
        when(boardDecisionRepository.countBySeriesSeriesIdAndDecisionTypeIgnoreCase(5L, "APPROVE"))
                .thenReturn(1L);
        when(boardDecisionRepository.countBySeriesSeriesIdAndDecisionTypeIgnoreCase(5L, "REJECT"))
                .thenReturn(0L);
        when(mangaSeriesRepository.save(series)).thenReturn(series);
        when(boardDecisionRepository.findBySeriesSeriesIdOrderByDecisionDateDesc(5L))
                .thenReturn(List.of(decision));

        var response = service.voteSeries(5L, new BoardDecisionRequest("APPROVE", "Ready"));

        assertEquals("Published", series.getStatus());
        assertEquals("Published", response.status());
        assertEquals(1L, response.approveVotes());
        assertEquals(1L, response.requiredVotes());
        assertEquals("APPROVE", response.currentUserDecision());
        verify(mangaSeriesRepository).save(series);
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
}

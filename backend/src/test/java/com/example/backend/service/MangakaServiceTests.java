package com.example.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import com.example.backend.dto.MangakaDtos.CreateSeriesRequest;
import com.example.backend.dto.MangakaDtos.SubmitSeriesReviewRequest;
import com.example.backend.model.Chapter;
import com.example.backend.model.ChapterPage;
import com.example.backend.model.MangaSeries;
import com.example.backend.model.User;
import com.example.backend.repository.ChapterPageRepository;
import com.example.backend.repository.ChapterRepository;
import com.example.backend.repository.MangaSeriesRepository;
import com.example.backend.repository.NotificationRepository;
import com.example.backend.repository.SeriesRankingRepository;
import com.example.backend.repository.SubmissionRepository;
import com.example.backend.repository.TaskRepository;
import com.example.backend.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class MangakaServiceTests {
    private static final String EMAIL = "mangaka@test.local";

    @Mock
    private UserRepository userRepository;
    @Mock
    private MangaSeriesRepository mangaSeriesRepository;
    @Mock
    private ChapterRepository chapterRepository;
    @Mock
    private ChapterPageRepository chapterPageRepository;
    @Mock
    private TaskRepository taskRepository;
    @Mock
    private SubmissionRepository submissionRepository;
    @Mock
    private SeriesRankingRepository seriesRankingRepository;
    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private MangakaService service;

    @BeforeEach
    void authenticateMangaka() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(EMAIL, null, List.of()));
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createSeriesAssignsAuthenticatedMangakaAndDraftStatus() {
        User mangaka = user(1L, EMAIL);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(mangaka));
        when(mangaSeriesRepository.save(any(MangaSeries.class))).thenAnswer(invocation -> {
            MangaSeries series = invocation.getArgument(0);
            series.setSeriesId(10L);
            return series;
        });

        var response = service.createSeries(new CreateSeriesRequest(
                "New series", List.of("Action", "Comedy"), null, "Description", null, null));

        assertEquals(10L, response.id());
        assertEquals("DRAFT", response.status());
        assertEquals(List.of("Action", "Comedy"), response.genres());
        verify(mangaSeriesRepository).save(any(MangaSeries.class));
    }

    @Test
    void createSeriesKeepsLongCoverUrl() {
        User mangaka = user(1L, EMAIL);
        String coverUrl = "https://cdn.example.test/covers/series-cover.jpg?token="
                + "a".repeat(300);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(mangaka));
        when(mangaSeriesRepository.save(any(MangaSeries.class))).thenAnswer(invocation -> {
            MangaSeries series = invocation.getArgument(0);
            series.setSeriesId(10L);
            return series;
        });

        var response = service.createSeries(new CreateSeriesRequest(
                "New series", List.of("Action"), "  " + coverUrl + "  ", null, null, null));

        ArgumentCaptor<MangaSeries> captor = ArgumentCaptor.forClass(MangaSeries.class);
        verify(mangaSeriesRepository).save(captor.capture());
        assertEquals(coverUrl, captor.getValue().getCoverImage());
        assertEquals(coverUrl, response.coverUrl());
    }

    @Test
    void submitSeriesRejectsSeriesOwnedByAnotherMangaka() {
        MangaSeries series = new MangaSeries();
        series.setSeriesId(20L);
        series.setAuthor(user(2L, "other@test.local"));
        series.setStatus("DRAFT");
        when(mangaSeriesRepository.findById(20L)).thenReturn(Optional.of(series));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> service.submitSeries(20L, new SubmitSeriesReviewRequest("storyboard-url")));

        assertSame(HttpStatus.FORBIDDEN, exception.getStatusCode());
        verify(mangaSeriesRepository, never()).save(any());
    }

    @Test
    void submitSeriesMovesDraftToTantouReview() {
        MangaSeries series = new MangaSeries();
        series.setSeriesId(20L);
        series.setAuthor(user(1L, EMAIL));
        series.setStatus("DRAFT");
        when(mangaSeriesRepository.findById(20L)).thenReturn(Optional.of(series));
        when(mangaSeriesRepository.save(series)).thenReturn(series);

        var response = service.submitSeries(20L, new SubmitSeriesReviewRequest("storyboard-url"));

        assertEquals("TANTOU_REVIEW", series.getStatus());
        assertEquals("TANTOU_REVIEW", response.status());
    }

    @Test
    void getsChaptersForOwnedSeries() {
        MangaSeries series = new MangaSeries();
        series.setSeriesId(20L);
        series.setAuthor(user(1L, EMAIL));
        Chapter chapter = new Chapter();
        chapter.setChapterId(30L);
        chapter.setSeries(series);
        chapter.setChapterNumber(1);
        chapter.setTitle("Chapter 1");
        chapter.setStatus("DRAFT");
        when(mangaSeriesRepository.findById(20L)).thenReturn(Optional.of(series));
        when(chapterRepository.findBySeriesSeriesIdOrderByChapterNumberAsc(20L))
                .thenReturn(List.of(chapter));

        var response = service.getSeriesChapters(20L);

        assertEquals(1, response.size());
        assertEquals(30L, response.get(0).id());
        assertEquals(1, response.get(0).chapterNumber());
    }

    @Test
    void getsPagesForOwnedChapter() {
        MangaSeries series = new MangaSeries();
        series.setSeriesId(20L);
        series.setAuthor(user(1L, EMAIL));
        Chapter chapter = new Chapter();
        chapter.setChapterId(30L);
        chapter.setSeries(series);
        ChapterPage page = new ChapterPage();
        page.setPageId(40L);
        page.setChapter(chapter);
        page.setPageNumber(1);
        page.setImageUrl("/static/covers/page-1.png");
        page.setPageStatus("DRAFT");
        when(chapterRepository.findById(30L)).thenReturn(Optional.of(chapter));
        when(chapterPageRepository.findByChapterChapterIdOrderByPageNumberAsc(30L))
                .thenReturn(List.of(page));

        var response = service.getChapterPages(30L);

        assertEquals(1, response.size());
        assertEquals(40L, response.get(0).id());
        assertEquals(30L, response.get(0).chapterId());
        assertEquals(1, response.get(0).pageNumber());
        assertEquals("/static/covers/page-1.png", response.get(0).imageUrl());
    }

    private User user(Long id, String email) {
        User user = new User();
        user.setUserId(id);
        user.setEmail(email);
        user.setUsername(email);
        return user;
    }
}

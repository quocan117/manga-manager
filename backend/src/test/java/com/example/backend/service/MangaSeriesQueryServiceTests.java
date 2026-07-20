package com.example.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.example.backend.model.Chapter;
import com.example.backend.model.MangaSeries;
import com.example.backend.model.User;
import com.example.backend.repository.ChapterLikeLogRepository;
import com.example.backend.repository.ChapterRepository;
import com.example.backend.repository.MangaSeriesRepository;

@ExtendWith(MockitoExtension.class)
class MangaSeriesQueryServiceTests {
    @Mock
    private MangaSeriesRepository mangaSeriesRepository;
    @Mock
    private ChapterRepository chapterRepository;
    @Mock
    private ChapterLikeLogRepository chapterLikeLogRepository;

    @InjectMocks
    private MangaSeriesQueryService service;

    @Test
    void getsOnlyPublishedSeriesAndPublishedChapters() {
        MangaSeries series = series(10L, "Haikyuu!!", "Published");
        Chapter chapter = chapter(20L, series, "PUBLISHED");

        when(mangaSeriesRepository.findPublicSeriesWithPublishedChaptersOrderByCreatedAtDesc(
                "Published", "PUBLISHED"))
                .thenReturn(List.of(series));
        when(chapterRepository.findBySeriesSeriesIdAndStatusIgnoreCaseOrderByChapterNumberAsc(
                10L, "PUBLISHED"))
                .thenReturn(List.of(chapter));
        when(chapterLikeLogRepository.countByChapterChapterId(20L)).thenReturn(33L);

        var response = service.getAll();

        assertEquals(1, response.size());
        assertEquals("Haikyuu!!", response.get(0).getTitle());
        assertEquals("Publishing", response.get(0).getStatus());
        assertEquals(1, response.get(0).getChapters().size());
        assertEquals(33L, response.get(0).getChapters().get(0).getLikes());
        verify(mangaSeriesRepository).findPublicSeriesWithPublishedChaptersOrderByCreatedAtDesc(
                "Published", "PUBLISHED");
    }

    @Test
    void unpublishedSeriesDetailReturnsNotFound() {
        when(mangaSeriesRepository.findBySeriesIdAndStatusIgnoreCase(10L, "Published"))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> service.getDetail(10L));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    private MangaSeries series(Long id, String title, String status) {
        User author = new User();
        author.setUsername("Haruichi Furudate");

        MangaSeries series = new MangaSeries();
        series.setSeriesId(id);
        series.setTitle(title);
        series.setAuthor(author);
        series.setGenre("Thể thao, Học đường");
        series.setCoverImage("haikyuu.jpg");
        series.setDescription("Volleyball story");
        series.setStatus(status);
        return series;
    }

    private Chapter chapter(Long id, MangaSeries series, String status) {
        Chapter chapter = new Chapter();
        chapter.setChapterId(id);
        chapter.setSeries(series);
        chapter.setTitle("Chapter 1");
        chapter.setStatus(status);
        return chapter;
    }
}

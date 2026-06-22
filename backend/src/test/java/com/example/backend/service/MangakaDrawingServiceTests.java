package com.example.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import com.example.backend.dto.DrawingDtos.SaveDrawingRequest;
import com.example.backend.model.Chapter;
import com.example.backend.model.ChapterPage;
import com.example.backend.model.MangaSeries;
import com.example.backend.model.PageDrawing;
import com.example.backend.model.User;
import com.example.backend.repository.ChapterPageRepository;
import com.example.backend.repository.PageDrawingRepository;
import com.example.backend.repository.PageDrawingRevisionRepository;
import com.example.backend.repository.SubmissionRepository;
import com.example.backend.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class MangakaDrawingServiceTests {
    private static final String EMAIL = "artist@manga.test";

    @Mock
    private ChapterPageRepository pageRepository;
    @Mock
    private PageDrawingRepository drawingRepository;
    @Mock
    private PageDrawingRevisionRepository revisionRepository;
    @Mock
    private SubmissionRepository submissionRepository;
    @Mock
    private UserRepository userRepository;

    private MangakaDrawingService service;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        service = new MangakaDrawingService(
                pageRepository,
                drawingRepository,
                revisionRepository,
                submissionRepository,
                userRepository,
                objectMapper);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(EMAIL, null, List.of()));
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createsDrawingAndFirstRevisionForOwnedPage() throws Exception {
        User mangaka = user(1L, EMAIL);
        ChapterPage page = page(10L, mangaka);
        when(pageRepository.findById(10L)).thenReturn(Optional.of(page));
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(mangaka));
        when(drawingRepository.findByPagePageIdAndTaskIsNull(10L)).thenReturn(Optional.empty());
        when(drawingRepository.saveAndFlush(any(PageDrawing.class))).thenAnswer(invocation -> {
            PageDrawing drawing = invocation.getArgument(0);
            drawing.setDrawingId(20L);
            drawing.setVersion(0L);
            return drawing;
        });

        var response = service.saveDrawing(
                10L,
                new SaveDrawingRequest(
                        objectMapper.readTree("{\"objects\":[]}"),
                        "preview.png",
                        null,
                        null));

        assertEquals(20L, response.id());
        assertEquals(0L, response.version());
        assertEquals("DRAFT", response.status());
        assertEquals(0, response.canvasData().get("objects").size());
        verify(revisionRepository).save(any());
    }

    @Test
    void rejectsStaleDrawingVersion() throws Exception {
        User mangaka = user(1L, EMAIL);
        ChapterPage page = page(10L, mangaka);
        PageDrawing drawing = new PageDrawing();
        drawing.setDrawingId(20L);
        drawing.setPage(page);
        drawing.setOwner(mangaka);
        drawing.setVersion(3L);
        drawing.setCanvasData("{}");
        when(pageRepository.findById(10L)).thenReturn(Optional.of(page));
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(mangaka));
        when(drawingRepository.findByPagePageIdAndTaskIsNull(10L)).thenReturn(Optional.of(drawing));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.saveDrawing(
                        10L,
                        new SaveDrawingRequest(
                                objectMapper.readTree("{}"), null, null, 2L)));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(drawingRepository, never()).saveAndFlush(any());
    }

    @Test
    void rejectsPageOwnedByAnotherMangaka() {
        ChapterPage page = page(10L, user(2L, "other@manga.test"));
        when(pageRepository.findById(10L)).thenReturn(Optional.of(page));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.getDrawing(10L));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        verify(drawingRepository, never()).findByPagePageIdAndTaskIsNull(any());
    }

    private ChapterPage page(Long pageId, User author) {
        MangaSeries series = new MangaSeries();
        series.setSeriesId(30L);
        series.setAuthor(author);
        Chapter chapter = new Chapter();
        chapter.setChapterId(40L);
        chapter.setSeries(series);
        ChapterPage page = new ChapterPage();
        page.setPageId(pageId);
        page.setChapter(chapter);
        return page;
    }

    private User user(Long id, String email) {
        User user = new User();
        user.setUserId(id);
        user.setEmail(email);
        return user;
    }
}

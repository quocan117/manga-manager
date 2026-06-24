package com.example.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.example.backend.model.Chapter;
import com.example.backend.model.ChapterLikeLog;
import com.example.backend.model.GuestAccessLog;
import com.example.backend.model.MangaSeries;
import com.example.backend.repository.ChapterLikeLogRepository;
import com.example.backend.repository.ChapterRepository;
import com.example.backend.repository.GuestAccessLogRepository;

@ExtendWith(MockitoExtension.class)
class LikeServiceTests {
    @Mock
    private ChapterRepository chapterRepository;
    @Mock
    private GuestAccessLogRepository guestRepository;
    @Mock
    private ChapterLikeLogRepository likeRepository;

    @InjectMocks
    private LikeService service;

    @Test
    void likesChapterUsingSessionToken() {
        Chapter chapter = chapter(5L);
        GuestAccessLog guest = guest(9L, "guest-token");
        when(chapterRepository.findById(5L)).thenReturn(Optional.of(chapter));
        when(guestRepository.findBySessionToken("guest-token")).thenReturn(Optional.of(guest));
        when(likeRepository.existsByGuestLogLogIdAndChapterChapterId(9L, 5L)).thenReturn(false);

        service.likeChapter(5L, null, "guest-token");

        verify(likeRepository).saveAndFlush(any(ChapterLikeLog.class));
    }

    @Test
    void acceptsSessionTokenInLegacyLogIdField() {
        Chapter chapter = chapter(5L);
        GuestAccessLog guest = guest(9L, "guest-token");
        when(chapterRepository.findById(5L)).thenReturn(Optional.of(chapter));
        when(guestRepository.findBySessionToken("guest-token")).thenReturn(Optional.of(guest));
        when(likeRepository.existsByGuestLogLogIdAndChapterChapterId(9L, 5L)).thenReturn(false);

        service.likeChapter(5L, "guest-token", null);

        verify(likeRepository).saveAndFlush(any(ChapterLikeLog.class));
    }

    @Test
    void duplicateLikeReturnsConflict() {
        Chapter chapter = chapter(5L);
        GuestAccessLog guest = guest(9L, "guest-token");
        when(chapterRepository.findById(5L)).thenReturn(Optional.of(chapter));
        when(guestRepository.findBySessionToken("guest-token")).thenReturn(Optional.of(guest));
        when(likeRepository.existsByGuestLogLogIdAndChapterChapterId(9L, 5L)).thenReturn(true);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> service.likeChapter(5L, null, "guest-token"));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(likeRepository, never()).saveAndFlush(any());
    }

    @Test
    void unpublishedChapterIsNotLikable() {
        Chapter chapter = chapter(5L);
        chapter.setStatus("DRAFT");
        when(chapterRepository.findById(5L)).thenReturn(Optional.of(chapter));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> service.likeChapter(5L, null, "guest-token"));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verify(guestRepository, never()).findBySessionToken(any());
        verify(likeRepository, never()).saveAndFlush(any());
    }

    private Chapter chapter(Long id) {
        MangaSeries series = new MangaSeries();
        series.setSeriesId(3L);
        series.setStatus("Published");
        Chapter chapter = new Chapter();
        chapter.setChapterId(id);
        chapter.setSeries(series);
        chapter.setStatus("PUBLISHED");
        return chapter;
    }

    private GuestAccessLog guest(Long id, String token) {
        GuestAccessLog guest = new GuestAccessLog();
        guest.setLogId(id);
        guest.setSessionToken(token);
        return guest;
    }
}

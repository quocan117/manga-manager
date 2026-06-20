package com.example.backend.service;

import java.time.LocalDateTime;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.backend.model.Chapter;
import com.example.backend.model.ChapterLikeLog;
import com.example.backend.model.GuestAccessLog;
import com.example.backend.repository.ChapterLikeLogRepository;
import com.example.backend.repository.ChapterRepository;
import com.example.backend.repository.GuestAccessLogRepository;

@Service
public class LikeService {
        private final ChapterRepository chapterRepository;
        private final GuestAccessLogRepository guestRepository;
        private final ChapterLikeLogRepository likeRepository;

        public LikeService(
                        ChapterRepository chapterRepository,
                        GuestAccessLogRepository guestRepository,
                        ChapterLikeLogRepository likeRepository) {
                this.chapterRepository = chapterRepository;
                this.guestRepository = guestRepository;
                this.likeRepository = likeRepository;
        }

        @Transactional
        public void likeChapter(Long chapterId, String logId, String sessionToken) {

                Chapter chapter = chapterRepository.findById(chapterId)
                                .orElseThrow(() -> new ResponseStatusException(
                                                HttpStatus.NOT_FOUND, "Chapter not found"));

                GuestAccessLog guest = resolveGuest(logId, sessionToken);

                if (likeRepository.existsByGuestLogLogIdAndChapterChapterId(
                                guest.getLogId(), chapter.getChapterId())) {
                        throw new ResponseStatusException(
                                        HttpStatus.CONFLICT, "This guest already liked the chapter");
                }

                ChapterLikeLog like = new ChapterLikeLog();

                like.setChapter(chapter);

                like.setGuestLog(guest);

                like.setLikedAt(LocalDateTime.now());

                try {
                        likeRepository.saveAndFlush(like);
                } catch (DataIntegrityViolationException exception) {
                        throw new ResponseStatusException(
                                        HttpStatus.CONFLICT,
                                        "This guest already liked the chapter",
                                        exception);
                }

        }

        private GuestAccessLog resolveGuest(String logId, String sessionToken) {
                if (sessionToken != null && !sessionToken.isBlank()) {
                        return guestRepository.findBySessionToken(sessionToken.trim())
                                        .orElseThrow(() -> new ResponseStatusException(
                                                        HttpStatus.NOT_FOUND, "Guest session not found"));
                }
                if (logId != null && !logId.isBlank()) {
                        String identity = logId.trim();
                        try {
                                Long numericLogId = Long.valueOf(identity);
                                return guestRepository.findById(numericLogId)
                                        .orElseThrow(() -> new ResponseStatusException(
                                                        HttpStatus.NOT_FOUND, "Guest log not found"));
                        } catch (NumberFormatException exception) {
                                return guestRepository.findBySessionToken(identity)
                                                .orElseThrow(() -> new ResponseStatusException(
                                                                HttpStatus.NOT_FOUND,
                                                                "Guest session not found"));
                        }
                }
                throw new ResponseStatusException(
                                HttpStatus.BAD_REQUEST, "sessionToken or logId is required");

        }
}

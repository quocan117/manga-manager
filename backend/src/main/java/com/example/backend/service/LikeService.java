package com.example.backend.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

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

        public void likeChapter(
                        Long chapterId,
                        Long logId) {

                Chapter chapter = chapterRepository.findById(chapterId)
                                .orElseThrow();

                GuestAccessLog guest = guestRepository.findById(logId)
                                .orElseThrow();

                ChapterLikeLog like = new ChapterLikeLog();

                like.setChapter(chapter);

                like.setGuestLog(guest);

                like.setLikedAt(LocalDateTime.now());

                likeRepository.save(like);

        }
}

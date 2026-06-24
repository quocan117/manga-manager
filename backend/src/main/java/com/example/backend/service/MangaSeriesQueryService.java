package com.example.backend.service;

import java.util.Arrays;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.backend.dto.ChapterSummaryResponse;
import com.example.backend.dto.MangaSeriesDetailResponse;
import com.example.backend.model.MangaSeries;
import com.example.backend.repository.ChapterLikeLogRepository;
import com.example.backend.repository.ChapterRepository;
import com.example.backend.repository.MangaSeriesRepository;

@Service
public class MangaSeriesQueryService {
        private static final String PUBLISHED_SERIES_STATUS = "Published";
        private static final String PUBLISHED_CHAPTER_STATUS = "PUBLISHED";

        private final MangaSeriesRepository mangaSeriesRepository;
        private final ChapterRepository chapterRepository;
        private final ChapterLikeLogRepository chapterLikeLogRepository;

        public MangaSeriesQueryService(
                        MangaSeriesRepository mangaSeriesRepository,
                        ChapterRepository chapterRepository,
                        ChapterLikeLogRepository chapterLikeLogRepository) {
                this.mangaSeriesRepository = mangaSeriesRepository;
                this.chapterRepository = chapterRepository;
                this.chapterLikeLogRepository = chapterLikeLogRepository;
        }

        public MangaSeriesDetailResponse getDetail(Long seriesId) {
                MangaSeries series = mangaSeriesRepository
                                .findBySeriesIdAndStatusIgnoreCase(seriesId, PUBLISHED_SERIES_STATUS)
                                .orElseThrow(() -> new ResponseStatusException(
                                                HttpStatus.NOT_FOUND, "Manga series not found"));

                return toResponse(series);
        }

        public List<MangaSeriesDetailResponse> getAll() {
                return mangaSeriesRepository
                                .findByStatusIgnoreCaseOrderByCreatedAtDesc(PUBLISHED_SERIES_STATUS)
                                .stream()
                                .map(this::toResponse)
                                .toList();
        }

        private MangaSeriesDetailResponse toResponse(MangaSeries series) {
                List<ChapterSummaryResponse> chapters = chapterRepository
                                .findBySeriesSeriesIdAndStatusIgnoreCaseOrderByChapterNumberAsc(
                                                series.getSeriesId(), PUBLISHED_CHAPTER_STATUS)
                                .stream()
                                .map(chapter -> new ChapterSummaryResponse(
                                                chapter.getChapterId(),
                                                chapter.getTitle(),
                                                chapterLikeLogRepository
                                                                .countByChapterChapterId(chapter.getChapterId())))
                                .toList();

                String author = series.getAuthor() == null
                                ? null
                                : series.getAuthor().getUsername();

                return new MangaSeriesDetailResponse(
                                series.getSeriesId(),
                                series.getTitle(),
                                author,
                                parseGenres(series.getGenre()),
                                series.getCoverImage(),
                                series.getDescription(),
                                series.getStatus(),
                                chapters);
        }

        private List<String> parseGenres(String genre) {
                if (genre == null || genre.isBlank()) {
                        return List.of();
                }

                return Arrays.stream(genre.split(","))
                                .map(String::trim)
                                .filter(value -> !value.isEmpty())
                                .toList();
        }
}

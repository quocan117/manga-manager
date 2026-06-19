package com.example.backend.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MangaSeriesDetailResponse {
    private Long id;
    private String title;
    private String author;
    private List<String> genres;
    private String coverUrl;
    private String description;
    private String status;
    private List<ChapterSummaryResponse> chapters;
}

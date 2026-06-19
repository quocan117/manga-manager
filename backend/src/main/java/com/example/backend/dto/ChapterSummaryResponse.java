package com.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ChapterSummaryResponse {
    private Long id;
    private String title;
    private long likes;
}

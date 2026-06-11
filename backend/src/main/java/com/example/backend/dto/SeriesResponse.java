package com.example.backend.dto;

import com.example.backend.model.Series;
import com.example.backend.model.SeriesStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class SeriesResponse {

    private Long id;
    private String name;
    private String genre;
    private String summary;
    private SeriesStatus status;
    private LocalDateTime createdAt;
    private List<CharacterResponse> characters;
    private List<DraftResponse> drafts;

    public static SeriesResponse from(Series series) {
        return SeriesResponse.builder()
                .id(series.getId())
                .name(series.getName())
                .genre(series.getGenre())
                .summary(series.getSummary())
                .status(series.getStatus())
                .createdAt(series.getCreatedAt())
                .characters(series.getCharacters().stream()
                        .map(CharacterResponse::from)
                        .toList())
                .drafts(series.getDrafts().stream()
                        .map(DraftResponse::from)
                        .toList())
                .build();
    }
}

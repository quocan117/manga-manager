package com.example.backend.dto;

import com.example.backend.model.SeriesCharacter;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CharacterResponse {

    private Long id;
    private String name;
    private String description;

    public static CharacterResponse from(SeriesCharacter character) {
        return CharacterResponse.builder()
                .id(character.getId())
                .name(character.getName())
                .description(character.getDescription())
                .build();
    }
}

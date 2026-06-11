package com.example.backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "series")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Series {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 200)
    private String name;

    @Column(nullable = false, length = 100)
    private String genre;

    @Column(nullable = false, length = 2000)
    private String summary;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private SeriesStatus status = SeriesStatus.DRAFT;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "series", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SeriesCharacter> characters = new ArrayList<>();

    @OneToMany(mappedBy = "series", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Draft> drafts = new ArrayList<>();

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public void addCharacter(SeriesCharacter character) {
        characters.add(character);
        character.setSeries(this);
    }

    public void addDraft(Draft draft) {
        drafts.add(draft);
        draft.setSeries(this);
    }
}

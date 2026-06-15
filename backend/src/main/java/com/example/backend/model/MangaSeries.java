package com.example.backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "manga_series")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MangaSeries {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long seriesId;

    private String title;

    @ManyToOne
    @JoinColumn(name = "author_id")
    private User author;

    private String genre;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String coverImage;
    private String status;
    private Float rankingScore;
    private String publicationType;
    private LocalDateTime createdAt;
    private String artStyle;
}
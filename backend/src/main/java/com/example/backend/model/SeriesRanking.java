package com.example.backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "series_rankings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SeriesRanking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long rankingId;

    @ManyToOne
    @JoinColumn(name = "series_id")
    private MangaSeries series;

    private Integer rankingPosition;
    private Float score;
    private LocalDateTime calculatedAt;
    private LocalDateTime periodStart;
    private LocalDateTime periodEnd;
    private Integer voteCount;
}

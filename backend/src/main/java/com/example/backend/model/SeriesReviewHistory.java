package com.example.backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "series_review_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SeriesReviewHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long historyId;

    @ManyToOne
    @JoinColumn(name = "series_id", nullable = false)
    private MangaSeries series;

    @ManyToOne
    @JoinColumn(name = "actor_id")
    private User actor;

    @Column(length = 50)
    private String actorRole;

    @Column(nullable = false, length = 100)
    private String action;

    @Column(length = 50)
    private String previousStatus;

    @Column(length = 50)
    private String newStatus;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String reason;

    private Long referenceId;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}

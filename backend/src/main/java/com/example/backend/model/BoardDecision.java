package com.example.backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "board_decisions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BoardDecision {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long decisionId;

    @ManyToOne
    @JoinColumn(name = "series_id")
    private MangaSeries series;

    @ManyToOne
    @JoinColumn(name = "board_member_id")
    private User boardMember;

    private String decisionType;

    @Column(columnDefinition = "TEXT")
    private String reason;

    private LocalDateTime decisionDate;
}
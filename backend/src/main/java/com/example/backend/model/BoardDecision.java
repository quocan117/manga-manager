package com.example.backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "board_decisions",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"series_id", "board_member_id"})
        }
)
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

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String reason;

    private LocalDateTime decisionDate;
}

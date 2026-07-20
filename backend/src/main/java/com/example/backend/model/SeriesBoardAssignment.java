package com.example.backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "series_board_assignments",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"series_id", "board_member_id"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SeriesBoardAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long assignmentId;

    @ManyToOne
    @JoinColumn(name = "series_id")
    private MangaSeries series;

    @ManyToOne
    @JoinColumn(name = "board_member_id")
    private User boardMember;

    private LocalDateTime assignedAt;
}

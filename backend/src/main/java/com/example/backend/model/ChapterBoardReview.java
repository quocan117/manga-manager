package com.example.backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "chapter_board_reviews",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"chapter_id", "board_member_id"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChapterBoardReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reviewId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "chapter_id", nullable = false)
    private Chapter chapter;

    @ManyToOne(optional = false)
    @JoinColumn(name = "board_member_id", nullable = false)
    private User boardMember;

    private Boolean confirmed;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String comment;

    private LocalDateTime reviewedAt;
}

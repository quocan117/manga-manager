package com.example.backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "review_comments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long commentId;

    @ManyToOne
    @JoinColumn(name = "page_id")
    private ChapterPage page;

    @ManyToOne
    @JoinColumn(name = "editor_id")
    private User editor;

    @Column(columnDefinition = "TEXT")
    private String commentText;

    private Float positionX;
    private Float positionY;
    private LocalDateTime createdAt;
}
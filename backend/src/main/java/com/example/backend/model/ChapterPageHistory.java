package com.example.backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "chapter_page_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChapterPageHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long historyId;

    @ManyToOne
    @JoinColumn(name = "page_id", nullable = false)
    private ChapterPage page;

    @ManyToOne
    @JoinColumn(name = "submission_id")
    private Submission submission;

    @ManyToOne
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String previousImageUrl;

    @Column(columnDefinition = "NVARCHAR(MAX)", nullable = false)
    private String newImageUrl;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}

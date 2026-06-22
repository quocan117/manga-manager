package com.example.backend.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "page_drawings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PageDrawing {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long drawingId;

    @ManyToOne
    @JoinColumn(name = "page_id", nullable = false)
    private ChapterPage page;

    @ManyToOne
    @JoinColumn(name = "task_id")
    private Task task;

    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @ManyToOne
    @JoinColumn(name = "source_submission_id")
    private Submission sourceSubmission;

    @Column(columnDefinition = "NVARCHAR(MAX)", nullable = false)
    private String canvasData;

    @Column(columnDefinition = "TEXT")
    private String previewImageUrl;

    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Version
    private Long version;
}

package com.example.backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "chapter_revision_notes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChapterRevisionNote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long noteId;

    @ManyToOne
    @JoinColumn(name = "chapter_id")
    private Chapter chapter;

    @ManyToOne
    @JoinColumn(name = "editor_id")
    private User editor;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String imageUrl;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String canvasData;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String description;

    @Column(nullable = false)
    private Integer roundNumber = 1;

    private Integer orderIndex;
    private LocalDateTime createdAt;
}

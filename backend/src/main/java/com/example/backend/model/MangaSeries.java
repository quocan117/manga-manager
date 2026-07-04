package com.example.backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "manga_series")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MangaSeries {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long seriesId;

    @Column(columnDefinition = "NVARCHAR(255)")
    private String title;

    @ManyToOne
    @JoinColumn(name = "author_id")
    private User author;

    @ManyToOne
    @JoinColumn(name = "tantou_editor_id")
    private User tantouEditor;

    @Column(columnDefinition = "NVARCHAR(255)")
    private String genre;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String description;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String coverImage;
    private String status;
    private Float rankingScore;
    private String publicationType;
    private LocalDateTime createdAt;
    private String artStyle;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String storyboardUrl;

    private LocalDateTime submittedAt;

    @Column(name = "editor_assigned_at")
    private LocalDateTime editorAssignedAt;

    public LocalDateTime getEditorAssignedAt() {
        return editorAssignedAt;
    }

    public void setEditorAssignedAt(LocalDateTime editorAssignedAt) {
        this.editorAssignedAt = editorAssignedAt;
    }
}

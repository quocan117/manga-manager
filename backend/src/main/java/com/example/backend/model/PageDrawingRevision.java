package com.example.backend.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "page_drawing_revisions", uniqueConstraints = @UniqueConstraint(name = "uk_drawing_revision_version", columnNames = {
        "drawing_id", "version_number" }))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PageDrawingRevision {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long revisionId;

    @ManyToOne
    @JoinColumn(name = "drawing_id", nullable = false)
    private PageDrawing drawing;

    @ManyToOne
    @JoinColumn(name = "saved_by", nullable = false)
    private User savedBy;

    private Long versionNumber;

    @Column(columnDefinition = "NVARCHAR(MAX)", nullable = false)
    private String canvasData;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String previewImageUrl;

    private String status;
    private LocalDateTime createdAt;
}

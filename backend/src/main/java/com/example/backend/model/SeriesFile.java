package com.example.backend.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;

@Entity
@Table(name = "series_files")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SeriesFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long fileId;

    @ManyToOne
    @JoinColumn(name = "series_id")
    private MangaSeries series;

    @ManyToOne
    @JoinColumn(name = "task_id")
    private Task task;

    @ManyToOne
    @JoinColumn(name = "chapter_id")
    private Chapter chapter;

    @ManyToOne
    @JoinColumn(name = "uploaded_by")
    private User uploadedBy;

    @Column(columnDefinition = "NVARCHAR(255)")
    private String fileName;

    @Column(columnDefinition = "NVARCHAR(255)")
    private String originalFileName;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String fileUrl;

    @Column(columnDefinition = "NVARCHAR(255)")
    private String contentType;

    private Long fileSize;

    @Column(columnDefinition = "NVARCHAR(50)")
    private String fileType;

    @Column(nullable = false)
    @ColumnDefault("1")
    private Integer roundNumber = 1;

    @Column(columnDefinition = "NVARCHAR(50)")
    private String purpose;

    @Column(nullable = false)
    private Boolean active = true;

    private LocalDateTime uploadedAt;
}

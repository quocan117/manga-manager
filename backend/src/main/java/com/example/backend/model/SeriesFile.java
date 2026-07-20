package com.example.backend.model;

import jakarta.persistence.*;
import lombok.*;

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

    private LocalDateTime uploadedAt;
}

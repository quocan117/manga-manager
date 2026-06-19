package com.example.backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "chapters")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Chapter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long chapterId;

    @ManyToOne
    @JoinColumn(name = "series_id")
    private MangaSeries series;

    private Integer chapterNumber;

    @Column(columnDefinition = "NVARCHAR(255)")
    private String title;
    private String status;
    private LocalDateTime releaseDate;
    private LocalDateTime createdAt;
}

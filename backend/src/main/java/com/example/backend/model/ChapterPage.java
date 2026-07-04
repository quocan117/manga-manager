package com.example.backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "pages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChapterPage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pageId;

    @ManyToOne
    @JoinColumn(name = "chapter_id")
    private Chapter chapter;

    private Integer pageNumber;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String imageUrl;

    private String pageStatus;
    private LocalDateTime createdAt;
}
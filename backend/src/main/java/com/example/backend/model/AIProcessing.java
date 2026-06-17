package com.example.backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ai_processing")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AIProcessing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long aiProcessId;

    @ManyToOne
    @JoinColumn(name = "chapterpage_id")
    private ChapterPage chapterPage;

    private String processType;
    private String status;
    private String resultUrl;
    private LocalDateTime createdAt;

}

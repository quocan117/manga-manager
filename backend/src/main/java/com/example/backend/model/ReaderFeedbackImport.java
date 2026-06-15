package com.example.backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reader_feedback_imports")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReaderFeedbackImport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long importId;

    @ManyToOne
    @JoinColumn(name = "series_id")
    private MangaSeries series;

    @ManyToOne
    @JoinColumn(name = "imported_by")
    private User importedBy;

    private String period;
    private Integer voteCount;
    private Float avgScore;

    @Column(columnDefinition = "TEXT")
    private String sourceNote;

    private LocalDateTime importedAt;
}
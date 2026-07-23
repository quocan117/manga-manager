package com.example.backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "task_markup_pages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TaskMarkupPage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long markupPageId;

    @ManyToOne
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @Column(nullable = false)
    private Integer roundNumber;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String imageUrl;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String canvasData;

    private Integer orderIndex;
    private LocalDateTime createdAt;
}

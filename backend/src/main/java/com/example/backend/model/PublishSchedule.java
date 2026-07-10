package com.example.backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "publish_schedules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PublishSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long scheduleId;

    @ManyToOne
    @JoinColumn(name = "series_id")
    private MangaSeries series;

    private LocalDateTime publishDate;
    private String frequency;
    private String status;
    private Boolean overdueNotified = false;
}
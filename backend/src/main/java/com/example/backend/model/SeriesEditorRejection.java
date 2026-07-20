package com.example.backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "series_editor_rejections",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"series_id", "editor_id"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SeriesEditorRejection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long rejectionId;

    @ManyToOne
    @JoinColumn(name = "series_id")
    private MangaSeries series;

    @ManyToOne
    @JoinColumn(name = "editor_id")
    private User editor;

    private LocalDateTime rejectedAt;
}

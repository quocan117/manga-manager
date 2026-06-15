package com.example.backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "chapter_like_logs",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"log_id", "chapter_id"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChapterLikeLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long likeId;

    @ManyToOne
    @JoinColumn(name = "log_id")
    private GuestAccessLog guestLog;

    @ManyToOne
    @JoinColumn(name = "chapter_id")
    private Chapter chapter;

    private LocalDateTime likedAt;
}
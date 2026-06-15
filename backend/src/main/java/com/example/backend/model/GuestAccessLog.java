package com.example.backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "guest_access_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GuestAccessLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long logId;

    private String sessionToken;
    private String ipAddress;

    @Column(columnDefinition = "TEXT")
    private String userAgent;

    private LocalDateTime createdAt;
    private LocalDateTime lastActiveAt;
}
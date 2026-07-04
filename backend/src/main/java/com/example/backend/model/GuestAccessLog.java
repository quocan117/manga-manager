package com.example.backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "guest_access_logs",
        indexes = @Index(
                name = "ux_guest_access_session_token",
                columnList = "session_token",
                unique = true))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GuestAccessLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long logId;

    @Column(nullable = false, unique = true)
    private String sessionToken;
    private String ipAddress;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String userAgent;

    private LocalDateTime createdAt;
    private LocalDateTime lastActiveAt;
}

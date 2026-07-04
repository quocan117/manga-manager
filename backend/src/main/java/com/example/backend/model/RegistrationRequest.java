package com.example.backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "registration_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long requestId;

    private String fullName;

    @Column(unique = true)
    private String email;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String portfolioUrl;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String introduction;

    private String phoneNumber;

    private String requestedRole;

    /**
     * PENDING
     * APPROVED
     * REJECTED
     */
    private String status;

    private LocalDateTime createdAt;

    private LocalDateTime reviewedAt;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String reviewNote;

    @ManyToOne
    @JoinColumn(name = "reviewed_by")
    private User reviewedBy;
}

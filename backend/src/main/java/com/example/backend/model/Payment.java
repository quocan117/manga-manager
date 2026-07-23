package com.example.backend.model;

import lombok.*;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentId;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private String paymentType;

    private String paymentSource;

    private Integer approvedPages;

    private BigDecimal amount;

    private String status;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String description;

    private LocalDateTime createdAt;
}

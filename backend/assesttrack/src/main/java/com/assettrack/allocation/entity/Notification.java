package com.assettrack.allocation.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(length = 2000)
    private String message;

    // Values: "warning", "info", "critical", "success"
    private String category;

    // For warranty alerts: the asset serial number
    // For low-stock alerts: "LOW_STOCK:<type>"
    private String assetTag;

    private LocalDateTime createdAt;

    private boolean unread;
}
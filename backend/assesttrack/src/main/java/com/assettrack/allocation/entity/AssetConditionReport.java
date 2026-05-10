package com.assettrack.allocation.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "asset_condition_reports")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssetConditionReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The asset being reported
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id", nullable = false)
    private Asset asset;

    // The user who filed the report
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_by_id", nullable = false)
    private User reportedBy;

    @Column(nullable = false)
    private String issueType;   // e.g. "Broken", "Weak battery", "Screen damage"

    @Column(length = 2000)
    private String description;

    // "OPEN", "IN_PROGRESS", "RESOLVED"
    @Builder.Default
    private String status = "OPEN";

    @Column(length = 2000)
    private String adminNotes;

    @Builder.Default
    private LocalDateTime reportedAt = LocalDateTime.now();

    private LocalDateTime resolvedAt;
}

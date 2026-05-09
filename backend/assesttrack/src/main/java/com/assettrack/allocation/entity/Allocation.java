package com.assettrack.allocation.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "allocations",
    indexes = {
        @Index(name = "idx_allocation_asset",  columnList = "asset_id"),
        @Index(name = "idx_allocation_user",   columnList = "assigned_to_id"),
        @Index(name = "idx_allocation_active", columnList = "active")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Allocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The asset being allocated
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id", nullable = false)
    private Asset asset;

    // The user who received the asset
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to_id", nullable = false)
    private User assignedTo;

    // The admin/manager who performed the assignment
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_by_id", nullable = false)
    private User assignedBy;

    @Column(nullable = false)
    private LocalDateTime assignedDate;

    // Null until the asset is returned
    private LocalDateTime returnedDate;

    // Only ONE allocation per asset can be active = true
    @Column(nullable = false)
    private boolean active;

    @Column(length = 500)
    private String notes;
}

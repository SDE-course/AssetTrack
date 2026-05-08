package com.assettrack.allocation.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "allocations", indexes = {
    @Index(name = "idx_allocation_asset", columnList = "asset_id"),
    @Index(name = "idx_allocation_user",  columnList = "assigned_to_id"),
    @Index(name = "idx_allocation_active", columnList = "active")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Allocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "asset_id", nullable = false)
    private Asset asset;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assigned_to_id", nullable = false)
    private User assignedTo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assigned_by_id", nullable = false)
    private User assignedBy;

    @Column(nullable = false)
    private LocalDateTime assignedDate;

    private LocalDateTime returnedDate;

    @Column(nullable = false)
    private boolean active;

    @Column(length = 500)
    private String notes;
}

package com.assettrack.allocation.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "assets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Asset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String serialNumber;

    private String brand;

    private String type;

    // Extra fields used by spare-laptop filter
    private Integer ram;      // in GB
    private Integer storage;  // in GB

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssetStatus status;

    @OneToMany(mappedBy = "asset", fetch = FetchType.LAZY)
    private List<Allocation> allocations;
}

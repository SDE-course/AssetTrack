package com.assettrack.allocation.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "assets")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Asset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String serialNumber;

    private String brand;

    @Column(nullable = false)
    private String type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssetStatus status;

    // RAM / storage fields for spare-laptop filtering
    private Integer ram;
    private Integer storage;

    @OneToMany(mappedBy = "asset", fetch = FetchType.LAZY)
    private java.util.List<Allocation> allocations;
}

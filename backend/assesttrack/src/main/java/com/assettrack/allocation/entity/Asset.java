package com.assettrack.allocation.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
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
    private String name; // كان model → خليناه name

    @Column(nullable = false, unique = true)
    private String serialNumber;

    private String brand;

    private String type;

    private LocalDate purchaseDate;

    private LocalDate warrantyExpiryDate;

    private Integer ram;
    private Integer storage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssetStatus status;

    @OneToMany(mappedBy = "asset", fetch = FetchType.LAZY)
    private List<Allocation> allocations;
}
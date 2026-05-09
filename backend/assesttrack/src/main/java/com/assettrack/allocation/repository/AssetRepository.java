package com.assettrack.allocation.repository;

import com.assettrack.allocation.entity.Asset;
import com.assettrack.allocation.entity.AssetStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssetRepository extends JpaRepository<Asset, Long> {

    /**
     * Spare laptop query with optional brand / RAM / storage filters.
     * Null parameters are ignored (treated as "no filter").
     */
    @Query("SELECT a FROM Asset a WHERE " +
           "LOWER(a.type) = 'laptop' " +
           "AND a.status = com.assettrack.allocation.entity.AssetStatus.AVAILABLE " +
           "AND (:brand   IS NULL OR LOWER(a.brand)   = LOWER(:brand)) " +
           "AND (:ram     IS NULL OR a.ram             >= :ram) " +
           "AND (:storage IS NULL OR a.storage         >= :storage)")
    List<Asset> findSpareLaptops(
            @Param("brand")   String brand,
            @Param("ram")     Integer ram,
            @Param("storage") Integer storage
    );

    List<Asset> findByStatus(AssetStatus status);
}

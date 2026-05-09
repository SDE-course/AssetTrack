package com.assettrack.allocation.repository;

import com.assettrack.allocation.entity.Asset;
import com.assettrack.allocation.entity.AssetStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssetRepository extends JpaRepository<Asset, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Asset a WHERE a.id = :id")
    java.util.Optional<Asset> findByIdForUpdate(@Param("id") Long id);

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

    /**
     * Dynamic filter query. Any null parameter is ignored.
     * - assignedUser: matches active allocation's assignedTo name or email (partial, case-insensitive)
     */
    @Query("SELECT DISTINCT a FROM Asset a " +
           "LEFT JOIN a.allocations alloc " +
           "LEFT JOIN alloc.assignedTo u " +
           "WHERE (:serialNumber IS NULL OR LOWER(a.serialNumber) LIKE CONCAT('%', LOWER(:serialNumber), '%')) " +
           "AND (:status IS NULL OR a.status = :status) " +
           "AND (:type IS NULL OR LOWER(a.type) = LOWER(:type)) " +
           "AND (:brand IS NULL OR LOWER(a.brand) = LOWER(:brand)) " +
           "AND (:assignedUser IS NULL OR (alloc IS NOT NULL AND alloc.active = true AND (LOWER(u.name) LIKE CONCAT('%', LOWER(:assignedUser), '%') OR LOWER(u.email) LIKE CONCAT('%', LOWER(:assignedUser), '%'))))")
    List<Asset> findByFilters(
            @Param("serialNumber") String serialNumber,
            @Param("assignedUser") String assignedUser,
            @Param("status") AssetStatus status,
            @Param("type") String type,
            @Param("brand") String brand
    );
}

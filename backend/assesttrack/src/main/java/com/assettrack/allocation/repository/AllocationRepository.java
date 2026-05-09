package com.assettrack.allocation.repository;

import com.assettrack.allocation.entity.Allocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AllocationRepository extends JpaRepository<Allocation, Long> {

    /**
     * Find the single currently active allocation for an asset.
     * Business rule: only ONE active allocation can exist per asset at any time.
     */
    Optional<Allocation> findByAssetIdAndActiveTrue(Long assetId);

    /**
     * Check whether any active allocation exists for an asset.
     */
    boolean existsByAssetIdAndActiveTrue(Long assetId);

    /**
     * Full allocation history for an asset, newest first.
     */
    @Query("SELECT a FROM Allocation a " +
           "JOIN FETCH a.assignedTo " +
           "JOIN FETCH a.assignedBy " +
           "WHERE a.asset.id = :assetId " +
           "ORDER BY a.assignedDate DESC")
    List<Allocation> findAllByAssetIdOrderByAssignedDateDesc(@Param("assetId") Long assetId);

    /**
     * All active allocations for a specific user.
     */
    List<Allocation> findByAssignedToIdAndActiveTrue(Long userId);

    boolean existsByAssignedToIdAndActiveTrue(Long userId);
}

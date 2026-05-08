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

    /** Find the single active allocation for a given asset. */
    Optional<Allocation> findByAssetIdAndActiveTrue(Long assetId);

    /** Check whether an active allocation already exists for an asset. */
    boolean existsByAssetIdAndActiveTrue(Long assetId);

    /** Full history for an asset, newest first. */
    @Query("SELECT a FROM Allocation a " +
           "JOIN FETCH a.assignedTo " +
           "JOIN FETCH a.assignedBy " +
           "WHERE a.asset.id = :assetId " +
           "ORDER BY a.assignedDate DESC")
    List<Allocation> findAllByAssetIdOrderByAssignedDateDesc(@Param("assetId") Long assetId);

    /** All allocations currently assigned to a user. */
    List<Allocation> findByAssignedToIdAndActiveTrue(Long userId);
}

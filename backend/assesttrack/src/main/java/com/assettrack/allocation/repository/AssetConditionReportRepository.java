package com.assettrack.allocation.repository;

import com.assettrack.allocation.entity.AssetConditionReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssetConditionReportRepository extends JpaRepository<AssetConditionReport, Long> {

    // All reports for a specific asset
    List<AssetConditionReport> findByAssetId(Long assetId);

    // All reports filed by a specific user
    List<AssetConditionReport> findByReportedById(Long userId);

    // Paginated — used by admin/manager view
    Page<AssetConditionReport> findAllByOrderByReportedAtDesc(Pageable pageable);

    // Filter by status
    Page<AssetConditionReport> findByStatusOrderByReportedAtDesc(String status, Pageable pageable);
}

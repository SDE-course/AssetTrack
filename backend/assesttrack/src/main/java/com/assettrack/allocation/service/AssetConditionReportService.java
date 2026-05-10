package com.assettrack.allocation.service;

import com.assettrack.allocation.dto.AssetConditionReportDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AssetConditionReportService {

    /** User submits a condition report for one of their assets. */
    AssetConditionReportDTO.Response createReport(
            Long currentUserId,
            AssetConditionReportDTO.CreateRequest request);

    /** Admin / Manager: paginated list of all reports, newest first. */
    Page<AssetConditionReportDTO.Response> getAllReports(Pageable pageable);

    /** Admin / Manager: filter by status. */
    Page<AssetConditionReportDTO.Response> getReportsByStatus(String status, Pageable pageable);

    /** Admin / Manager: update status and add notes. */
    AssetConditionReportDTO.Response updateReport(
            Long reportId,
            AssetConditionReportDTO.UpdateRequest request);

    /** Reports filed by a specific user (for user's own view). */
    List<AssetConditionReportDTO.Response> getMyReports(Long currentUserId);
}

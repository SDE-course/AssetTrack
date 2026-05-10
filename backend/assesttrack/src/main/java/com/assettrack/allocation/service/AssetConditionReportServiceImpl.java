package com.assettrack.allocation.service;

import com.assettrack.allocation.dto.AssetConditionReportDTO;
import com.assettrack.allocation.entity.Asset;
import com.assettrack.allocation.entity.AssetConditionReport;
import com.assettrack.allocation.entity.User;
import com.assettrack.allocation.exception.ResourceNotFoundException;
import com.assettrack.allocation.repository.AssetConditionReportRepository;
import com.assettrack.allocation.repository.AssetRepository;
import com.assettrack.allocation.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AssetConditionReportServiceImpl implements AssetConditionReportService {

    private final AssetConditionReportRepository reportRepository;
    private final AssetRepository assetRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public AssetConditionReportDTO.Response createReport(Long currentUserId,
                                                          AssetConditionReportDTO.CreateRequest request) {
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + currentUserId));

        Asset asset = assetRepository.findById(request.getAssetId())
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found: " + request.getAssetId()));

        AssetConditionReport report = AssetConditionReport.builder()
                .asset(asset)
                .reportedBy(user)
                .issueType(request.getIssueType())
                .description(request.getDescription())
                .status("OPEN")
                .reportedAt(LocalDateTime.now())
                .build();

        return toResponse(reportRepository.save(report));
    }

    @Override
    public Page<AssetConditionReportDTO.Response> getAllReports(Pageable pageable) {
        return reportRepository.findAllByOrderByReportedAtDesc(pageable).map(this::toResponse);
    }

    @Override
    public Page<AssetConditionReportDTO.Response> getReportsByStatus(String status, Pageable pageable) {
        return reportRepository.findByStatusOrderByReportedAtDesc(status, pageable).map(this::toResponse);
    }

    @Override
    @Transactional
    public AssetConditionReportDTO.Response updateReport(Long reportId,
                                                          AssetConditionReportDTO.UpdateRequest request) {
        AssetConditionReport report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found: " + reportId));

        if (request.getStatus() != null) {
            report.setStatus(request.getStatus());
            if ("RESOLVED".equals(request.getStatus())) {
                report.setResolvedAt(LocalDateTime.now());
            }
        }
        if (request.getAdminNotes() != null) {
            report.setAdminNotes(request.getAdminNotes());
        }

        return toResponse(reportRepository.save(report));
    }

    @Override
    public List<AssetConditionReportDTO.Response> getMyReports(Long currentUserId) {
        return reportRepository.findByReportedById(currentUserId)
                .stream()
                .map(this::toResponse)
                .sorted((a, b) -> b.getReportedAt().compareTo(a.getReportedAt()))
                .collect(Collectors.toList());
    }

    // ─── Mapper ───────────────────────────────────────────────────────────────

    private AssetConditionReportDTO.Response toResponse(AssetConditionReport r) {
        return AssetConditionReportDTO.Response.builder()
                .id(r.getId())
                .assetId(r.getAsset().getId())
                .assetName(r.getAsset().getName())
                .assetSerial(r.getAsset().getSerialNumber())
                .reportedByName(r.getReportedBy().getName())
                .issueType(r.getIssueType())
                .description(r.getDescription())
                .status(r.getStatus())
                .adminNotes(r.getAdminNotes())
                .reportedAt(r.getReportedAt())
                .resolvedAt(r.getResolvedAt())
                .build();
    }
}

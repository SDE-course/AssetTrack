package com.assettrack.allocation.controller;

import com.assettrack.allocation.dto.AssetConditionReportDTO;
import com.assettrack.allocation.entity.User;
import com.assettrack.allocation.service.AssetConditionReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/condition-reports")
@RequiredArgsConstructor
public class AssetConditionReportController {

    private final AssetConditionReportService conditionReportService;

    /**
     * POST /api/condition-reports
     * Any authenticated user can report a condition issue for an asset.
     */
    @PostMapping
    public ResponseEntity<AssetConditionReportDTO.Response> createReport(
            Authentication auth,
            @RequestBody AssetConditionReportDTO.CreateRequest request) {

        User user = (User) auth.getPrincipal();
        AssetConditionReportDTO.Response response =
                conditionReportService.createReport(user.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /api/condition-reports
     * ADMIN / MANAGER only: paginated list of all reports.
     * Optional ?status=OPEN|IN_PROGRESS|RESOLVED filter.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Page<AssetConditionReportDTO.Response>> getAllReports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status) {

        Pageable pageable = PageRequest.of(page, size);
        Page<AssetConditionReportDTO.Response> result = (status != null && !status.isBlank())
                ? conditionReportService.getReportsByStatus(status.toUpperCase(), pageable)
                : conditionReportService.getAllReports(pageable);
        return ResponseEntity.ok(result);
    }

    /**
     * PUT /api/condition-reports/{id}
     * ADMIN / MANAGER only: update status and/or admin notes.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<AssetConditionReportDTO.Response> updateReport(
            @PathVariable Long id,
            @RequestBody AssetConditionReportDTO.UpdateRequest request) {

        return ResponseEntity.ok(conditionReportService.updateReport(id, request));
    }

    /**
     * GET /api/condition-reports/my
     * Authenticated user: returns their own submitted reports.
     */
    @GetMapping("/my")
    public ResponseEntity<List<AssetConditionReportDTO.Response>> getMyReports(Authentication auth) {
        User user = (User) auth.getPrincipal();
        return ResponseEntity.ok(conditionReportService.getMyReports(user.getId()));
    }
}

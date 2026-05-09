package com.assettrack.allocation.controller;

import com.assettrack.allocation.dto.*;
import com.assettrack.allocation.service.AllocationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

/**
 * REST controller for all Allocation operations.
 *
 * Role matrix:
 *  ADMIN     → full access
 *  MANAGER   → assign / return / transfer / read history
 *  DEVELOPER → read only (history, current owner, spare laptops)
 */
@RestController
@RequestMapping("/api/allocations")
@RequiredArgsConstructor
public class AllocationController {

    private final AllocationService allocationService;

    // ── 1. Assign ─────────────────────────────────────────────────────────────
    /**
     * POST /api/allocations/assign
     * Body: { "assetId": 1, "userId": 3, "notes": "..." }
     */
    @PostMapping("/assign")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<MessageResponse> assignAsset(
            @Valid @RequestBody AssignAssetRequest request,
            Principal principal) {

        MessageResponse response = allocationService
                .assignAsset(request, principal.getName());
        return ResponseEntity.ok(response);
    }

    // ── 2. Return ─────────────────────────────────────────────────────────────
    /**
     * POST /api/allocations/{id}/return
     */
    @PostMapping("/{id}/return")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<MessageResponse> returnAsset(@PathVariable Long id) {
        return ResponseEntity.ok(allocationService.returnAsset(id));
    }

    // ── 3. Transfer ───────────────────────────────────────────────────────────
    /**
     * POST /api/allocations/transfer
     * Body: { "allocationId": 1, "newUserId": 5, "notes": "..." }
     */
    @PostMapping("/transfer")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<MessageResponse> transferAsset(
            @Valid @RequestBody TransferAssetRequest request,
            Principal principal) {

        return ResponseEntity.ok(
                allocationService.transferAsset(request, principal.getName()));
    }

    // ── 4. History ────────────────────────────────────────────────────────────
    /**
     * GET /api/allocations/asset/{assetId}/history
     */
    @GetMapping("/asset/{assetId}/history")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'DEVELOPER')")
    public ResponseEntity<List<AllocationHistoryResponse>> getHistory(
            @PathVariable Long assetId) {

        return ResponseEntity.ok(allocationService.getAllocationHistory(assetId));
    }

    // ── 5. Current owner ──────────────────────────────────────────────────────
    /**
     * GET /api/allocations/current-owner/{assetId}
     */
    @GetMapping("/current-owner/{assetId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'DEVELOPER')")
    public ResponseEntity<Object> getCurrentOwner(@PathVariable Long assetId) {
        return ResponseEntity.ok(allocationService.getCurrentOwner(assetId));
    }

    // ── 6. Spare laptops ──────────────────────────────────────────────────────
    /**
     * GET /api/allocations/spare-laptops?brand=Dell&ram=16&storage=512
     */
    @GetMapping("/spare-laptops")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'DEVELOPER')")
    public ResponseEntity<List<AssetResponse>> getSpareLaptops(
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) Integer ram,
            @RequestParam(required = false) Integer storage) {

        return ResponseEntity.ok(allocationService.getSpareLaptops(brand, ram, storage));
    }
}

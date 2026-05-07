package com.assettrack.allocation.controller;

import com.assettrack.allocation.dto.*;
import com.assettrack.allocation.service.AllocationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/allocations")
@RequiredArgsConstructor
public class AllocationController {

    private final AllocationService allocationService;

    // ── Assign ──────────────────────────────────────────────────────────────

    @PostMapping("/assign")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<MessageResponse> assignAsset(
            @Valid @RequestBody AssignAssetRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long currentUserId = extractUserId(userDetails);
        return ResponseEntity.ok(allocationService.assignAsset(request, currentUserId));
    }

    // ── Return ──────────────────────────────────────────────────────────────

    @PostMapping("/{id}/return")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<MessageResponse> returnAsset(@PathVariable Long id) {
        return ResponseEntity.ok(allocationService.returnAsset(id));
    }

    // ── Transfer ─────────────────────────────────────────────────────────────

    @PostMapping("/transfer")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<MessageResponse> transferAsset(
            @Valid @RequestBody TransferAssetRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long currentUserId = extractUserId(userDetails);
        return ResponseEntity.ok(allocationService.transferAsset(request, currentUserId));
    }

    // ── History ──────────────────────────────────────────────────────────────

    @GetMapping("/asset/{assetId}/history")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'DEVELOPER')")
    public ResponseEntity<List<AllocationHistoryResponse>> getHistory(
            @PathVariable Long assetId) {
        return ResponseEntity.ok(allocationService.getAllocationHistory(assetId));
    }

    // ── Current Owner ────────────────────────────────────────────────────────

    @GetMapping("/current-owner/{assetId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'DEVELOPER')")
    public ResponseEntity<Object> getCurrentOwner(@PathVariable Long assetId) {
        return ResponseEntity.ok(allocationService.getCurrentOwner(assetId));
    }

    // ── Spare Laptops ────────────────────────────────────────────────────────

    @GetMapping("/spare-laptops")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'DEVELOPER')")
    public ResponseEntity<List<SpareLaptopResponse>> getSpareLaptops(
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) Integer ram,
            @RequestParam(required = false) Integer storage) {
        return ResponseEntity.ok(allocationService.getSpareLaptops(brand, ram, storage));
    }

    // ── Helper ───────────────────────────────────────────────────────────────

    /**
     * Extracts the numeric user ID from the JWT principal.
     * Assumes the username stored in UserDetails IS the user's database ID.
     * Adjust if your JWT stores the ID differently (e.g., in a custom claim).
     */
    private Long extractUserId(UserDetails userDetails) {
        return Long.parseLong(userDetails.getUsername());
    }
}

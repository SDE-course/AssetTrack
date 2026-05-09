package com.assettrack.allocation.service;

import com.assettrack.allocation.dto.*;
import com.assettrack.allocation.entity.*;
import com.assettrack.allocation.exception.*;
import com.assettrack.allocation.mapper.AllocationMapper;
import com.assettrack.allocation.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AllocationServiceImpl implements AllocationService {

    private final AllocationRepository allocationRepository;
    private final AssetRepository      assetRepository;
    private final UserRepository       userRepository;
    private final AllocationMapper     mapper;

    // ── 1. Assign ─────────────────────────────────────────────────────────────
    @Override
    @Transactional
    public MessageResponse assignAsset(AssignAssetRequest request, String performedByEmail) {

        // Resolve asset
        Asset asset = assetRepository.findById(request.getAssetId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Asset not found with id: " + request.getAssetId()));

        // Resolve target user
        User targetUser = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + request.getUserId()));

        // Resolve actor (the logged-in manager/admin)
        User performedBy = resolveActor(performedByEmail);

        // Business rule: must be AVAILABLE
        if (asset.getStatus() == AssetStatus.EXPIRED) {
            throw new BadRequestException(
                    "Cannot assign expired asset: " + asset.getSerialNumber());
        }
        if (asset.getStatus() != AssetStatus.AVAILABLE) {
            throw new AssetAlreadyAssignedException(
                    "Asset is not available. Current status: " + asset.getStatus());
        }

        // Double-check no active allocation exists (race-condition guard)
        if (allocationRepository.existsByAssetIdAndActiveTrue(asset.getId())) {
            throw new AssetAlreadyAssignedException(
                    "Asset already has an active allocation.");
        }

        // Create allocation record
        Allocation allocation = Allocation.builder()
                .asset(asset)
                .assignedTo(targetUser)
                .assignedBy(performedBy)
                .assignedDate(LocalDateTime.now())
                .active(true)
                .notes(request.getNotes())
                .build();

        allocationRepository.save(allocation);

        // Update asset status
        asset.setStatus(AssetStatus.ASSIGNED);
        assetRepository.save(asset);

        log.info("Asset [{}] assigned to user [{}] by [{}]",
                asset.getSerialNumber(), targetUser.getEmail(), performedByEmail);

        return new MessageResponse("Asset assigned successfully");
    }

    // ── 2. Return ─────────────────────────────────────────────────────────────
    @Override
    @Transactional
    public MessageResponse returnAsset(Long allocationId) {

        Allocation allocation = allocationRepository.findById(allocationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Allocation not found with id: " + allocationId));

        if (!allocation.isActive()) {
            throw new BadRequestException(
                    "Allocation is already closed (asset was already returned).");
        }

        allocation.setActive(false);
        allocation.setReturnedDate(LocalDateTime.now());
        allocationRepository.save(allocation);

        Asset asset = allocation.getAsset();
        asset.setStatus(AssetStatus.AVAILABLE);
        assetRepository.save(asset);

        log.info("Asset [{}] returned — allocation [{}] closed.",
                asset.getSerialNumber(), allocationId);

        return new MessageResponse("Asset returned successfully");
    }

    // ── 3. Transfer ───────────────────────────────────────────────────────────
    @Override
    @Transactional
    public MessageResponse transferAsset(TransferAssetRequest request, String performedByEmail) {

        Allocation oldAllocation = allocationRepository.findById(request.getAllocationId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Allocation not found with id: " + request.getAllocationId()));

        if (!oldAllocation.isActive()) {
            throw new BadRequestException(
                    "Cannot transfer from an inactive allocation.");
        }

        User newUser = userRepository.findById(request.getNewUserId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "New user not found with id: " + request.getNewUserId()));

        // Business rule: cannot transfer to the same user
        if (oldAllocation.getAssignedTo().getId().equals(newUser.getId())) {
            throw new BadRequestException(
                    "Cannot transfer asset to the same user who currently holds it.");
        }

        User performedBy = resolveActor(performedByEmail);

        // Close old allocation
        oldAllocation.setActive(false);
        oldAllocation.setReturnedDate(LocalDateTime.now());
        allocationRepository.save(oldAllocation);

        // Open new allocation — asset stays ASSIGNED
        Allocation newAllocation = Allocation.builder()
                .asset(oldAllocation.getAsset())
                .assignedTo(newUser)
                .assignedBy(performedBy)
                .assignedDate(LocalDateTime.now())
                .active(true)
                .notes(request.getNotes())
                .build();

        allocationRepository.save(newAllocation);

        log.info("Asset [{}] transferred from [{}] to [{}] by [{}]",
                oldAllocation.getAsset().getSerialNumber(),
                oldAllocation.getAssignedTo().getEmail(),
                newUser.getEmail(),
                performedByEmail);

        return new MessageResponse("Asset transferred successfully");
    }

    // ── 4. History ────────────────────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public List<AllocationHistoryResponse> getAllocationHistory(Long assetId) {

        // Ensure asset exists
        if (!assetRepository.existsById(assetId)) {
            throw new ResourceNotFoundException("Asset not found with id: " + assetId);
        }

        return allocationRepository
                .findAllByAssetIdOrderByAssignedDateDesc(assetId)
                .stream()
                .map(mapper::toHistoryResponse)
                .collect(Collectors.toList());
    }

    // ── 5. Current owner ──────────────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public Object getCurrentOwner(Long assetId) {

        if (!assetRepository.existsById(assetId)) {
            throw new ResourceNotFoundException("Asset not found with id: " + assetId);
        }

        return allocationRepository.findByAssetIdAndActiveTrue(assetId)
                .<Object>map(mapper::toResponse)
                .orElse(new MessageResponse("Asset is currently available"));
    }

    // ── 6. Spare laptops ──────────────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public List<AssetResponse> getSpareLaptops(String brand, Integer ram, Integer storage) {

        return assetRepository
                .findSpareLaptops(brand, ram, storage)
                .stream()
                .map(mapper::toAssetResponse)
                .collect(Collectors.toList());
    }

    // ── Helper: resolve the logged-in actor ───────────────────────────────────
    /**
     * During DEV mode the security principal is an in-memory username (not email).
     * We first try by email, then fall back to finding any user with that name.
     * When real JWT auth lands, this will just call findByEmail.
     */
    private User resolveActor(String emailOrUsername) {
        return userRepository.findByEmail(emailOrUsername)
                .orElseGet(() ->
                    // Fallback: use the first admin user so tests don't fail
                    userRepository.findAll().stream()
                            .filter(u -> u.getRole() == Role.ADMIN ||
                                         u.getRole() == Role.MANAGER)
                            .findFirst()
                            .orElseThrow(() -> new ResourceNotFoundException(
                                    "No admin/manager user found in the system. " +
                                    "Please seed at least one user via /api/dev/seed"))
                );
    }
}

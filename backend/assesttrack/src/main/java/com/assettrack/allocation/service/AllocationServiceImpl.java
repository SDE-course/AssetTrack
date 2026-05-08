package com.assettrack.allocation.service;

import com.assettrack.allocation.dto.*;
import com.assettrack.allocation.entity.Allocation;
import com.assettrack.allocation.entity.Asset;
import com.assettrack.allocation.entity.AssetStatus;
import com.assettrack.allocation.entity.User;
import com.assettrack.allocation.exception.AssetAlreadyAssignedException;
import com.assettrack.allocation.exception.BadRequestException;
import com.assettrack.allocation.exception.ResourceNotFoundException;
import com.assettrack.allocation.mapper.AllocationMapper;
import com.assettrack.allocation.repository.AllocationRepository;
import com.assettrack.allocation.repository.AssetRepository;
import com.assettrack.allocation.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AllocationServiceImpl implements AllocationService {

    private final AllocationRepository allocationRepository;
    private final AssetRepository assetRepository;
    private final UserRepository userRepository;
    private final AllocationMapper mapper;

    // ─────────────────────────────────────────────────────────────────────────
    // 1. Assign Asset
    // ─────────────────────────────────────────────────────────────────────────
    @Override
    @Transactional
    public MessageResponse assignAsset(AssignAssetRequest request, Long assignedByUserId) {

        Asset asset = assetRepository.findById(request.getAssetId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Asset not found with id: " + request.getAssetId()));

        User assignedTo = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + request.getUserId()));

        User assignedBy = userRepository.findById(assignedByUserId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Performing user not found with id: " + assignedByUserId));

        if (asset.getStatus() == AssetStatus.EXPIRED) {
            throw new BadRequestException("Expired assets cannot be assigned.");
        }

        if (asset.getStatus() != AssetStatus.AVAILABLE) {
            throw new BadRequestException(
                    "Asset is not available. Current status: " + asset.getStatus());
        }

        if (allocationRepository.existsByAssetIdAndActiveTrue(asset.getId())) {
            throw new AssetAlreadyAssignedException(
                    "Asset already has an active allocation.");
        }

        Allocation allocation = Allocation.builder()
                .asset(asset)
                .assignedTo(assignedTo)
                .assignedBy(assignedBy)
                .assignedDate(LocalDateTime.now())
                .active(true)
                .notes(request.getNotes())
                .build();

        asset.setStatus(AssetStatus.ASSIGNED);
        assetRepository.save(asset);
        allocationRepository.save(allocation);

        return new MessageResponse("Asset assigned successfully");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 2. Return Asset
    // ─────────────────────────────────────────────────────────────────────────
    @Override
    @Transactional
    public MessageResponse returnAsset(Long allocationId) {

        Allocation allocation = allocationRepository.findById(allocationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Allocation not found with id: " + allocationId));

        if (!allocation.isActive()) {
            throw new BadRequestException("Allocation is already closed.");
        }

        allocation.setActive(false);
        allocation.setReturnedDate(LocalDateTime.now());
        allocation.getAsset().setStatus(AssetStatus.AVAILABLE);

        assetRepository.save(allocation.getAsset());
        allocationRepository.save(allocation);

        return new MessageResponse("Asset returned successfully");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 3. Transfer Asset
    // ─────────────────────────────────────────────────────────────────────────
    @Override
    @Transactional
    public MessageResponse transferAsset(TransferAssetRequest request, Long transferredByUserId) {

        Allocation oldAllocation = allocationRepository.findById(request.getAllocationId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Allocation not found with id: " + request.getAllocationId()));

        if (!oldAllocation.isActive()) {
            throw new BadRequestException("Cannot transfer — allocation is not active.");
        }

        User newUser = userRepository.findById(request.getNewUserId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Target user not found with id: " + request.getNewUserId()));

        if (oldAllocation.getAssignedTo().getId().equals(request.getNewUserId())) {
            throw new BadRequestException("Transfer target is the current holder of the asset.");
        }

        User transferredBy = userRepository.findById(transferredByUserId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Performing user not found with id: " + transferredByUserId));

        // Close old allocation
        oldAllocation.setActive(false);
        oldAllocation.setReturnedDate(LocalDateTime.now());
        allocationRepository.save(oldAllocation);

        // Open new allocation (asset stays ASSIGNED)
        Allocation newAllocation = Allocation.builder()
                .asset(oldAllocation.getAsset())
                .assignedTo(newUser)
                .assignedBy(transferredBy)
                .assignedDate(LocalDateTime.now())
                .active(true)
                .notes(request.getNotes())
                .build();

        allocationRepository.save(newAllocation);

        return new MessageResponse("Asset transferred successfully");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 4. Allocation History
    // ─────────────────────────────────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public List<AllocationHistoryResponse> getAllocationHistory(Long assetId) {

        if (!assetRepository.existsById(assetId)) {
            throw new ResourceNotFoundException("Asset not found with id: " + assetId);
        }

        return allocationRepository
                .findAllByAssetIdOrderByAssignedDateDesc(assetId)
                .stream()
                .map(mapper::toHistoryResponse)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 5. Current Owner
    // ─────────────────────────────────────────────────────────────────────────
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

    // ─────────────────────────────────────────────────────────────────────────
    // 6. Spare Laptops
    // ─────────────────────────────────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public List<SpareLaptopResponse> getSpareLaptops(String brand, Integer ram, Integer storage) {

        return assetRepository
                .findSpareLaptops(AssetStatus.AVAILABLE, brand, ram, storage)
                .stream()
                .map(mapper::toSpareLaptopResponse)
                .collect(Collectors.toList());
    }
}

package com.assettrack.allocation.service;

import com.assettrack.allocation.dto.AssetResponse;
import com.assettrack.allocation.dto.CreateAssetRequest;
import com.assettrack.allocation.dto.UpdateAssetRequest;
import com.assettrack.allocation.entity.Asset;
import com.assettrack.allocation.entity.AssetStatus;
import com.assettrack.allocation.entity.AssetType;
import com.assettrack.allocation.exception.BadRequestException;
import com.assettrack.allocation.exception.ResourceNotFoundException;
import com.assettrack.allocation.repository.AssetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AssetServiceImpl implements AssetService {

    private final AssetRepository assetRepository;
    private final com.assettrack.allocation.repository.AllocationRepository allocationRepository;

    @Override
    @Transactional
    public AssetResponse createAsset(CreateAssetRequest request) {
        verifyUniqueSerialNumber(request.getSerialNumber(), null);

        Asset asset = Asset.builder()
            .type(parseType(request.getType()))
                .brand(request.getBrand())
                .name(request.getName())
                .serialNumber(request.getSerialNumber())
                .purchaseDate(request.getPurchaseDate())
                .warrantyExpiryDate(request.getWarrantyExpiryDate())
                .status(AssetStatus.AVAILABLE)
                .build();

        return toResponse(assetRepository.save(asset));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssetResponse> getAllAssets() {
        return assetRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public AssetResponse getAssetById(Long id) {
        Asset asset = assetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found with id: " + id));
        return toResponse(asset);
    }

    @Override
    @Transactional
    public AssetResponse updateAsset(Long id, UpdateAssetRequest request) {
        Asset asset = assetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found with id: " + id));

        if (request.getSerialNumber() != null && !request.getSerialNumber().isBlank()) {
            verifyUniqueSerialNumber(request.getSerialNumber(), id);
            asset.setSerialNumber(request.getSerialNumber());
        }
        if (request.getType() != null && !request.getType().isBlank()) {
            asset.setType(parseType(request.getType()));
        }
        if (request.getBrand() != null && !request.getBrand().isBlank()) {
            asset.setBrand(request.getBrand());
        }
        if (request.getModel() != null && !request.getModel().isBlank()) {
            asset.setName(request.getModel());
        }
        if (request.getStatus() != null) {
            asset.setStatus(request.getStatus());
        }
        if (request.getPurchaseDate() != null) {
            asset.setPurchaseDate(request.getPurchaseDate());
        }
        if (request.getWarrantyExpiryDate() != null) {
            asset.setWarrantyExpiryDate(request.getWarrantyExpiryDate());
        }

        return toResponse(assetRepository.save(asset));
    }

    @Override
    @Transactional
    public void deleteAsset(Long id) {
        if (!assetRepository.existsById(id)) {
            throw new ResourceNotFoundException("Asset not found with id: " + id);
        }
        assetRepository.deleteById(id);
    }

    private AssetResponse toResponse(Asset asset) {
        AssetResponse.AssetResponseBuilder b = AssetResponse.builder()
                .id(asset.getId())
                .name(asset.getName())
                .serialNumber(asset.getSerialNumber())
                .brand(asset.getBrand())
            .type(asset.getType() != null ? asset.getType().name() : null)
                .status(asset.getStatus() != null ? asset.getStatus().name() : null)
                .ram(asset.getRam())
                .storage(asset.getStorage())
                .purchaseDate(asset.getPurchaseDate())
                .warrantyExpiryDate(asset.getWarrantyExpiryDate());

        // Attach last allocation (most recent) if available
        try {
            java.util.List<com.assettrack.allocation.entity.Allocation> history = allocationRepository.findAllByAssetIdOrderByAssignedDateDesc(asset.getId());
            if (history != null && !history.isEmpty()) {
                com.assettrack.allocation.entity.Allocation last = history.get(0);
                b.lastAssignedTo(last.getAssignedTo() != null ? last.getAssignedTo().getName() : null);
                b.lastAssignedDate(last.getAssignedDate() != null ? last.getAssignedDate().toLocalDate() : null);
            }
        } catch (Exception ignored) {
        }

        return b.build();
    }

    private AssetType parseType(String type) {
        if (type == null || type.isBlank()) return null;
        try {
            return AssetType.valueOf(type.trim().toUpperCase());
        } catch (Exception e) {
            throw new BadRequestException("Invalid asset type: " + type);
        }
    }

    private void verifyUniqueSerialNumber(String serialNumber, Long existingAssetId) {
        if (serialNumber == null || serialNumber.isBlank()) {
            return;
        }

        boolean duplicate = assetRepository.findAll().stream()
                .filter(a -> a.getSerialNumber() != null)
                .filter(a -> a.getSerialNumber().equals(serialNumber))
                .anyMatch(a -> existingAssetId == null || !a.getId().equals(existingAssetId));

        if (duplicate) {
            throw new BadRequestException("Asset serial number must be unique");
        }
    }
}

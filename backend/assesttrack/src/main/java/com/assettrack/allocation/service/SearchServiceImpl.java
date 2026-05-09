package com.assettrack.allocation.service;

import com.assettrack.allocation.dto.AssetResponse;
import com.assettrack.allocation.dto.SearchFilterDTO;
import com.assettrack.allocation.entity.Allocation;
import com.assettrack.allocation.entity.Asset;
import com.assettrack.allocation.entity.AssetStatus;
import com.assettrack.allocation.repository.AllocationRepository;
import com.assettrack.allocation.repository.AssetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final AssetRepository assetRepository;
    private final AllocationRepository allocationRepository;

    @Override
    @Transactional(readOnly = true)
    public List<AssetResponse> searchAssets(SearchFilterDTO filter) {
        List<Asset> assets = assetRepository.findByFilters(
                emptyToNull(filter.getSerialNumber()),
                emptyToNull(filter.getAssignedUser()),
                filter.getStatus(),
                emptyToNull(filter.getType()),
                emptyToNull(filter.getBrand())
        );

        return assets.stream().map(this::toResponseWithLastOwner).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssetResponse> findSpareLaptops(String brand, Integer minRam, Integer minStorage) {
        List<Asset> assets = assetRepository.findSpareLaptops(
                emptyToNull(brand), minRam, minStorage
        );
        return assets.stream().map(this::toResponseWithLastOwner).collect(Collectors.toList());
    }

    private String emptyToNull(String s) {
        return s == null || s.isBlank() ? null : s;
    }

    private AssetResponse toResponseWithLastOwner(Asset asset) {
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

        List<Allocation> history = allocationRepository.findAllByAssetIdOrderByAssignedDateDesc(asset.getId());
        if (history != null && !history.isEmpty()) {
            Allocation last = history.get(0);
            b.lastAssignedTo(last.getAssignedTo() != null ? last.getAssignedTo().getName() : null);
            b.lastAssignedDate(last.getAssignedDate() != null ? last.getAssignedDate().toLocalDate() : null);
        }

        return b.build();
    }
}

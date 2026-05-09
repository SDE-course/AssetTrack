package com.assettrack.allocation.mapper;

import com.assettrack.allocation.dto.AllocationHistoryResponse;
import com.assettrack.allocation.dto.AllocationResponse;
import com.assettrack.allocation.dto.AssetResponse;
import com.assettrack.allocation.entity.Allocation;
import com.assettrack.allocation.entity.Asset;
import org.springframework.stereotype.Component;

@Component
public class AllocationMapper {

    public AllocationResponse toResponse(Allocation a) {
        return AllocationResponse.builder()
                .id(a.getId())
                .assetId(a.getAsset().getId())
                .assetName(a.getAsset().getName())
                .serialNumber(a.getAsset().getSerialNumber())
                .assignedToId(a.getAssignedTo().getId())
                .assignedToName(a.getAssignedTo().getName())
                .assignedById(a.getAssignedBy().getId())
                .assignedByName(a.getAssignedBy().getName())
                .assignedDate(a.getAssignedDate())
                .returnedDate(a.getReturnedDate())
                .active(a.isActive())
                .notes(a.getNotes())
                .build();
    }

    public AllocationHistoryResponse toHistoryResponse(Allocation a) {
        return AllocationHistoryResponse.builder()
                .allocationId(a.getId())
                .user(a.getAssignedTo().getName())
                .assignedBy(a.getAssignedBy().getName())
                .assignedDate(a.getAssignedDate())
                .returnedDate(a.getReturnedDate())
                .active(a.isActive())
                .notes(a.getNotes())
                .build();
    }

    public AssetResponse toAssetResponse(Asset asset) {
        return AssetResponse.builder()
                .id(asset.getId())
                .name(asset.getName())
                .serialNumber(asset.getSerialNumber())
                .brand(asset.getBrand())
                .type(asset.getType())
                .status(asset.getStatus().name())
                .ram(asset.getRam())
                .storage(asset.getStorage())
                .build();
    }
}

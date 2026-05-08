package com.assettrack.allocation.service;

import com.assettrack.allocation.dto.*;

import java.util.List;

public interface AllocationService {

    MessageResponse assignAsset(AssignAssetRequest request, Long assignedByUserId);

    MessageResponse returnAsset(Long allocationId);

    MessageResponse transferAsset(TransferAssetRequest request, Long transferredByUserId);

    List<AllocationHistoryResponse> getAllocationHistory(Long assetId);

    Object getCurrentOwner(Long assetId);

    List<SpareLaptopResponse> getSpareLaptops(String brand, Integer ram, Integer storage);
}

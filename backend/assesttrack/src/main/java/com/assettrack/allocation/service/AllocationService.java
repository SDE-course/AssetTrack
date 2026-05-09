package com.assettrack.allocation.service;

import com.assettrack.allocation.dto.*;

import java.util.List;

public interface AllocationService {

    /**
     * Assign an available asset to a user.
     */
    MessageResponse assignAsset(AssignAssetRequest request, String performedByEmail);

    /**
     * Return an active allocation — marks it inactive and frees the asset.
     */
    MessageResponse returnAsset(Long allocationId);

    /**
     * Transfer asset from current holder to a new user atomically.
     */
    MessageResponse transferAsset(TransferAssetRequest request, String performedByEmail);

    /**
     * Full chronological allocation history for a single asset.
     */
    List<AllocationHistoryResponse> getAllocationHistory(Long assetId);

    /**
     * Returns the current active allocation for an asset, or a message if available.
     */
    Object getCurrentOwner(Long assetId);

    /**
     * Returns available laptops, with optional brand / RAM / storage filters.
     */
    List<AssetResponse> getSpareLaptops(String brand, Integer ram, Integer storage);
}

package com.assettrack.allocation.service;

import com.assettrack.allocation.dto.AssetResponse;
import com.assettrack.allocation.dto.CreateAssetRequest;
import com.assettrack.allocation.dto.UpdateAssetRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AssetService {

    AssetResponse createAsset(CreateAssetRequest request);

    List<AssetResponse> getAllAssets();

    Page<AssetResponse> getAllAssetsPaginated(Pageable pageable);

    AssetResponse getAssetById(Long id);

    AssetResponse updateAsset(Long id, UpdateAssetRequest request);

    void deleteAsset(Long id);
}

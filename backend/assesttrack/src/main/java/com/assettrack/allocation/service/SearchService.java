package com.assettrack.allocation.service;

import com.assettrack.allocation.dto.AssetResponse;
import com.assettrack.allocation.dto.SearchFilterDTO;

import java.util.List;

public interface SearchService {
	List<AssetResponse> searchAssets(SearchFilterDTO filter);

	/**
	 * Find an available spare laptop (type=laptop and status=AVAILABLE) optionally filtered
	 * by brand/ram/storage. Returns details including last owner if any.
	 */
	List<AssetResponse> findSpareLaptops(String brand, Integer minRam, Integer minStorage);
}

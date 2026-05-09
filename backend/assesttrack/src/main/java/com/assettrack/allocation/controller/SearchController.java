package com.assettrack.allocation.controller;

import com.assettrack.allocation.dto.AssetResponse;
import com.assettrack.allocation.dto.SearchFilterDTO;
import com.assettrack.allocation.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

	private final SearchService searchService;

	@PostMapping
	public ResponseEntity<List<AssetResponse>> search(@RequestBody SearchFilterDTO filter) {
		return ResponseEntity.ok(searchService.searchAssets(filter));
	}

	@GetMapping("/spare-laptops")
	public ResponseEntity<List<AssetResponse>> spareLaptops(
			@RequestParam(required = false) String brand,
			@RequestParam(required = false) Integer minRam,
			@RequestParam(required = false) Integer minStorage
	) {
		return ResponseEntity.ok(searchService.findSpareLaptops(brand, minRam, minStorage));
	}
}

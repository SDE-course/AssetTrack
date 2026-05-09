package com.assettrack.allocation.controller;

import com.assettrack.allocation.dto.UsageStatisticsDTO;
import com.assettrack.allocation.service.ReportsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportsController {

	private final ReportsService reportsService;

	@GetMapping("/usage-statistics")
	public ResponseEntity<UsageStatisticsDTO> getUsageStatistics() {
		return ResponseEntity.ok(reportsService.getUsageStatistics());
	}
}

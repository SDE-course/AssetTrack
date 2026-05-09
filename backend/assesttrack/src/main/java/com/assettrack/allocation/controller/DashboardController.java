package com.assettrack.allocation.controller;

import com.assettrack.allocation.dto.DashboardStatsDTO;
import com.assettrack.allocation.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

	private final DashboardService dashboardService;

	@GetMapping
	public ResponseEntity<DashboardStatsDTO> getDashboardStats() {
		return ResponseEntity.ok(dashboardService.getDashboardStats());
	}
}

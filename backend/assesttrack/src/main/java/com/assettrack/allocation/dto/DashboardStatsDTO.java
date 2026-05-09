package com.assettrack.allocation.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class DashboardStatsDTO {
	private Long totalAssets;
	private Long availableAssets;
	private Long assignedAssets;
	private Map<String, Long> typeCounts;
	private Map<String, Long> statusCounts;
	private Map<String, Long> assignedUserCounts;
}

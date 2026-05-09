package com.assettrack.allocation.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class UsageStatisticsDTO {
	private long totalAssets;
	private long totalAllocations;
	private long activeAllocations;
	private double averageAllocationDurationDays;
	private Map<String, Long> allocationsByType;
	private Map<String, Long> allocationsByUser;
	private List<AssetUsageDTO> topUsedAssets;
	private List<UserAllocationDTO> userAllocationStats;
	private List<AllocationHistoryDTO> recentAllocations;
}

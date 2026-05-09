package com.assettrack.allocation.service;

import com.assettrack.allocation.dto.AllocationHistoryDTO;
import com.assettrack.allocation.dto.AssetUsageDTO;
import com.assettrack.allocation.dto.UsageStatisticsDTO;
import com.assettrack.allocation.dto.UserAllocationDTO;
import com.assettrack.allocation.entity.Allocation;
import com.assettrack.allocation.entity.Asset;
import com.assettrack.allocation.repository.AllocationRepository;
import com.assettrack.allocation.repository.AssetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportsServiceImpl implements ReportsService {

	private final AssetRepository assetRepository;
	private final AllocationRepository allocationRepository;

	@Override
	public UsageStatisticsDTO getUsageStatistics() {
		List<Asset> allAssets = assetRepository.findAll();
		List<Allocation> allAllocations = allocationRepository.findAll();

		// Total stats
		long totalAssets = allAssets.size();
		long totalAllocations = allAllocations.size();
		long activeAllocations = allAllocations.stream().filter(Allocation::isActive).count();

		// Average allocation duration
		double avgDurationDays = allAllocations.stream()
				.filter(a -> a.getReturnedDate() != null)
				.mapToLong(a -> ChronoUnit.DAYS.between(a.getAssignedDate(), a.getReturnedDate()))
				.average()
				.orElse(0.0);

		// Allocations by asset type
		Map<String, Long> allocationsByType = allAssets.stream()
				.collect(Collectors.groupingBy(
						asset -> asset.getType() != null ? asset.getType() : "Unknown",
						Collectors.counting()
				));

		// Allocations by user (assigned to)
		Map<String, Long> allocationsByUser = allAllocations.stream()
				.filter(a -> a.getAssignedTo() != null)
				.collect(Collectors.groupingBy(
						a -> a.getAssignedTo().getName() != null ? a.getAssignedTo().getName() : "Unknown",
						Collectors.counting()
				));

		// Top used assets (by allocation count)
		Map<Long, Long> assetAllocationCount = allAllocations.stream()
				.collect(Collectors.groupingBy(a -> a.getAsset().getId(), Collectors.counting()));

		List<AssetUsageDTO> topUsedAssets = allAssets.stream()
				.map(asset -> AssetUsageDTO.builder()
						.assetTag(asset.getSerialNumber())
						.assetName(asset.getName())
						.assetType(asset.getType())
						.allocationCount(assetAllocationCount.getOrDefault(asset.getId(), 0L))
						.currentStatus(asset.getStatus() != null ? asset.getStatus().toString() : "Unknown")
						.build())
				.sorted((a, b) -> Long.compare(b.getAllocationCount(), a.getAllocationCount()))
				.limit(10)
				.collect(Collectors.toList());

		// User allocation stats
		List<UserAllocationDTO> userAllocationStats = allocationsByUser.entrySet().stream()
				.map(entry -> {
					String userName = entry.getKey();
					long total = entry.getValue();
					long active = allAllocations.stream()
							.filter(a -> a.isActive() && a.getAssignedTo().getName().equals(userName))
							.count();

					return UserAllocationDTO.builder()
							.userName(userName)
							.allocationCount(total)
							.activeAllocations(active)
							.build();
				})
				.sorted((a, b) -> Long.compare(b.getActiveAllocations(), a.getActiveAllocations()))
				.limit(10)
				.collect(Collectors.toList());

		// Recent allocations
		List<AllocationHistoryDTO> recentAllocations = allAllocations.stream()
				.map(allocation -> AllocationHistoryDTO.builder()
						.allocationId(allocation.getId())
						.assetTag(allocation.getAsset().getSerialNumber())
						.assetName(allocation.getAsset().getName())
						.assignedToUser(allocation.getAssignedTo() != null ? allocation.getAssignedTo().getName() : "Unknown")
						.assignedByUser(allocation.getAssignedBy() != null ? allocation.getAssignedBy().getName() : "Unknown")
						.assignedDate(allocation.getAssignedDate())
						.returnedDate(allocation.getReturnedDate())
						.active(allocation.isActive())
						.notes(allocation.getNotes())
						.build())
				.sorted((a, b) -> b.getAssignedDate().compareTo(a.getAssignedDate()))
				.limit(20)
				.collect(Collectors.toList());

		return UsageStatisticsDTO.builder()
				.totalAssets(totalAssets)
				.totalAllocations(totalAllocations)
				.activeAllocations(activeAllocations)
				.averageAllocationDurationDays(avgDurationDays)
				.allocationsByType(allocationsByType)
				.allocationsByUser(allocationsByUser)
				.topUsedAssets(topUsedAssets)
				.userAllocationStats(userAllocationStats)
				.recentAllocations(recentAllocations)
				.build();
	}
}

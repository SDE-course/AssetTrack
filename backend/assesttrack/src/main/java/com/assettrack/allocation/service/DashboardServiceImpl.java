package com.assettrack.allocation.service;

import com.assettrack.allocation.dto.DashboardStatsDTO;
import com.assettrack.allocation.entity.Allocation;
import com.assettrack.allocation.entity.Asset;
import com.assettrack.allocation.entity.AssetStatus;
import com.assettrack.allocation.repository.AllocationRepository;
import com.assettrack.allocation.repository.AssetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final AssetRepository assetRepository;
    private final AllocationRepository allocationRepository;

    @Override
    @Transactional(readOnly = true)
    public DashboardStatsDTO getDashboardStats() {
        List<Asset> assets = assetRepository.findAll();
        List<Allocation> allocations = allocationRepository.findAll();

        Map<String, Long> typeCounts = assets.stream()
                .collect(Collectors.groupingBy(
                        asset -> normalize(asset.getType() != null ? asset.getType().name() : null),
                        LinkedHashMap::new,
                        Collectors.counting()));

        Map<String, Long> statusCounts = assets.stream()
                .collect(Collectors.groupingBy(
                        asset -> asset.getStatus() == null ? "UNKNOWN" : asset.getStatus().name(),
                        LinkedHashMap::new,
                        Collectors.counting()));

        Map<String, Long> assignedUserCounts = allocations.stream()
                .filter(Allocation::isActive)
                .collect(Collectors.groupingBy(
                        allocation -> allocation.getAssignedTo() == null ? "Unassigned" : allocation.getAssignedTo().getName(),
                        LinkedHashMap::new,
                        Collectors.counting()));

        long assignedAssets = allocations.stream().filter(Allocation::isActive).count();
        long availableAssets = assets.stream()
                .filter(asset -> asset.getStatus() == AssetStatus.AVAILABLE)
                .count();

        return DashboardStatsDTO.builder()
                .totalAssets((long) assets.size())
                .availableAssets(availableAssets)
                .assignedAssets(assignedAssets)
                .typeCounts(typeCounts)
                .statusCounts(statusCounts)
                .assignedUserCounts(assignedUserCounts)
                .build();
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? "Unknown" : value;
    }
}
package com.assettrack.allocation.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AssetUsageDTO {
	private String assetTag;
	private String assetName;
	private String assetType;
	private long allocationCount;
	private String currentStatus;
}

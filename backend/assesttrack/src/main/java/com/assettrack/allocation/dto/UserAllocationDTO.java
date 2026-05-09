package com.assettrack.allocation.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserAllocationDTO {
	private String userName;
	private long allocationCount;
	private long activeAllocations;
}

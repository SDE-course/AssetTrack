package com.assettrack.allocation.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AllocationHistoryDTO {
	private Long allocationId;
	private String assetTag;
	private String assetName;
	private String assignedToUser;
	private String assignedByUser;
	private LocalDateTime assignedDate;
	private LocalDateTime returnedDate;
	private boolean active;
	private String notes;
}

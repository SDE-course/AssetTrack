package com.assettrack.allocation.dto;

import com.assettrack.allocation.entity.AssetStatus;
import lombok.Data;

@Data
public class SearchFilterDTO {
	private String serialNumber;
	private String assignedUser; // name or email (partial)
	private AssetStatus status;
	private String type;
	private String brand;
}


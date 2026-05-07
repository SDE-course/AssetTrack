package com.assettrack.allocation.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AssignAssetRequest {

    @NotNull(message = "Asset ID is required")
    private Long assetId;

    @NotNull(message = "User ID is required")
    private Long userId;

    @Size(max = 500, message = "Notes must not exceed 500 characters")
    private String notes;
}

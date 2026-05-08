package com.assettrack.allocation.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TransferAssetRequest {

    @NotNull(message = "Allocation ID is required")
    private Long allocationId;

    @NotNull(message = "New user ID is required")
    private Long newUserId;

    @Size(max = 500, message = "Notes must not exceed 500 characters")
    private String notes;
}

package com.assettrack.allocation.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class AllocationResponse {
    private Long id;
    private Long assetId;
    private String assetName;
    private String serialNumber;
    private Long assignedToId;
    private String assignedToName;
    private Long assignedById;
    private String assignedByName;
    private LocalDateTime assignedDate;
    private LocalDateTime returnedDate;
    private boolean active;
    private String notes;
}

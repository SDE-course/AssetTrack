package com.assettrack.allocation.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class AllocationHistoryResponse {
    private Long allocationId;
    private String user;
    private Long userId;
    private String assignedBy;
    private LocalDateTime assignedDate;
    private LocalDateTime returnedDate;
    private boolean active;
    private String notes;
}

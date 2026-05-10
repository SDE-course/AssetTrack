package com.assettrack.allocation.dto;

import lombok.*;
import java.time.LocalDateTime;

// ─── Request: user submits a condition report ────────────────────────────────
public class AssetConditionReportDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateRequest {
        private Long assetId;
        private String issueType;
        private String description;
    }

    // ─── Request: admin/manager updates status + notes ────────────────────────
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpdateRequest {
        private String status;      // OPEN | IN_PROGRESS | RESOLVED
        private String adminNotes;
    }

    // ─── Response sent to the frontend ───────────────────────────────────────
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long id;
        private Long assetId;
        private String assetName;
        private String assetSerial;
        private String reportedByName;
        private String issueType;
        private String description;
        private String status;
        private String adminNotes;
        private LocalDateTime reportedAt;
        private LocalDateTime resolvedAt;
    }
}

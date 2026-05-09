package com.assettrack.allocation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class AssetResponse {
    private Long id;

    @JsonProperty("model")
    private String name;

    private String serialNumber;
    private String brand;
    private String type;
    private String status;
    private Integer ram;
    private Integer storage;
    private LocalDate purchaseDate;
    private LocalDate warrantyExpiryDate;

    // Last owner/assigned user information
    private String lastAssignedTo;
    private LocalDate lastAssignedDate;
}

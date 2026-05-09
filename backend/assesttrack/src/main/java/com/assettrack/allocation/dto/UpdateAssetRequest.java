package com.assettrack.allocation.dto;

import com.assettrack.allocation.entity.AssetStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class UpdateAssetRequest {

    private String type;
    private String brand;
    private String model;
    private String serialNumber;
    private AssetStatus status;
    private LocalDate purchaseDate;
    private LocalDate warrantyExpiryDate;
}

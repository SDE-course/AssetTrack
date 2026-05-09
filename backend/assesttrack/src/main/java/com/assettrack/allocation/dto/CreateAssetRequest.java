package com.assettrack.allocation.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateAssetRequest {

    @NotBlank(message = "Asset name is required")
    private String name;

    @NotBlank(message = "Asset brand is required")
    private String brand;

    @NotBlank(message = "Asset type is required")
    private String type;

    @NotBlank(message = "Asset serial number is required")
    private String serialNumber;

    private LocalDate purchaseDate;

    private LocalDate warrantyExpiryDate;

    private Integer ram;
    private Integer storage;
}
package com.assettrack.allocation.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SpareLaptopResponse {
    private Long id;
    private String name;
    private String serialNumber;
    private String brand;
    private Integer ram;
    private Integer storage;
    private String status;
}

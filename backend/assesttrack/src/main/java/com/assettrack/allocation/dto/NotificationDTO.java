package com.assettrack.allocation.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class NotificationDTO {
    private Long id;
    private String title;
    private String message;
    private String category;
    private String assetTag;
    private LocalDateTime createdAt;
    private boolean unread;
}
package com.assettrack.allocation.controller;

import com.assettrack.allocation.dto.NotificationDTO;
import com.assettrack.allocation.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationsController {

    private final NotificationService notificationService;

    @GetMapping("/test")
    public String testNotification() {
        notificationService.createTestNotification();
        return "done";
    }

    @GetMapping
    public ResponseEntity<List<NotificationDTO>> list() {
        return ResponseEntity.ok(notificationService.findAll());
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<Void> markRead(@PathVariable Long id) {
        notificationService.markRead(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/mark-all-read")
    public ResponseEntity<Void> markAllRead() {
        notificationService.markAllRead();
        return ResponseEntity.ok().build();
    }
}

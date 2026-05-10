package com.assettrack.allocation.controller;

import com.assettrack.allocation.config.WarrantyNotificationScheduler;
import com.assettrack.allocation.dto.NotificationDTO;
import com.assettrack.allocation.service.EmailService;
import com.assettrack.allocation.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationsController {

    private final NotificationService notificationService;
    private final WarrantyNotificationScheduler warrantyScheduler;
    private final EmailService emailService;

    /**
     * GET /api/notifications?page=0&size=10&category=warranty
     * Returns paginated notifications — unread first, newest first.
     * Optional ?category filter: warranty | low-stock | assignment | etc.
     */
    @GetMapping
    public ResponseEntity<Page<NotificationDTO>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String category) {

        Pageable pageable = PageRequest.of(page, size);

        if (category != null && !category.isBlank() && !category.equalsIgnoreCase("all")) {
            return ResponseEntity.ok(notificationService.findByCategoryPaginated(category, pageable));
        }
        return ResponseEntity.ok(notificationService.findAllPaginated(pageable));
    }

    /**
     * POST /api/notifications/{id}/read
     */
    @PostMapping("/{id}/read")
    public ResponseEntity<Void> markRead(@PathVariable Long id) {
        notificationService.markRead(id);
        return ResponseEntity.ok().build();
    }

    /**
     * POST /api/notifications/mark-all-read
     */
    @PostMapping("/mark-all-read")
    public ResponseEntity<Void> markAllRead() {
        notificationService.markAllRead();
        return ResponseEntity.ok().build();
    }

    /**
     * DELETE /api/notifications/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        notificationService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ── Dev / test endpoints (remove before production) ──────────────────────

    @GetMapping("/test")
    public String testNotification() {
        notificationService.createTestNotification();
        return "done";
    }

    @GetMapping("/test-email")
    public String testEmail() {
        emailService.sendTestEmail();
        return "Test email sent — check your inbox.";
    }

    @GetMapping("/trigger-warranty-check")
    public ResponseEntity<String> triggerWarrantyCheck() {
        warrantyScheduler.checkUpcomingWarrantyExpirations();
        warrantyScheduler.checkAccessoryStockLevels();
        return ResponseEntity.ok("Warranty and low-stock checks triggered.");
    }

    @GetMapping("/low-stock-counts")
    public ResponseEntity<java.util.Map<String, Long>> lowStockCounts() {
        return ResponseEntity.ok(warrantyScheduler.getAvailableStockByMonitoredTypes());
    }

    @PostMapping("/migrate-legacy")
    public ResponseEntity<String> migrateLegacy() {
        notificationService.migrateLegacyWarningCategories();
        return ResponseEntity.ok("Legacy notifications migrated to new categories.");
    }
}

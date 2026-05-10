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

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationsController {

    private final NotificationService notificationService;
    private final WarrantyNotificationScheduler warrantyScheduler;
    private final EmailService emailService;

    /**
     * GET /api/notifications?page=0&size=10
     * Returns paginated notifications — unread first, newest first.
     */
    @GetMapping
    public ResponseEntity<Page<NotificationDTO>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(notificationService.findAllPaginated(pageable));
    }

    /**
     * POST /api/notifications/{id}/read
     * Marks a single notification as read.
     */
    @PostMapping("/{id}/read")
    public ResponseEntity<Void> markRead(@PathVariable Long id) {
        notificationService.markRead(id);
        return ResponseEntity.ok().build();
    }

    /**
     * POST /api/notifications/mark-all-read
     * Marks every unread notification as read.
     */
    @PostMapping("/mark-all-read")
    public ResponseEntity<Void> markAllRead() {
        notificationService.markAllRead();
        return ResponseEntity.ok().build();
    }

    /**
     * DELETE /api/notifications/{id}
     * Permanently removes a notification.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        notificationService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /api/notifications/test
     * Creates a test notification — remove this before production.
     */
    @GetMapping("/test")
    public String testNotification() {
        notificationService.createTestNotification();
        return "done";
    }

    /**
     * GET /api/notifications/test-email
     * Sends a test email to verify SMTP configuration.
     */
    @GetMapping("/test-email")
    public String testEmail() {
        emailService.sendTestEmail();
        return "Test email sent — check your inbox.";
    }

    /**
     * GET /api/notifications/trigger-warranty-check
     * Manually triggers the warranty expiry check — dev/testing only.
     * Remove this endpoint before production.
     */
    @GetMapping("/trigger-warranty-check")
    public ResponseEntity<String> triggerWarrantyCheck() {
        warrantyScheduler.checkUpcomingWarrantyExpirations();
        warrantyScheduler.checkAccessoryStockLevels();
        return ResponseEntity.ok("Warranty and low-stock checks triggered. Check /api/notifications for new alerts.");
    }

    /**
     * GET /api/notifications/low-stock-counts
     * Returns a map of available counts for monitored low-stock types. Useful for debugging.
     */
    @GetMapping("/low-stock-counts")
    public ResponseEntity<java.util.Map<String, Long>> lowStockCounts() {
        return ResponseEntity.ok(warrantyScheduler.getAvailableStockByMonitoredTypes());
    }

    /**
     * POST /api/notifications/migrate-legacy
     * Migrates old "warning" category notifications to new "warranty" and "low-stock" categories.
     * Dev/admin endpoint — remove before production.
     */
    @PostMapping("/migrate-legacy")
    public ResponseEntity<String> migrateLegacy() {
        notificationService.migrateLegacyWarningCategories();
        return ResponseEntity.ok("Legacy notifications migrated to new categories.");
    }
}
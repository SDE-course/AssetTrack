package com.assettrack.allocation.config;

import com.assettrack.allocation.entity.Asset;
import com.assettrack.allocation.entity.AssetStatus;
import com.assettrack.allocation.entity.Notification;
import com.assettrack.allocation.repository.AssetRepository;
import com.assettrack.allocation.repository.NotificationRepository;
import com.assettrack.allocation.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class WarrantyNotificationScheduler {

    private final AssetRepository assetRepository;
    private final NotificationRepository notificationRepository;
    private final EmailService emailService;

    @Value("${notifications.warranty-warning-days:30}")
    private int warrantyWarningDays;

    @Value("${notifications.low-stock-threshold:3}")
    private int lowStockThreshold;

    @Value("${notifications.low-stock-types:LAPTOP, DESKTOP, MONITOR, KEYBOARD, MOUSE, PRINTER, TABLET, MOBILE}")
    private String lowStockTypes;

    // -----------------------------------------------------------------------
    // Warranty expiry check
    // -----------------------------------------------------------------------

    @Scheduled(cron = "${notifications.warranty-check-cron:0 0 9 * * ?}")
    public void checkUpcomingWarrantyExpirations() {
        LocalDate today = LocalDate.now();
        LocalDate windowEnd = today.plusDays(warrantyWarningDays);

        List<Asset> assets = assetRepository.findAll();
        int alertsCreated = 0;

        for (Asset asset : assets) {
            if (asset.getWarrantyExpiryDate() == null) {
                continue;
            }

            LocalDate expiryDate = asset.getWarrantyExpiryDate();

            // Only alert for warranties expiring within the future window
            // Skip already-expired warranties
            boolean expiresSoon = !expiryDate.isBefore(today) && !expiryDate.isAfter(windowEnd);
            if (!expiresSoon) {
                continue;
            }

            long daysRemaining = ChronoUnit.DAYS.between(today, expiryDate);
            String assetTag = asset.getSerialNumber();

            String title = "Warranty expires soon";
            String message = String.format(
                    "%s (%s) warranty expires on %s (%d day%s remaining).",
                    asset.getName(),
                    assetTag,
                    expiryDate,
                    daysRemaining,
                    daysRemaining == 1 ? "" : "s"
            );

            if (saveNotificationIfAbsent(title, message, "warranty", assetTag)) {
                alertsCreated++;
                emailService.sendWarrantyExpiryAlert(
                        asset.getName(),
                        assetTag,
                        expiryDate.toString(),
                        daysRemaining
                );
            }
        }

        log.info("Warranty check done — {} assets scanned, {} new alerts created (window: {} days)",
                assets.size(), alertsCreated, warrantyWarningDays);
    }

    // -----------------------------------------------------------------------
    // Low stock check
    // -----------------------------------------------------------------------

    @Scheduled(cron = "${notifications.low-stock-check-cron:0 30 9 * * ?}")
    public void checkAccessoryStockLevels() {
        List<String> monitoredTypes = Arrays.stream(lowStockTypes.split(","))
                .map(String::trim)
                .filter(t -> !t.isBlank())
                .map(t -> t.toLowerCase(Locale.ROOT))
                .toList();

        if (monitoredTypes.isEmpty()) {
            log.warn("Low-stock check skipped — no types configured in notifications.low-stock-types");
            return;
        }

        // Count available assets per monitored type
        Map<String, Long> stockByType = assetRepository.findAll().stream()
                .filter(asset -> asset.getType() != null)
                .filter(asset -> monitoredTypes.contains(asset.getType().name().toLowerCase(Locale.ROOT)))
                .filter(asset -> asset.getStatus() == AssetStatus.AVAILABLE)
                .collect(Collectors.groupingBy(
                        asset -> asset.getType().name().toLowerCase(Locale.ROOT),
                        Collectors.counting()
                ));

        int alertsCreated = 0;

        for (String type : monitoredTypes) {
            long count = stockByType.getOrDefault(type, 0L);
            if (count > lowStockThreshold) {
                continue;
            }

            String assetTag = "LOW_STOCK:" + type;
            String title = "Low stock alert";
            String message = String.format(
                    "Only %d %s item%s available in inventory (threshold: %d).",
                    count,
                    type,
                    count == 1 ? " is" : "s are",
                    lowStockThreshold
            );

            if (saveNotificationIfAbsent(title, message, "low-stock", assetTag)) {
                alertsCreated++;
                emailService.sendLowStockAlert(type, count, lowStockThreshold);
            }
        }

        log.info("Low-stock check done — {} types monitored, {} new alerts created (threshold: {})",
            monitoredTypes.size(), alertsCreated, lowStockThreshold);

        // Debug: log current available stock counts for monitored types to aid troubleshooting
        if (log.isDebugEnabled()) {
            log.debug("Low-stock current counts: {}", stockByType);
        }
    }

        /**
         * Return a map of available stock counts for the configured monitored types.
         * Useful for diagnostics and UI debug endpoints.
         */
        public java.util.Map<String, Long> getAvailableStockByMonitoredTypes() {
        java.util.List<String> monitoredTypes = Arrays.stream(lowStockTypes.split(","))
            .map(String::trim)
            .filter(t -> !t.isBlank())
            .map(t -> t.toLowerCase(Locale.ROOT))
            .toList();

        java.util.Map<String, Long> stockByType = assetRepository.findAll().stream()
            .filter(asset -> asset.getType() != null)
            .filter(asset -> monitoredTypes.contains(asset.getType().name().toLowerCase(Locale.ROOT)))
            .filter(asset -> asset.getStatus() == AssetStatus.AVAILABLE)
            .collect(Collectors.groupingBy(
                asset -> asset.getType().name().toLowerCase(Locale.ROOT),
                Collectors.counting()
            ));

        // Ensure all monitored types are present in the map (default 0)
        java.util.Map<String, Long> result = new java.util.HashMap<>();
        for (String t : monitoredTypes) result.put(t, stockByType.getOrDefault(t, 0L));
        return result;
        }

    // -----------------------------------------------------------------------
    // Helper
    // -----------------------------------------------------------------------

    /**
     * Saves a notification only if no unread one already exists for the same
     * category + assetTag. Prevents duplicate alerts on every scheduler run.
     *
     * @return true if a new notification was saved, false if it already existed
     */
    private boolean saveNotificationIfAbsent(String title, String message, String category, String assetTag) {
        if (notificationRepository.existsByCategoryAndAssetTagAndUnreadTrue(category, assetTag)) {
            return false;
        }

        Notification notification = Notification.builder()
                .title(title)
                .message(message)
                .category(category)
                .assetTag(assetTag)
                .createdAt(LocalDateTime.now())
                .unread(true)
                .build();

        notificationRepository.save(notification);
        return true;
    }
}
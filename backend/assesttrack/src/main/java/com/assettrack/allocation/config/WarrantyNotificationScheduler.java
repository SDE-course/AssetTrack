package com.assettrack.allocation.config;

import com.assettrack.allocation.entity.Asset;
import com.assettrack.allocation.entity.AssetStatus;
import com.assettrack.allocation.entity.Notification;
import com.assettrack.allocation.repository.AssetRepository;
import com.assettrack.allocation.repository.NotificationRepository;
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

	@Value("${notifications.warranty-warning-days:30}")
	private int warrantyWarningDays;

	@Value("${notifications.low-stock-threshold:3}")
	private int lowStockThreshold;

	@Value("${notifications.low-stock-types:accessory,mouse,keyboard,dock,adapter,headset,monitor,MOUSE}")
	private String lowStockTypes;

	@Scheduled(cron = "${notifications.warranty-check-cron:0 0 9 * * ?}")
	public void checkUpcomingWarrantyExpirations() {
		LocalDate today = LocalDate.now();
		LocalDate windowEnd = today.plusDays(warrantyWarningDays);

		List<Asset> assets = assetRepository.findAll();

		for (Asset asset : assets) {
			if (asset.getWarrantyExpiryDate() == null) {
				continue;
			}

			LocalDate expiryDate = asset.getWarrantyExpiryDate();
			boolean expiresSoon = !expiryDate.isBefore(today) && !expiryDate.isAfter(windowEnd);
			if (!expiresSoon) {
				continue;
			}

			String assetTag = asset.getSerialNumber();
			String title = "Warranty expires soon";
			String message = String.format(
					"%s (%s) warranty expires on %s (%d days remaining).",
					asset.getName(),
					assetTag,
					expiryDate,
					ChronoUnit.DAYS.between(today, expiryDate)
			);

			saveNotificationIfAbsent(title, message, "warning", assetTag);
		}

		log.debug("Warranty alert check completed for {} assets", assets.size());
	}

	@Scheduled(cron = "${notifications.low-stock-check-cron:0 30 9 * * ?}")
	public void checkAccessoryStockLevels() {
		List<String> monitoredTypes = Arrays.stream(lowStockTypes.split(","))
				.map(String::trim)
				.filter(type -> !type.isBlank())
				.map(type -> type.toLowerCase(Locale.ROOT))
				.toList();

		if (monitoredTypes.isEmpty()) {
			return;
		}

		Map<String, Long> stockByType = assetRepository.findAll().stream()
				.filter(asset -> asset.getType() != null)
				.filter(asset -> monitoredTypes.contains(asset.getType().trim().toLowerCase(Locale.ROOT)))
				.filter(asset -> asset.getStatus() == AssetStatus.AVAILABLE)
				.collect(Collectors.groupingBy(
						asset -> asset.getType().trim().toLowerCase(Locale.ROOT),
						Collectors.counting()
				));

		for (String type : monitoredTypes) {
			long count = stockByType.getOrDefault(type, 0L);
			if (count > lowStockThreshold) {
				continue;
			}

			String assetTag = "LOW_STOCK:" + type;
			String title = "Low stock alert";
			String message = String.format(
					"Only %d %s items are available in inventory.",
					count,
					type
			);

			saveNotificationIfAbsent(title, message, "warning", assetTag);
		}

		log.debug("Low-stock alert check completed for {} monitored types", monitoredTypes.size());
	}

	private void saveNotificationIfAbsent(String title, String message, String category, String assetTag) {
		if (notificationRepository.existsByCategoryAndAssetTagAndUnreadTrue(category, assetTag)) {
			return;
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
	}
}

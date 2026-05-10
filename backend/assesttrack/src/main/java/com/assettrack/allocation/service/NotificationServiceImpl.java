package com.assettrack.allocation.service;

import com.assettrack.allocation.dto.NotificationDTO;
import com.assettrack.allocation.entity.Notification;
import com.assettrack.allocation.repository.NotificationRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    // -----------------------------------------------------------------------
    // Queries
    // -----------------------------------------------------------------------

    @Override
    public List<NotificationDTO> findAll() {
        return notificationRepository.findAll()
                .stream()
                // Unread first, then newest first within each group
                .sorted(Comparator
                        .comparing(Notification::isUnread).reversed()
                        .thenComparing(Comparator.comparing(Notification::getCreatedAt).reversed()))
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Page<NotificationDTO> findAllPaginated(Pageable pageable) {
        List<NotificationDTO> all = findAll();
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), all.size());
        List<NotificationDTO> pageContent = all.subList(start, end);
        return new PageImpl<>(pageContent, pageable, all.size());
    }

    // -----------------------------------------------------------------------
    // Mutations
    // -----------------------------------------------------------------------

    @Override
    @Transactional
    public void markRead(Long id) {
        notificationRepository.findById(id).ifPresent(n -> {
            n.setUnread(false);
            notificationRepository.save(n);
        });
    }

    @Override
    @Transactional
    public void markAllRead() {
        List<Notification> unread = notificationRepository.findByUnreadTrue();
        unread.forEach(n -> n.setUnread(false));
        notificationRepository.saveAll(unread);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!notificationRepository.existsById(id)) {
            throw new EntityNotFoundException("Notification not found: " + id);
        }
        notificationRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void createTestNotification() {
        Notification notification = Notification.builder()
                .title("Test notification")
                .message("Test notification works!")
                .category("info")
                .assetTag("TEST-001")
                .createdAt(LocalDateTime.now())
                .unread(true)
                .build();
        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void migrateLegacyWarningCategories() {
        List<Notification> warnings = notificationRepository.findAll().stream()
                .filter(n -> "warning".equals(n.getCategory()))
                .collect(Collectors.toList());

        for (Notification n : warnings) {
            if (n.getTitle() != null && n.getTitle().contains("Warranty")) {
                n.setCategory("warranty");
            } else if (n.getTitle() != null && n.getTitle().contains("Low stock")) {
                n.setCategory("low-stock");
            } else if (n.getMessage() != null && n.getMessage().contains("warranty")) {
                n.setCategory("warranty");
            } else if (n.getMessage() != null && n.getMessage().contains("stock")) {
                n.setCategory("low-stock");
            }
        }

        notificationRepository.saveAll(warnings);
    }

    // -----------------------------------------------------------------------
    // Mapping
    // -----------------------------------------------------------------------

    private NotificationDTO toDto(Notification n) {
        return NotificationDTO.builder()
                .id(n.getId())
                .title(n.getTitle())
                .message(n.getMessage())
                .category(n.getCategory())
                .assetTag(n.getAssetTag())
                .createdAt(n.getCreatedAt())
                .unread(n.isUnread())
                .build();
    }
}
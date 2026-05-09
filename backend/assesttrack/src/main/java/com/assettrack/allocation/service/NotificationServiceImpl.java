package com.assettrack.allocation.service;

import com.assettrack.allocation.dto.NotificationDTO;
import com.assettrack.allocation.entity.Notification;
import com.assettrack.allocation.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

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

    @Override
    public List<NotificationDTO> findAll() {
        return notificationRepository.findAll()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

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
        var unread = notificationRepository.findByUnreadTrue();
        unread.forEach(n -> n.setUnread(false));
        notificationRepository.saveAll(unread);
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
}

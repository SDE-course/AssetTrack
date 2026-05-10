package com.assettrack.allocation.service;

import com.assettrack.allocation.dto.NotificationDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface NotificationService {
    List<NotificationDTO> findAll();
    Page<NotificationDTO> findAllPaginated(Pageable pageable);
    void markRead(Long id);
    void markAllRead();
    void delete(Long id);
    void createTestNotification();
    void migrateLegacyWarningCategories();
}
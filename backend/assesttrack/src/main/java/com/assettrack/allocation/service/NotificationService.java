package com.assettrack.allocation.service;

import com.assettrack.allocation.dto.NotificationDTO;

import java.util.List;

public interface NotificationService {
	List<NotificationDTO> findAll();
	void markRead(Long id);
	void markAllRead();

	void createTestNotification();
}

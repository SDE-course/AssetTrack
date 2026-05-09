package com.assettrack.allocation.repository;

import com.assettrack.allocation.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
	List<Notification> findByUnreadTrue();

	boolean existsByCategoryAndAssetTagAndUnreadTrue(String category, String assetTag);
}

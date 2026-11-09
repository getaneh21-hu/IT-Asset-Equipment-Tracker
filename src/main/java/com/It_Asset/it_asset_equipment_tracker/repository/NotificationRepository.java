
package com.It_Asset.it_asset_equipment_tracker.repository;

import com.It_Asset.it_asset_equipment_tracker.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository
extends JpaRepository<Notification, Long> {

    // Retrieve all notifications, newest first
    List<Notification> findAllByOrderByCreatedAtDesc();

    // Retrieve unread notifications, newest first
    List<Notification> findByReadFalseOrderByCreatedAtDesc();

    // Count unread notifications
    long countByReadFalse();
}

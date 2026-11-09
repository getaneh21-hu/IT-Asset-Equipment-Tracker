
package com.It_Asset.it_asset_equipment_tracker.service;

import com.It_Asset.it_asset_equipment_tracker.entity.Notification;
import com.It_Asset.it_asset_equipment_tracker.repository.NotificationRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(
            NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    // Create a new notification
    public Notification createNotification(
            String title,
            String message,
            String type) {

        Notification notification =
                new Notification(title, message, type);

        return notificationRepository.save(notification);
    }

    // Get all notifications
    public List<Notification> getAllNotifications() {
        return notificationRepository
                .findAllByOrderByCreatedAtDesc();
    }

    // Get unread notifications
    public List<Notification> getUnreadNotifications() {
        return notificationRepository
                .findByReadFalseOrderByCreatedAtDesc();
    }

    // Get unread notification count
    public long getUnreadCount() {
        return notificationRepository.countByReadFalse();
    }

    // Mark one notification as read
    @Transactional
    public Notification markAsRead(Long id) {

        Notification notification = notificationRepository
                .findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Notification not found with ID: " + id
                        )
                );

        notification.setRead(true);

        return notificationRepository.save(notification);
    }

    // Mark all notifications as read
    @Transactional
    public void markAllAsRead() {

        List<Notification> notifications =
                notificationRepository.findByReadFalseOrderByCreatedAtDesc();

        notifications.forEach(notification ->
                notification.setRead(true)
        );

        notificationRepository.saveAll(notifications);
    }

    // Delete one notification
    public void deleteNotification(Long id) {

        if (!notificationRepository.existsById(id)) {
            throw new RuntimeException(
                    "Notification not found with ID: " + id
            );
        }

        notificationRepository.deleteById(id);
    }

    // Delete all notifications
    public void deleteAllNotifications() {
        notificationRepository.deleteAll();
    }
}

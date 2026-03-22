package com.studyroom.booking.repository;

import com.studyroom.booking.model.Notification;
import com.studyroom.booking.model.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    // Get all notifications for a user (latest first)
    List<Notification> findByUser_IdOrderByCreatedAtDesc(UUID userId);

    // Get unread notifications for a user
    List<Notification> findByUser_IdAndReadFalseOrderByCreatedAtDesc(UUID userId);

    // Count unread notifications
    long countByUser_IdAndReadFalse(UUID userId);

    // Get notifications by type
    List<Notification> findByUser_IdAndTypeOrderByCreatedAtDesc(UUID userId, NotificationType type);

    // Get notifications related to a booking
    List<Notification> findByBookingId(UUID bookingId);

    // Prevent duplicate notifications for same user + type + booking
    boolean existsByUser_IdAndTypeAndBookingId(UUID userId, NotificationType type, UUID bookingId);
}
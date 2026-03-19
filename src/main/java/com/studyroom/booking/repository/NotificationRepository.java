package com.studyroom.booking.repository;

import com.studyroom.booking.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // Get all notifications for a user (latest first)
    List<Notification> findByUser_IdOrderByCreatedAtDesc(UUID userId);

    // Count unread notifications
    long countByUser_IdAndReadFalse(UUID userId);
}
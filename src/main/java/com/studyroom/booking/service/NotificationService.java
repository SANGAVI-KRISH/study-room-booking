package com.studyroom.booking.service;

import com.studyroom.booking.model.Booking;
import com.studyroom.booking.model.Notification;
import com.studyroom.booking.model.NotificationType;
import com.studyroom.booking.model.User;
import com.studyroom.booking.repository.NotificationRepository;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;

    public NotificationService(NotificationRepository notificationRepository,
                               EmailService emailService) {
        this.notificationRepository = notificationRepository;
        this.emailService = emailService;
    }

    public Notification createNotification(User user, NotificationType type, String title, String message) {
        Notification notification = new Notification(user, type, title, message);
        return notificationRepository.save(notification);
    }

    public void sendInAppAndEmail(User user, NotificationType type, String title, String message) {
        if (user == null) {
            return;
        }

        createNotification(user, type, title, message);

        if (user.getEmail() != null && !user.getEmail().isBlank()) {
            emailService.sendEmail(user.getEmail(), title, message);
        }
    }

    public List<Notification> getUserNotifications(UUID userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public long getUnreadCount(UUID userId) {
        return notificationRepository.countByUserIdAndReadFalse(userId);
    }

    public Notification markAsRead(Notification notification) {
        notification.setRead(true);
        return notificationRepository.save(notification);
    }

    public String buildBookingMessage(String prefix, Booking booking) {
        String roomNumber = "N/A";
        String bookingDate = "N/A";
        String timeRange = "N/A";

        if (booking != null) {
            if (booking.getRoom() != null) {
                roomNumber = booking.getRoom().getRoomNumber();
            }

            if (booking.getBookingDate() != null) {
                bookingDate = booking.getBookingDate().toString();
            }

            if (booking.getStartTime() != null && booking.getEndTime() != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
                timeRange = booking.getStartTime().format(formatter)
                        + " - "
                        + booking.getEndTime().format(formatter);
            }
        }

        return prefix
                + "\nRoom: " + roomNumber
                + "\nDate: " + bookingDate
                + "\nTime: " + timeRange;
    }
}
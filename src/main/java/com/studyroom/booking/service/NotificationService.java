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

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("HH:mm");

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public NotificationService(NotificationRepository notificationRepository,
                               EmailService emailService) {
        this.notificationRepository = notificationRepository;
        this.emailService = emailService;
    }

    public Notification createNotification(User user, NotificationType type, String title, String message) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }

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
        return notificationRepository.findByUser_IdOrderByCreatedAtDesc(userId);
    }

    public long getUnreadCount(UUID userId) {
        return notificationRepository.countByUser_IdAndReadFalse(userId);
    }

    public Notification markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found with id: " + notificationId));

        notification.setRead(true);
        return notificationRepository.save(notification);
    }

    public Notification markAsUnread(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found with id: " + notificationId));

        notification.setRead(false);
        return notificationRepository.save(notification);
    }

    public void deleteNotification(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found with id: " + notificationId));

        notificationRepository.delete(notification);
    }

    public String buildBookingMessage(String prefix, Booking booking) {
        String roomNumber = "N/A";
        String roomName = "N/A";
        String bookingDate = "N/A";
        String timeRange = "N/A";
        String startAtText = "N/A";
        String endAtText = "N/A";

        if (booking != null) {
            if (booking.getRoom() != null) {
                roomNumber = booking.getRoom().getRoomNumber();

                String blockName = booking.getRoom().getBlockName();
                if (blockName != null && !blockName.isBlank()) {
                    roomName = blockName + " - " + roomNumber;
                } else {
                    roomName = roomNumber;
                }
            }

            if (booking.getStartAt() != null) {
                bookingDate = booking.getStartAt().format(DATE_FORMATTER);
                startAtText = booking.getStartAt().format(DATE_TIME_FORMATTER);
            }

            if (booking.getEndAt() != null) {
                endAtText = booking.getEndAt().format(DATE_TIME_FORMATTER);
            }

            if (booking.getStartAt() != null && booking.getEndAt() != null) {
                timeRange = booking.getStartAt().format(TIME_FORMATTER)
                        + " - "
                        + booking.getEndAt().format(TIME_FORMATTER);
            }
        }

        return prefix
                + "\nRoom: " + roomName
                + "\nDate: " + bookingDate
                + "\nTime: " + timeRange
                + "\nStart: " + startAtText
                + "\nEnd: " + endAtText;
    }

    public void sendBookingConfirmedNotification(Booking booking) {
        if (booking == null || booking.getUser() == null) {
            return;
        }

        String title = "Booking Confirmed";
        String message = buildBookingMessage("Your study room booking has been confirmed.", booking);

        sendInAppAndEmail(booking.getUser(), NotificationType.BOOKING_CONFIRMED, title, message);
    }

    public void sendBookingCancelledNotification(Booking booking) {
        if (booking == null || booking.getUser() == null) {
            return;
        }

        String title = "Booking Cancelled";
        String message = buildBookingMessage("Your study room booking has been cancelled.", booking);

        sendInAppAndEmail(booking.getUser(), NotificationType.BOOKING_CANCELLED, title, message);
    }

    public void sendBookingApprovedNotification(Booking booking) {
        if (booking == null || booking.getUser() == null) {
            return;
        }

        String title = "Booking Approved";
        String message = buildBookingMessage("Your booking request has been approved.", booking);

        sendInAppAndEmail(booking.getUser(), NotificationType.BOOKING_APPROVED, title, message);
    }

    public void sendBookingRejectedNotification(Booking booking) {
        if (booking == null || booking.getUser() == null) {
            return;
        }

        String title = "Booking Rejected";
        String message = buildBookingMessage("Your booking request has been rejected.", booking);

        sendInAppAndEmail(booking.getUser(), NotificationType.BOOKING_REJECTED, title, message);
    }

    public void sendBookingReminderNotification(Booking booking) {
        if (booking == null || booking.getUser() == null) {
            return;
        }

        String title = "Booking Reminder";
        String message = buildBookingMessage("Reminder: Your booking will start soon.", booking);

        sendInAppAndEmail(booking.getUser(), NotificationType.BOOKING_REMINDER, title, message);
    }

    public void sendWaitlistAvailableNotification(User user, Booking booking) {
        if (user == null) {
            return;
        }

        String title = "Waitlist Slot Available";
        String message = buildBookingMessage("A slot has become available for booking.", booking);

        sendInAppAndEmail(user, NotificationType.WAITLIST_AVAILABLE, title, message);
    }
}
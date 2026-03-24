package com.studyroom.booking.service;

import com.studyroom.booking.model.Booking;
import com.studyroom.booking.model.Notification;
import com.studyroom.booking.model.NotificationType;
import com.studyroom.booking.model.User;
import com.studyroom.booking.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;

    private static final ZoneId APP_ZONE = ZoneId.of("Asia/Kolkata");

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("hh:mm a");

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy, hh:mm a");

    public NotificationService(NotificationRepository notificationRepository,
                               EmailService emailService) {
        this.notificationRepository = notificationRepository;
        this.emailService = emailService;
    }

    public Notification createNotification(User user,
                                           NotificationType type,
                                           String title,
                                           String message) {
        return createNotification(user, type, title, message, null);
    }

    public Notification createNotification(User user,
                                           NotificationType type,
                                           String title,
                                           String message,
                                           UUID bookingId) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }

        Notification notification = new Notification();
        notification.setUser(user);
        notification.setType(type);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setRead(false);
        notification.setBookingId(bookingId);
        notification.setCreatedAt(OffsetDateTime.now(APP_ZONE));

        return notificationRepository.save(notification);
    }

    public void sendInAppAndEmail(User user,
                                  NotificationType type,
                                  String title,
                                  String message) {
        sendInAppAndEmail(user, type, title, message, null);
    }

    public void sendInAppAndEmail(User user,
                                  NotificationType type,
                                  String title,
                                  String message,
                                  UUID bookingId) {
        if (user == null) {
            return;
        }

        createNotification(user, type, title, message, bookingId);

        try {
            if (user.getEmail() != null && !user.getEmail().isBlank()) {
                String emailBody = buildEmailBody(user, title, message);
                emailService.sendEmail(user.getEmail(), title, emailBody);
            }
        } catch (Exception e) {
            log.error("Failed to send email to user {}: {}", user.getEmail(), e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public List<Notification> getUserNotifications(UUID userId) {
        return notificationRepository.findByUser_IdOrderByCreatedAtDesc(userId);
    }

    @Transactional(readOnly = true)
    public List<Notification> getUnreadNotifications(UUID userId) {
        return notificationRepository.findByUser_IdAndReadFalseOrderByCreatedAtDesc(userId);
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(UUID userId) {
        return notificationRepository.countByUser_IdAndReadFalse(userId);
    }

    @Transactional(readOnly = true)
    public Notification getNotificationById(UUID notificationId) {
        return notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found with id: " + notificationId));
    }

    public Notification markAsRead(UUID notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found with id: " + notificationId));

        notification.setRead(true);
        return notificationRepository.save(notification);
    }

    public Notification markAsUnread(UUID notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found with id: " + notificationId));

        notification.setRead(false);
        return notificationRepository.save(notification);
    }

    public void markAllAsRead(UUID userId) {
        List<Notification> unreadNotifications =
                notificationRepository.findByUser_IdAndReadFalseOrderByCreatedAtDesc(userId);

        for (Notification notification : unreadNotifications) {
            notification.setRead(true);
        }

        notificationRepository.saveAll(unreadNotifications);
    }

    public void clearAllNotifications(UUID userId) {
        List<Notification> notifications = notificationRepository.findByUser_IdOrderByCreatedAtDesc(userId);
        notificationRepository.deleteAll(notifications);
    }

    public void deleteNotification(UUID notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found with id: " + notificationId));

        notificationRepository.delete(notification);
    }

    public void deleteNotificationForUser(UUID notificationId, UUID userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found with id: " + notificationId));

        if (notification.getUser() == null || !notification.getUser().getId().equals(userId)) {
            throw new RuntimeException("You are not allowed to delete this notification");
        }

        notificationRepository.delete(notification);
    }

    private String formatDate(OffsetDateTime dateTime) {
        if (dateTime == null) {
            return "N/A";
        }
        return dateTime.atZoneSameInstant(APP_ZONE).format(DATE_FORMATTER);
    }

    private String formatTime(OffsetDateTime dateTime) {
        if (dateTime == null) {
            return "N/A";
        }
        return dateTime.atZoneSameInstant(APP_ZONE).format(TIME_FORMATTER);
    }

    private String formatDateTime(OffsetDateTime dateTime) {
        if (dateTime == null) {
            return "N/A";
        }
        return dateTime.atZoneSameInstant(APP_ZONE).format(DATE_TIME_FORMATTER);
    }

    private String getRoomDisplayName(Booking booking) {
        if (booking == null || booking.getRoom() == null) {
            return "N/A";
        }

        if (booking.getRoom().getDisplayName() != null &&
                !booking.getRoom().getDisplayName().isBlank()) {
            return booking.getRoom().getDisplayName();
        }

        String roomNumber = booking.getRoom().getRoomNumber() != null
                ? booking.getRoom().getRoomNumber()
                : "N/A";

        String blockName = booking.getRoom().getBlockName();
        if (blockName != null && !blockName.isBlank()) {
            return blockName + " - " + roomNumber;
        }

        return roomNumber;
    }

    private String getUserDisplayName(User user) {
        if (user == null || user.getName() == null || user.getName().isBlank()) {
            return "User";
        }
        return user.getName();
    }

    public String buildBookingMessage(String prefix, Booking booking) {
        String roomName = "N/A";
        String bookingDate = "N/A";
        String timeRange = "N/A";
        String startAtText = "N/A";
        String endAtText = "N/A";
        String attendeeText = "N/A";
        String purposeText = "N/A";

        if (booking != null) {
            roomName = getRoomDisplayName(booking);

            if (booking.getStartAt() != null) {
                bookingDate = formatDate(booking.getStartAt());
                startAtText = formatDateTime(booking.getStartAt());
            }

            if (booking.getEndAt() != null) {
                endAtText = formatDateTime(booking.getEndAt());
            }

            if (booking.getStartAt() != null && booking.getEndAt() != null) {
                timeRange = formatTime(booking.getStartAt())
                        + " - "
                        + formatTime(booking.getEndAt());
            }

            if (booking.getAttendeeCount() != null) {
                attendeeText = String.valueOf(booking.getAttendeeCount());
            }

            if (booking.getPurpose() != null && !booking.getPurpose().isBlank()) {
                purposeText = booking.getPurpose();
            }
        }

        return prefix
                + "\nRoom: " + roomName
                + "\nDate: " + bookingDate
                + "\nTime: " + timeRange
                + "\nStart: " + startAtText
                + "\nEnd: " + endAtText
                + "\nAttendees: " + attendeeText
                + "\nPurpose: " + purposeText;
    }

    private String buildEmailBody(User user, String title, String message) {
        return "Dear " + getUserDisplayName(user) + ",\n\n"
                + title + "\n\n"
                + message + "\n\n"
                + "Regards,\n"
                + "Smart Study Room Booking System";
    }

    public void sendBookingConfirmedNotification(Booking booking) {
        if (booking == null || booking.getUser() == null) {
            return;
        }

        String title = "Booking Confirmed";
        String message = buildBookingMessage(
                "Your study room booking has been confirmed.",
                booking
        );

        sendInAppAndEmail(
                booking.getUser(),
                NotificationType.BOOKING_CONFIRMED,
                title,
                message,
                booking.getId()
        );
    }

    public void sendBookingCancelledNotification(Booking booking) {
        if (booking == null || booking.getUser() == null) {
            return;
        }

        String title = "Booking Cancelled";
        String message = buildBookingMessage(
                "Your study room booking has been cancelled.",
                booking
        );

        sendInAppAndEmail(
                booking.getUser(),
                NotificationType.BOOKING_CANCELLED,
                title,
                message,
                booking.getId()
        );
    }

    public void sendBookingApprovedNotification(Booking booking) {
        if (booking == null || booking.getUser() == null) {
            return;
        }

        String title = "Booking Approved";
        String message = buildBookingMessage(
                "Your booking request has been approved.",
                booking
        );

        sendInAppAndEmail(
                booking.getUser(),
                NotificationType.BOOKING_APPROVED,
                title,
                message,
                booking.getId()
        );
    }

    public void sendBookingRejectedNotification(Booking booking) {
        if (booking == null || booking.getUser() == null) {
            return;
        }

        String title = "Booking Rejected";
        String message = buildBookingMessage(
                "Your booking request has been rejected.",
                booking
        );

        sendInAppAndEmail(
                booking.getUser(),
                NotificationType.BOOKING_REJECTED,
                title,
                message,
                booking.getId()
        );
    }

    public void sendBookingReminderNotification(Booking booking) {
        if (booking == null || booking.getUser() == null || booking.getId() == null) {
            return;
        }

        boolean alreadyExists = notificationRepository
                .existsByUser_IdAndTypeAndBookingId(
                        booking.getUser().getId(),
                        NotificationType.BOOKING_REMINDER,
                        booking.getId()
                );

        if (alreadyExists) {
            return;
        }

        String title = "Booking Reminder";
        String message = buildBookingMessage(
                "Reminder: Your booking will start in 1 hour.",
                booking
        );

        sendInAppAndEmail(
                booking.getUser(),
                NotificationType.BOOKING_REMINDER,
                title,
                message,
                booking.getId()
        );
    }

    public void sendWaitlistAvailableNotification(User user, Booking booking) {
        if (user == null) {
            return;
        }

        String title = "Waitlist Slot Available";
        String message = buildBookingMessage(
                "A slot has become available for booking.",
                booking
        );

        sendInAppAndEmail(
                user,
                NotificationType.WAITLIST_AVAILABLE,
                title,
                message,
                booking != null ? booking.getId() : null
        );
    }

    public void sendNewBookingRequestNotificationToAdmins(List<User> admins, Booking booking) {
        if (admins == null || admins.isEmpty() || booking == null) {
            return;
        }

        String bookedBy = "Unknown User";
        if (booking.getUser() != null
                && booking.getUser().getName() != null
                && !booking.getUser().getName().isBlank()) {
            bookedBy = booking.getUser().getName();
        }

        String title = "New Booking Request";
        String message = buildBookingMessage(
                "A new booking request has been created by " + bookedBy + ". Approval is required.",
                booking
        );

        for (User admin : admins) {
            if (admin != null) {
                sendInAppAndEmail(
                        admin,
                        NotificationType.APPROVAL_REQUEST,
                        title,
                        message,
                        booking.getId()
                );
            }
        }
    }

    public void sendRoomDeletedCancellationNotification(Booking booking) {
        if (booking == null || booking.getUser() == null) {
            return;
        }

        String title = "Booking Cancelled - Room Removed";
        String message = buildBookingMessage(
                "Your booking was cancelled because the room was removed by admin.",
                booking
        );

        sendInAppAndEmail(
                booking.getUser(),
                NotificationType.BOOKING_CANCELLED,
                title,
                message,
                booking.getId()
        );
    }
}
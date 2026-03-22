package com.studyroom.booking.scheduler;

import com.studyroom.booking.model.Booking;
import com.studyroom.booking.model.BookingStatus;
import com.studyroom.booking.repository.BookingRepository;
import com.studyroom.booking.service.NotificationService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Component
public class NotificationScheduler {

    private final BookingRepository bookingRepository;
    private final NotificationService notificationService;

    public NotificationScheduler(BookingRepository bookingRepository,
                                 NotificationService notificationService) {
        this.bookingRepository = bookingRepository;
        this.notificationService = notificationService;
    }

    /**
     * Runs every 5 minutes.
     * Sends reminder notification exactly when booking start time
     * falls within the next 1 hour window.
     */
    @Scheduled(fixedRate = 300000) // 5 minutes
    @Transactional
    public void sendOneHourBookingReminders() {
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime oneHourLater = now.plusHours(1);

        List<Booking> approvedBookings =
                bookingRepository.findByStatusAndReminderSentFalse(BookingStatus.APPROVED);

        for (Booking booking : approvedBookings) {
            if (booking.getStartAt() == null || booking.getUser() == null) {
                continue;
            }

            OffsetDateTime startAt = booking.getStartAt();

            boolean isWithinReminderWindow =
                    (startAt.isEqual(now) || startAt.isAfter(now)) &&
                    (startAt.isEqual(oneHourLater) || startAt.isBefore(oneHourLater));

            if (isWithinReminderWindow) {
                notificationService.sendBookingReminderNotification(booking);
                booking.setReminderSent(true);
                bookingRepository.save(booking);
            }
        }
    }
}
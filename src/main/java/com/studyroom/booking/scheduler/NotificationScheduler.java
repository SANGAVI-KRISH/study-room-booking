package com.studyroom.booking.scheduler;

import com.studyroom.booking.model.Booking;
import com.studyroom.booking.model.BookingStatus;
import com.studyroom.booking.repository.BookingRepository;
import com.studyroom.booking.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;

@Component
public class NotificationScheduler {

    private static final Logger log = LoggerFactory.getLogger(NotificationScheduler.class);

    private final BookingRepository bookingRepository;
    private final NotificationService notificationService;

    public NotificationScheduler(BookingRepository bookingRepository,
                                 NotificationService notificationService) {
        this.bookingRepository = bookingRepository;
        this.notificationService = notificationService;
    }

    /**
     * Runs every 5 minutes.
     * Sends reminder for APPROVED bookings that start within the next 1 hour.
     */
    @Scheduled(fixedRateString = "300000")
    @Transactional
    public void sendOneHourBookingReminders() {
        OffsetDateTime now = OffsetDateTime.now();

        List<Booking> approvedBookings =
                bookingRepository.findPendingReminderBookings(BookingStatus.APPROVED);

        for (Booking booking : approvedBookings) {
            try {
                if (booking == null || booking.getStartAt() == null || booking.getUser() == null) {
                    continue;
                }

                OffsetDateTime startAt = booking.getStartAt();

                if (!startAt.isAfter(now)) {
                    continue;
                }

                long minutesUntilStart = Duration.between(now, startAt).toMinutes();

                if (minutesUntilStart > 0 && minutesUntilStart <= 60) {
                    notificationService.sendBookingReminderNotification(booking);
                    booking.setReminderSent(true);
                    bookingRepository.save(booking);

                    log.info("1-hour reminder sent for booking id: {}", booking.getId());
                }
            } catch (Exception e) {
                log.error("Failed to send reminder for booking id: {}",
                        booking != null ? booking.getId() : null, e);
            }
        }
    }

    /**
     * Runs every 5 minutes.
     * Marks CHECKED_IN bookings as COMPLETED after end time.
     */
    @Scheduled(fixedRateString = "300000")
    @Transactional
    public void markCompletedBookings() {
        OffsetDateTime now = OffsetDateTime.now();

        List<Booking> checkedInBookings =
                bookingRepository.findBookingsToMarkCompleted(BookingStatus.CHECKED_IN, now);

        for (Booking booking : checkedInBookings) {
            try {
                if (booking == null || booking.getEndAt() == null) {
                    continue;
                }

                if (!booking.getEndAt().isAfter(now)) {
                    booking.setStatus(BookingStatus.COMPLETED);
                    bookingRepository.save(booking);

                    log.info("Booking marked as COMPLETED for booking id: {}", booking.getId());
                }
            } catch (Exception e) {
                log.error("Failed to mark booking as COMPLETED for booking id: {}",
                        booking != null ? booking.getId() : null, e);
            }
        }
    }

    /**
     * Runs every 5 minutes.
     * Marks APPROVED bookings as NO_SHOW after end time if user never checked in.
     */
    @Scheduled(fixedRateString = "300000")
    @Transactional
    public void markNoShowBookings() {
        OffsetDateTime now = OffsetDateTime.now();

        List<Booking> approvedBookings =
                bookingRepository.findBookingsToMarkNoShow(BookingStatus.APPROVED, now);

        for (Booking booking : approvedBookings) {
            try {
                if (booking == null || booking.getEndAt() == null) {
                    continue;
                }

                if (!booking.getEndAt().isAfter(now)) {
                    booking.setStatus(BookingStatus.NO_SHOW);
                    booking.setCheckinStatus("not_checked_in");
                    booking.setIsPresent(false);
                    bookingRepository.save(booking);

                    log.info("Booking marked as NO_SHOW for booking id: {}", booking.getId());
                }
            } catch (Exception e) {
                log.error("Failed to mark booking as NO_SHOW for booking id: {}",
                        booking != null ? booking.getId() : null, e);
            }
        }
    }
}
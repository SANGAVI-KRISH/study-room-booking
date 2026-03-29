package com.studyroom.booking.scheduler;

import com.studyroom.booking.model.Booking;
import com.studyroom.booking.model.BookingStatus;
import com.studyroom.booking.service.BookingService;
import com.studyroom.booking.service.NotificationService;
import com.studyroom.booking.service.WaitlistService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;

@Component
public class BookingScheduler {

    private final BookingService bookingService;
    private final NotificationService notificationService;
    private final WaitlistService waitlistService;

    private static final ZoneId APP_ZONE = ZoneId.of("Asia/Kolkata");

    public BookingScheduler(
            BookingService bookingService,
            NotificationService notificationService,
            WaitlistService waitlistService
    ) {
        this.bookingService = bookingService;
        this.notificationService = notificationService;
        this.waitlistService = waitlistService;
    }

    /**
     * Runs every 5 minutes
     * Auto-cancel bookings if user did not check-in
     */
    @Scheduled(fixedRate = 300000)
    @Transactional
    public void autoCancelNoShowBookings() {

        OffsetDateTime now = OffsetDateTime.now(APP_ZONE);

        List<Booking> bookings = bookingService.getApprovedBookings()
                .stream()
                .filter(b -> b.getCheckInDeadline() != null)
                .filter(b -> b.getCheckInDeadline().isBefore(now))
                .filter(b -> b.getCheckedInAt() == null)
                .toList();

        for (Booking booking : bookings) {
            try {
                booking.autoCancel("Auto cancelled due to no check-in");

                bookingService.updateBookingStatus(
                        booking.getId(),
                        BookingStatus.AUTO_CANCELLED
                );

                // Notify user
                notificationService.sendAutoCancelledNotification(booking);

                // Trigger waitlist
                waitlistService.processReleasedBooking(booking);

            } catch (Exception e) {
                System.out.println("Auto cancel failed for booking: " + booking.getId());
            }
        }
    }

    /**
     * Runs every 5 minutes
     * Mark completed bookings
     */
    @Scheduled(fixedRate = 300000)
    public void markCompletedBookings() {
        bookingService.markCompletedBookings();
    }

    /**
     * Runs every 5 minutes
     * Mark no-show bookings
     */
    @Scheduled(fixedRate = 300000)
    public void markNoShowBookings() {
        bookingService.markNoShowBookings();
    }
}
package com.studyroom.booking.scheduler;

import com.studyroom.booking.model.Booking;
import com.studyroom.booking.model.BookingStatus;
import com.studyroom.booking.repository.BookingRepository;
import com.studyroom.booking.service.NotificationService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;

@Component
public class ReminderScheduler {

    private final BookingRepository bookingRepository;
    private final NotificationService notificationService;

    private static final ZoneId APP_ZONE = ZoneId.of("Asia/Kolkata");

    public ReminderScheduler(BookingRepository bookingRepository,
                             NotificationService notificationService) {
        this.bookingRepository = bookingRepository;
        this.notificationService = notificationService;
    }

    @Scheduled(fixedRate = 60000)
    public void sendBookingReminders() {
        OffsetDateTime now = OffsetDateTime.now(APP_ZONE);
        OffsetDateTime next30Minutes = now.plusMinutes(30);

        List<Booking> upcomingBookings =
                bookingRepository.findByStatusAndStartAtBetween(
                        BookingStatus.APPROVED,
                        now,
                        next30Minutes
                );

        for (Booking booking : upcomingBookings) {
            if (Boolean.TRUE.equals(booking.getReminderSent())) {
                continue;
            }

            notificationService.sendBookingReminderNotification(booking);

            booking.setReminderSent(true);
            bookingRepository.save(booking);
        }
    }
}
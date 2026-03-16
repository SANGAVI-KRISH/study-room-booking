package com.studyroom.booking.scheduler;

import com.studyroom.booking.model.Booking;
import com.studyroom.booking.model.BookingStatus;
import com.studyroom.booking.model.NotificationType;
import com.studyroom.booking.repository.BookingRepository;
import com.studyroom.booking.service.NotificationService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Component
public class ReminderScheduler {

    private final BookingRepository bookingRepository;
    private final NotificationService notificationService;

    public ReminderScheduler(BookingRepository bookingRepository,
                             NotificationService notificationService) {
        this.bookingRepository = bookingRepository;
        this.notificationService = notificationService;
    }

    @Scheduled(fixedRate = 60000)
    public void sendBookingReminders() {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        LocalTime next30Minutes = now.plusMinutes(30);

        List<Booking> todayBookings = bookingRepository.findByBookingDateAndStatus(today, BookingStatus.APPROVED);

        for (Booking booking : todayBookings) {
            if (booking.getStartTime().isAfter(now) && booking.getStartTime().isBefore(next30Minutes)) {
                String title = "Booking Reminder";
                String message = notificationService.buildBookingMessage(
                        "Reminder: Your booking will start soon.",
                        booking
                );

                notificationService.sendInAppAndEmail(
                        booking.getUser(),
                        NotificationType.BOOKING_REMINDER,
                        title,
                        message
                );
            }
        }
    }
}
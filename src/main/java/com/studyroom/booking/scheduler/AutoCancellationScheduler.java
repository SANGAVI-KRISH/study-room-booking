package com.studyroom.booking.scheduler;

import com.studyroom.booking.model.Booking;
import com.studyroom.booking.model.BookingStatus;
import com.studyroom.booking.repository.BookingRepository;
import com.studyroom.booking.service.BookingService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class AutoCancellationScheduler {

    private final BookingRepository bookingRepository;
    private final BookingService bookingProcessingService;

    public AutoCancellationScheduler(BookingRepository bookingRepository,
                                     BookingService bookingProcessingService) {
        this.bookingRepository = bookingRepository;
        this.bookingProcessingService = bookingProcessingService;
    }

    /**
     * Runs every 5 minutes
     */
    @Scheduled(fixedRate = 300000)
    public void autoCancelNoShowBookings() {

        LocalDateTime now = LocalDateTime.now();

        List<Booking> bookingsToCancel =
                bookingRepository.findBookingsEligibleForAutoCancellation(
                        BookingStatus.APPROVED,
                        now
                );

        for (Booking booking : bookingsToCancel) {
            try {
                // 🔥 Each booking handled in separate transaction
                bookingProcessingService.processSingleBooking(booking);

            } catch (Exception e) {
                System.err.println("Auto-cancel failed for booking: " + booking.getId());
                e.printStackTrace();
            }
        }
    }
}
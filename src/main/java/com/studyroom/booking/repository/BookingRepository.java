package com.studyroom.booking.repository;

import com.studyroom.booking.model.Booking;
import com.studyroom.booking.model.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID> {

    // Basic queries
    List<Booking> findByRoom_Id(UUID roomId);

    List<Booking> findByUser_Id(UUID userId);

    List<Booking> findByBookingDate(LocalDate bookingDate);

    List<Booking> findByStatus(BookingStatus status);

    List<Booking> findByRoom_IdAndBookingDate(UUID roomId, LocalDate bookingDate);

    // Status-based queries
    List<Booking> findByUser_IdAndStatus(UUID userId, BookingStatus status);

    List<Booking> findByRoom_IdAndStatus(UUID roomId, BookingStatus status);

    List<Booking> findByBookingDateAndStatus(LocalDate bookingDate, BookingStatus status);

    List<Booking> findByRoom_IdAndBookingDateAndStatus(UUID roomId, LocalDate bookingDate, BookingStatus status);

    // History and upcoming bookings
    List<Booking> findByUser_IdAndBookingDateBefore(UUID userId, LocalDate date);

    List<Booking> findByUser_IdAndBookingDateGreaterThanEqual(UUID userId, LocalDate date);

    List<Booking> findByUser_IdAndBookingDateGreaterThanEqualAndStatusNot(
            UUID userId,
            LocalDate date,
            BookingStatus status
    );

    List<Booking> findByUser_IdAndBookingDateGreaterThanEqualAndStatusIn(
            UUID userId,
            LocalDate date,
            List<BookingStatus> statuses
    );

    List<Booking> findByRoom_IdAndBookingDateAndStatusIn(
            UUID roomId,
            LocalDate bookingDate,
            List<BookingStatus> statuses
    );

    // Booking History Module
    List<Booking> findByUser_IdAndBookingDate(UUID userId, LocalDate bookingDate);

    List<Booking> findByUser_IdAndBookingDateBetween(UUID userId, LocalDate startDate, LocalDate endDate);

    List<Booking> findByUser_IdAndStatusIn(UUID userId, List<BookingStatus> statuses);

    List<Booking> findByUser_IdAndBookingDateBetweenAndStatusIn(
            UUID userId,
            LocalDate startDate,
            LocalDate endDate,
            List<BookingStatus> statuses
    );

    // Admin history / records
    List<Booking> findByBookingDateBetween(LocalDate startDate, LocalDate endDate);

    List<Booking> findByStatusIn(List<BookingStatus> statuses);

    List<Booking> findByBookingDateBetweenAndStatusIn(
            LocalDate startDate,
            LocalDate endDate,
            List<BookingStatus> statuses
    );

    // Overlap checks for booking creation
    boolean existsByRoom_IdAndBookingDateAndStartTimeLessThanAndEndTimeGreaterThan(
            UUID roomId,
            LocalDate bookingDate,
            LocalTime endTime,
            LocalTime startTime
    );

    boolean existsByRoom_IdAndBookingDateAndStatusInAndStartTimeLessThanAndEndTimeGreaterThan(
            UUID roomId,
            LocalDate bookingDate,
            List<BookingStatus> statuses,
            LocalTime endTime,
            LocalTime startTime
    );

    // Overlap check for booking update
    boolean existsByRoom_IdAndBookingDateAndStatusInAndStartTimeLessThanAndEndTimeGreaterThanAndIdNot(
            UUID roomId,
            LocalDate bookingDate,
            List<BookingStatus> statuses,
            LocalTime endTime,
            LocalTime startTime,
            UUID id
    );
}
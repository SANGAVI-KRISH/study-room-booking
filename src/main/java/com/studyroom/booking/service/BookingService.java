package com.studyroom.booking.service;

import com.studyroom.booking.model.Booking;
import com.studyroom.booking.model.BookingStatus;
import com.studyroom.booking.model.StudyRoom;
import com.studyroom.booking.model.User;
import com.studyroom.booking.repository.BookingRepository;
import com.studyroom.booking.repository.StudyRoomRepository;
import com.studyroom.booking.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final StudyRoomRepository studyRoomRepository;
    private final UserRepository userRepository;

    private static final List<BookingStatus> ACTIVE_BOOKING_STATUSES =
            List.of(BookingStatus.PENDING, BookingStatus.APPROVED);

    private static final List<BookingStatus> HISTORY_BOOKING_STATUSES =
            List.of(BookingStatus.COMPLETED, BookingStatus.CANCELLED, BookingStatus.REJECTED);

    public BookingService(
            BookingRepository bookingRepository,
            StudyRoomRepository studyRoomRepository,
            UserRepository userRepository
    ) {
        this.bookingRepository = bookingRepository;
        this.studyRoomRepository = studyRoomRepository;
        this.userRepository = userRepository;
    }

    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    public Booking getBookingById(UUID id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + id));
    }

    public List<Booking> getBookingsByRoomId(UUID roomId) {
        return bookingRepository.findByRoom_Id(roomId);
    }

    public List<Booking> getBookingsByUserId(UUID userId) {
        return bookingRepository.findByUser_Id(userId);
    }

    public List<Booking> getCurrentBookings(UUID userId) {
        LocalDate today = LocalDate.now();
        return bookingRepository.findByUser_IdAndBookingDateGreaterThanEqual(userId, today);
    }

    public List<Booking> getPastBookings(UUID userId) {
        LocalDate today = LocalDate.now();
        return bookingRepository.findByUser_IdAndBookingDateBefore(userId, today);
    }

    public List<Booking> getActiveCurrentBookings(UUID userId) {
        LocalDate today = LocalDate.now();
        return bookingRepository.findByUser_IdAndBookingDateGreaterThanEqualAndStatusIn(
                userId,
                today,
                ACTIVE_BOOKING_STATUSES
        );
    }

    public List<Booking> getBookingsByDate(LocalDate bookingDate) {
        return bookingRepository.findByBookingDate(bookingDate);
    }

    public List<Booking> getBookingsByStatus(BookingStatus status) {
        return bookingRepository.findByStatus(status);
    }

    public List<Booking> getPendingBookings() {
        return bookingRepository.findByStatus(BookingStatus.PENDING);
    }

    public List<Booking> getApprovedBookings() {
        return bookingRepository.findByStatus(BookingStatus.APPROVED);
    }

    public List<Booking> getRejectedBookings() {
        return bookingRepository.findByStatus(BookingStatus.REJECTED);
    }

    public List<Booking> getCancelledBookings() {
        return bookingRepository.findByStatus(BookingStatus.CANCELLED);
    }

    public List<Booking> getCompletedBookings() {
        return bookingRepository.findByStatus(BookingStatus.COMPLETED);
    }

    public List<Booking> getBookingsByRoomIdAndDate(UUID roomId, LocalDate bookingDate) {
        return bookingRepository.findByRoom_IdAndBookingDate(roomId, bookingDate);
    }

    public List<Booking> getBookingsByUserIdAndStatus(UUID userId, BookingStatus status) {
        return bookingRepository.findByUser_IdAndStatus(userId, status);
    }

    public List<Booking> getBookingsByRoomIdAndStatus(UUID roomId, BookingStatus status) {
        return bookingRepository.findByRoom_IdAndStatus(roomId, status);
    }

    public List<Booking> getBookingsByDateAndStatus(LocalDate bookingDate, BookingStatus status) {
        return bookingRepository.findByBookingDateAndStatus(bookingDate, status);
    }

    // ---------------- BOOKING HISTORY MODULE ----------------

    public List<Booking> getBookingHistory(UUID userId) {
        return bookingRepository.findByUser_IdAndStatusIn(userId, HISTORY_BOOKING_STATUSES);
    }

    public List<Booking> getCompletedBookingsByUserId(UUID userId) {
        return bookingRepository.findByUser_IdAndStatus(userId, BookingStatus.COMPLETED);
    }

    public List<Booking> getCancelledBookingsByUserId(UUID userId) {
        return bookingRepository.findByUser_IdAndStatus(userId, BookingStatus.CANCELLED);
    }

    public List<Booking> getRejectedBookingsByUserId(UUID userId) {
        return bookingRepository.findByUser_IdAndStatus(userId, BookingStatus.REJECTED);
    }

    public List<Booking> getBookingsByUserIdAndDate(UUID userId, LocalDate bookingDate) {
        return bookingRepository.findByUser_IdAndBookingDate(userId, bookingDate);
    }

    public List<Booking> getBookingHistoryByDate(UUID userId, LocalDate bookingDate) {
        return bookingRepository.findByUser_IdAndBookingDateBetweenAndStatusIn(
                userId,
                bookingDate,
                bookingDate,
                HISTORY_BOOKING_STATUSES
        );
    }

    public List<Booking> getBookingHistoryByDateRange(UUID userId, LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new RuntimeException("Start date and end date are required");
        }

        if (endDate.isBefore(startDate)) {
            throw new RuntimeException("End date cannot be before start date");
        }

        return bookingRepository.findByUser_IdAndBookingDateBetweenAndStatusIn(
                userId,
                startDate,
                endDate,
                HISTORY_BOOKING_STATUSES
        );
    }

    public List<Booking> getBookingHistoryByStatuses(UUID userId, List<BookingStatus> statuses) {
        if (statuses == null || statuses.isEmpty()) {
            throw new RuntimeException("At least one booking status is required");
        }

        return bookingRepository.findByUser_IdAndStatusIn(userId, statuses);
    }

    public List<Booking> getAllBookingHistoryForAdmin() {
        return bookingRepository.findByStatusIn(HISTORY_BOOKING_STATUSES);
    }

    public List<Booking> getAllBookingHistoryForAdminByDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new RuntimeException("Start date and end date are required");
        }

        if (endDate.isBefore(startDate)) {
            throw new RuntimeException("End date cannot be before start date");
        }

        return bookingRepository.findByBookingDateBetweenAndStatusIn(
                startDate,
                endDate,
                HISTORY_BOOKING_STATUSES
        );
    }

    // ---------------- BOOKING CREATION ----------------

    public Booking createBooking(
            UUID roomId,
            UUID userId,
            LocalDate bookingDate,
            LocalTime startTime,
            LocalTime endTime
    ) {
        validateBookingInputs(roomId, userId, bookingDate, startTime, endTime);

        StudyRoom room = studyRoomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found with id: " + roomId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        boolean alreadyBooked = bookingRepository
                .existsByRoom_IdAndBookingDateAndStatusInAndStartTimeLessThanAndEndTimeGreaterThan(
                        roomId,
                        bookingDate,
                        ACTIVE_BOOKING_STATUSES,
                        endTime,
                        startTime
                );

        if (alreadyBooked) {
            throw new RuntimeException("Room is already booked for the selected time slot");
        }

        Booking booking = new Booking();
        booking.setRoom(room);
        booking.setUser(user);
        booking.setBookingDate(bookingDate);
        booking.setStartTime(startTime);
        booking.setEndTime(endTime);

        if (room.isApprovalRequired()) {
            booking.setStatus(BookingStatus.PENDING);
        } else {
            booking.setStatus(BookingStatus.APPROVED);
        }

        return bookingRepository.save(booking);
    }

    // ---------------- STATUS UPDATE ----------------

    public Booking updateBookingStatus(UUID bookingId, BookingStatus status) {
        if (status == null) {
            throw new RuntimeException("Booking status is required");
        }

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + bookingId));

        if (status == BookingStatus.APPROVED) {
            return approveBooking(bookingId);
        }

        if (status == BookingStatus.REJECTED) {
            return rejectBooking(bookingId);
        }

        if (status == BookingStatus.CANCELLED) {
            return cancelBooking(bookingId);
        }

        if (status == BookingStatus.COMPLETED) {
            return completeBooking(bookingId);
        }

        booking.setStatus(status);
        return bookingRepository.save(booking);
    }

    public Booking approveBooking(UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + bookingId));

        if (booking.getStatus() == BookingStatus.APPROVED) {
            return booking;
        }

        if (booking.getStatus() == BookingStatus.REJECTED) {
            throw new RuntimeException("Rejected booking cannot be approved");
        }

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new RuntimeException("Cancelled booking cannot be approved");
        }

        if (booking.getStatus() == BookingStatus.COMPLETED) {
            throw new RuntimeException("Completed booking cannot be approved");
        }

        boolean conflictingApprovedBooking = bookingRepository
                .existsByRoom_IdAndBookingDateAndStatusInAndStartTimeLessThanAndEndTimeGreaterThanAndIdNot(
                        booking.getRoom().getId(),
                        booking.getBookingDate(),
                        List.of(BookingStatus.APPROVED),
                        booking.getEndTime(),
                        booking.getStartTime(),
                        booking.getId()
                );

        if (conflictingApprovedBooking) {
            throw new RuntimeException("Cannot approve booking because the room is already approved for that time slot");
        }

        booking.setStatus(BookingStatus.APPROVED);
        return bookingRepository.save(booking);
    }

    public Booking rejectBooking(UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + bookingId));

        if (booking.getStatus() == BookingStatus.APPROVED) {
            throw new RuntimeException("Approved booking cannot be rejected directly. Cancel it instead.");
        }

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new RuntimeException("Cancelled booking cannot be rejected");
        }

        if (booking.getStatus() == BookingStatus.COMPLETED) {
            throw new RuntimeException("Completed booking cannot be rejected");
        }

        booking.setStatus(BookingStatus.REJECTED);
        return bookingRepository.save(booking);
    }

    public Booking cancelBooking(UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + bookingId));

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            return booking;
        }

        if (booking.getStatus() == BookingStatus.REJECTED) {
            throw new RuntimeException("Rejected booking cannot be cancelled");
        }

        if (booking.getStatus() == BookingStatus.COMPLETED) {
            throw new RuntimeException("Completed booking cannot be cancelled");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        return bookingRepository.save(booking);
    }

    public Booking completeBooking(UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + bookingId));

        if (booking.getStatus() == BookingStatus.COMPLETED) {
            return booking;
        }

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new RuntimeException("Cancelled booking cannot be completed");
        }

        if (booking.getStatus() == BookingStatus.REJECTED) {
            throw new RuntimeException("Rejected booking cannot be completed");
        }

        if (booking.getStatus() != BookingStatus.APPROVED) {
            throw new RuntimeException("Only approved booking can be marked as completed");
        }

        booking.setStatus(BookingStatus.COMPLETED);
        return bookingRepository.save(booking);
    }

    // ---------------- RESCHEDULE ----------------

    public Booking rescheduleBooking(
            UUID bookingId,
            LocalDate bookingDate,
            LocalTime startTime,
            LocalTime endTime
    ) {
        if (bookingDate == null) {
            throw new RuntimeException("Booking date is required");
        }

        if (startTime == null || endTime == null) {
            throw new RuntimeException("Start time and end time are required");
        }

        if (!endTime.isAfter(startTime)) {
            throw new RuntimeException("End time must be after start time");
        }

        if (bookingDate.isBefore(LocalDate.now())) {
            throw new RuntimeException("Booking date cannot be in the past");
        }

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + bookingId));

        if (booking.getStatus() == BookingStatus.REJECTED) {
            throw new RuntimeException("Rejected booking cannot be rescheduled");
        }

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new RuntimeException("Cancelled booking cannot be rescheduled");
        }

        if (booking.getStatus() == BookingStatus.COMPLETED) {
            throw new RuntimeException("Completed booking cannot be rescheduled");
        }

        boolean alreadyBooked = bookingRepository
                .existsByRoom_IdAndBookingDateAndStatusInAndStartTimeLessThanAndEndTimeGreaterThanAndIdNot(
                        booking.getRoom().getId(),
                        bookingDate,
                        ACTIVE_BOOKING_STATUSES,
                        endTime,
                        startTime,
                        booking.getId()
                );

        if (alreadyBooked) {
            throw new RuntimeException("Room is already booked for the selected time slot");
        }

        booking.setBookingDate(bookingDate);
        booking.setStartTime(startTime);
        booking.setEndTime(endTime);

        if (booking.getRoom().isApprovalRequired()) {
            booking.setStatus(BookingStatus.PENDING);
        } else {
            booking.setStatus(BookingStatus.APPROVED);
        }

        return bookingRepository.save(booking);
    }

    // ---------------- DELETE ----------------

    public void deleteBooking(UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + bookingId));

        bookingRepository.delete(booking);
    }

    // ---------------- VALIDATION ----------------

    private void validateBookingInputs(
            UUID roomId,
            UUID userId,
            LocalDate bookingDate,
            LocalTime startTime,
            LocalTime endTime
    ) {
        if (roomId == null) {
            throw new RuntimeException("Room ID is required");
        }

        if (userId == null) {
            throw new RuntimeException("User ID is required");
        }

        if (bookingDate == null) {
            throw new RuntimeException("Booking date is required");
        }

        if (startTime == null || endTime == null) {
            throw new RuntimeException("Start time and end time are required");
        }

        if (!endTime.isAfter(startTime)) {
            throw new RuntimeException("End time must be after start time");
        }

        if (bookingDate.isBefore(LocalDate.now())) {
            throw new RuntimeException("Booking date cannot be in the past");
        }
    }
}
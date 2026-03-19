package com.studyroom.booking.service;

import com.studyroom.booking.model.Booking;
import com.studyroom.booking.model.BookingStatus;
import com.studyroom.booking.model.StudyRoom;
import com.studyroom.booking.model.User;
import com.studyroom.booking.repository.BookingRepository;
import com.studyroom.booking.repository.StudyRoomRepository;
import com.studyroom.booking.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final StudyRoomRepository studyRoomRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    private static final ZoneId APP_ZONE = ZoneId.of("Asia/Kolkata");

    private static final long MIN_BOOKING_DURATION_MINUTES = 30;
    private static final long MAX_BOOKING_DURATION_MINUTES = 240;

    private static final List<BookingStatus> ACTIVE_BOOKING_STATUSES =
            List.of(BookingStatus.PENDING, BookingStatus.APPROVED);

    private static final List<BookingStatus> HISTORY_BOOKING_STATUSES =
            List.of(
                    BookingStatus.COMPLETED,
                    BookingStatus.CANCELLED,
                    BookingStatus.REJECTED,
                    BookingStatus.AUTO_CANCELLED
            );

    public BookingService(
            BookingRepository bookingRepository,
            StudyRoomRepository studyRoomRepository,
            UserRepository userRepository,
            NotificationService notificationService
    ) {
        this.bookingRepository = bookingRepository;
        this.studyRoomRepository = studyRoomRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    // ---------------- BASIC GET METHODS ----------------

    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    public Booking getBookingById(UUID id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + id));
    }

    public List<Booking> getBookingsByRoomId(UUID roomId) {
        validateUuid(roomId, "Room ID is required");
        return bookingRepository.findByRoom_IdOrderByStartAtAsc(roomId);
    }

    public List<Booking> getBookingsByUserId(UUID userId) {
        validateUuid(userId, "User ID is required");
        return bookingRepository.findByUser_IdOrderByStartAtDesc(userId);
    }

    public List<Booking> getCurrentBookings(UUID userId) {
        validateUuid(userId, "User ID is required");
        OffsetDateTime now = OffsetDateTime.now(APP_ZONE);
        return bookingRepository.findByUser_IdAndStartAtGreaterThanEqualOrderByStartAtAsc(userId, now);
    }

    public List<Booking> getPastBookings(UUID userId) {
        validateUuid(userId, "User ID is required");
        OffsetDateTime now = OffsetDateTime.now(APP_ZONE);
        return bookingRepository.findByUser_IdAndStartAtBeforeOrderByStartAtDesc(userId, now);
    }

    public List<Booking> getActiveCurrentBookings(UUID userId) {
        validateUuid(userId, "User ID is required");
        OffsetDateTime now = OffsetDateTime.now(APP_ZONE);
        return bookingRepository.findByUser_IdAndStartAtGreaterThanEqualAndStatusInOrderByStartAtAsc(
                userId,
                now,
                ACTIVE_BOOKING_STATUSES
        );
    }

    public List<Booking> getBookingsByStatus(BookingStatus status) {
        validateStatus(status);
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

    public List<Booking> getBookingsByRoomIdAndStatus(UUID roomId, BookingStatus status) {
        validateUuid(roomId, "Room ID is required");
        validateStatus(status);
        return bookingRepository.findByRoom_IdAndStatus(roomId, status);
    }

    public List<Booking> getBookingsByUserIdAndStatus(UUID userId, BookingStatus status) {
        validateUuid(userId, "User ID is required");
        validateStatus(status);
        return bookingRepository.findByUser_IdAndStatus(userId, status);
    }

    public List<Booking> getBookingsByDate(LocalDate bookingDate) {
        validateSingleDate(bookingDate);

        OffsetDateTime dayStart = bookingDate.atStartOfDay().atZone(APP_ZONE).toOffsetDateTime();
        OffsetDateTime dayEnd = bookingDate.plusDays(1).atStartOfDay().atZone(APP_ZONE).toOffsetDateTime();

        return bookingRepository.findByStartAtBetween(dayStart, dayEnd);
    }

    public List<Booking> getBookingsByRoomIdAndDate(UUID roomId, LocalDate bookingDate) {
        validateUuid(roomId, "Room ID is required");
        validateSingleDate(bookingDate);

        OffsetDateTime dayStart = bookingDate.atStartOfDay().atZone(APP_ZONE).toOffsetDateTime();
        OffsetDateTime dayEnd = bookingDate.plusDays(1).atStartOfDay().atZone(APP_ZONE).toOffsetDateTime();

        return bookingRepository.findByRoom_IdAndStartAtBetween(roomId, dayStart, dayEnd);
    }

    public List<Booking> getBookingsByUserIdAndDate(UUID userId, LocalDate bookingDate) {
        validateUuid(userId, "User ID is required");
        validateSingleDate(bookingDate);

        OffsetDateTime dayStart = bookingDate.atStartOfDay().atZone(APP_ZONE).toOffsetDateTime();
        OffsetDateTime dayEnd = bookingDate.plusDays(1).atStartOfDay().atZone(APP_ZONE).toOffsetDateTime();

        return bookingRepository.findByUser_IdAndStartAtBetween(userId, dayStart, dayEnd);
    }

    public List<Booking> getBookingsByDateAndStatus(LocalDate bookingDate, BookingStatus status) {
        validateSingleDate(bookingDate);
        validateStatus(status);

        OffsetDateTime dayStart = bookingDate.atStartOfDay().atZone(APP_ZONE).toOffsetDateTime();
        OffsetDateTime dayEnd = bookingDate.plusDays(1).atStartOfDay().atZone(APP_ZONE).toOffsetDateTime();

        return bookingRepository.findByStartAtBetweenAndStatusIn(dayStart, dayEnd, List.of(status));
    }

    // ---------------- BOOKING HISTORY MODULE ----------------

    public List<Booking> getBookingHistory(UUID userId) {
        validateUuid(userId, "User ID is required");
        return bookingRepository.findByUser_IdAndStatusIn(userId, HISTORY_BOOKING_STATUSES);
    }

    public List<Booking> getCompletedBookingsByUserId(UUID userId) {
        validateUuid(userId, "User ID is required");
        return bookingRepository.findByUser_IdAndStatus(userId, BookingStatus.COMPLETED);
    }

    public List<Booking> getCancelledBookingsByUserId(UUID userId) {
        validateUuid(userId, "User ID is required");
        return bookingRepository.findByUser_IdAndStatus(userId, BookingStatus.CANCELLED);
    }

    public List<Booking> getRejectedBookingsByUserId(UUID userId) {
        validateUuid(userId, "User ID is required");
        return bookingRepository.findByUser_IdAndStatus(userId, BookingStatus.REJECTED);
    }

    public List<Booking> getBookingHistoryByDate(UUID userId, LocalDate bookingDate) {
        validateUuid(userId, "User ID is required");
        validateSingleDate(bookingDate);

        OffsetDateTime dayStart = bookingDate.atStartOfDay().atZone(APP_ZONE).toOffsetDateTime();
        OffsetDateTime dayEnd = bookingDate.plusDays(1).atStartOfDay().atZone(APP_ZONE).toOffsetDateTime();

        return bookingRepository.findByUser_IdAndStartAtBetweenAndStatusIn(
                userId,
                dayStart,
                dayEnd,
                HISTORY_BOOKING_STATUSES
        );
    }

    public List<Booking> getBookingHistoryByDateRange(UUID userId, LocalDate startDate, LocalDate endDate) {
        validateUuid(userId, "User ID is required");
        validateDateRange(startDate, endDate);

        OffsetDateTime rangeStart = startDate.atStartOfDay().atZone(APP_ZONE).toOffsetDateTime();
        OffsetDateTime rangeEnd = endDate.plusDays(1).atStartOfDay().atZone(APP_ZONE).toOffsetDateTime();

        return bookingRepository.findByUser_IdAndStartAtBetweenAndStatusIn(
                userId,
                rangeStart,
                rangeEnd,
                HISTORY_BOOKING_STATUSES
        );
    }

    public List<Booking> getBookingHistoryByStatuses(UUID userId, List<BookingStatus> statuses) {
        validateUuid(userId, "User ID is required");

        if (statuses == null || statuses.isEmpty()) {
            throw new RuntimeException("At least one booking status is required");
        }

        return bookingRepository.findByUser_IdAndStatusIn(userId, statuses);
    }

    public List<Booking> getAllBookingHistoryForAdmin() {
        return bookingRepository.findByStatusIn(HISTORY_BOOKING_STATUSES);
    }

    public List<Booking> getAllBookingHistoryForAdminByDateRange(LocalDate startDate, LocalDate endDate) {
        validateDateRange(startDate, endDate);

        OffsetDateTime rangeStart = startDate.atStartOfDay().atZone(APP_ZONE).toOffsetDateTime();
        OffsetDateTime rangeEnd = endDate.plusDays(1).atStartOfDay().atZone(APP_ZONE).toOffsetDateTime();

        return bookingRepository.findByStartAtBetweenAndStatusIn(
                rangeStart,
                rangeEnd,
                HISTORY_BOOKING_STATUSES
        );
    }

    // ---------------- BOOKING SUMMARY SUPPORT ----------------

    public String getRoomNameById(UUID roomId) {
        validateUuid(roomId, "Room ID is required");

        StudyRoom room = studyRoomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found with id: " + roomId));

        return room.getBlockName() + " - " + room.getRoomNumber();
    }

    // ---------------- BOOKING CREATION ----------------

    public Booking createBooking(
            UUID roomId,
            UUID userId,
            OffsetDateTime startAt,
            OffsetDateTime endAt
    ) {
        return createBooking(roomId, userId, startAt, endAt, null, null);
    }

    public Booking createBooking(
            UUID roomId,
            UUID userId,
            OffsetDateTime startAt,
            OffsetDateTime endAt,
            String purpose,
            Integer attendeeCount
    ) {
        validateBookingInputs(roomId, userId, startAt, endAt, attendeeCount);

        StudyRoom room = studyRoomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found with id: " + roomId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        boolean roomAlreadyBooked =
                bookingRepository.existsByRoom_IdAndStatusInAndStartAtLessThanAndEndAtGreaterThan(
                        roomId,
                        ACTIVE_BOOKING_STATUSES,
                        endAt,
                        startAt
                );

        if (roomAlreadyBooked) {
            throw new RuntimeException("Room is already booked for the selected time slot");
        }

        boolean userAlreadyBooked =
                bookingRepository.existsByUser_IdAndStatusInAndStartAtLessThanAndEndAtGreaterThan(
                        userId,
                        ACTIVE_BOOKING_STATUSES,
                        endAt,
                        startAt
                );

        if (userAlreadyBooked) {
            throw new RuntimeException("User already has another active booking for the selected time slot");
        }

        Booking booking = new Booking();
        booking.setRoom(room);
        booking.setUser(user);
        booking.setStartAt(startAt);
        booking.setEndAt(endAt);
        booking.setPurpose(purpose);
        booking.setAttendeeCount(attendeeCount);
        booking.setCheckinStatus("not_checked_in");
        booking.setCancellationReason(null);
        booking.setApprovalTime(null);
        booking.setApprovedBy(null);
        booking.setReminderSent(false);

        if (room.isApprovalRequired()) {
            booking.setStatus(BookingStatus.PENDING);
        } else {
            booking.setStatus(BookingStatus.APPROVED);
            booking.setApprovalTime(OffsetDateTime.now(APP_ZONE));
            booking.setApprovedBy(user);
        }

        Booking savedBooking = bookingRepository.save(booking);

        if (savedBooking.getStatus() == BookingStatus.APPROVED) {
            notificationService.sendBookingConfirmedNotification(savedBooking);
        }

        return savedBooking;
    }

    // ---------------- STATUS UPDATE ----------------

    public Booking updateBookingStatus(UUID bookingId, BookingStatus status) {
        validateUuid(bookingId, "Booking ID is required");
        validateStatus(status);

        if (status == BookingStatus.APPROVED) {
            throw new RuntimeException("Use approveBooking with approver user ID");
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

        if (status == BookingStatus.AUTO_CANCELLED) {
            return autoCancelBooking(bookingId);
        }

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + bookingId));

        booking.setStatus(status);
        return bookingRepository.save(booking);
    }

    public Booking approveBooking(UUID bookingId, UUID approvedByUserId) {
        validateUuid(bookingId, "Booking ID is required");
        validateUuid(approvedByUserId, "Approved by user ID is required");

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + bookingId));

        User approver = userRepository.findById(approvedByUserId)
                .orElseThrow(() -> new RuntimeException("Approver user not found with id: " + approvedByUserId));

        if (booking.getStatus() == BookingStatus.APPROVED) {
            if (booking.getApprovedBy() == null) {
                booking.setApprovedBy(approver);
                if (booking.getApprovalTime() == null) {
                    booking.setApprovalTime(OffsetDateTime.now(APP_ZONE));
                }
                Booking savedBooking = bookingRepository.save(booking);
                notificationService.sendBookingApprovedNotification(savedBooking);
                return savedBooking;
            }
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

        if (booking.getStatus() == BookingStatus.AUTO_CANCELLED) {
            throw new RuntimeException("Auto-cancelled booking cannot be approved");
        }

        boolean conflictingApprovedBooking =
                bookingRepository.existsByRoom_IdAndStatusInAndStartAtLessThanAndEndAtGreaterThanAndIdNot(
                        booking.getRoom().getId(),
                        List.of(BookingStatus.APPROVED),
                        booking.getEndAt(),
                        booking.getStartAt(),
                        booking.getId()
                );

        if (conflictingApprovedBooking) {
            throw new RuntimeException("Cannot approve booking because the room is already approved for that time slot");
        }

        booking.setStatus(BookingStatus.APPROVED);
        booking.setApprovalTime(OffsetDateTime.now(APP_ZONE));
        booking.setApprovedBy(approver);

        Booking savedBooking = bookingRepository.save(booking);
        notificationService.sendBookingApprovedNotification(savedBooking);
        return savedBooking;
    }

    public Booking rejectBooking(UUID bookingId) {
        validateUuid(bookingId, "Booking ID is required");

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

        if (booking.getStatus() == BookingStatus.AUTO_CANCELLED) {
            throw new RuntimeException("Auto-cancelled booking cannot be rejected");
        }

        booking.setStatus(BookingStatus.REJECTED);
        booking.setApprovalTime(null);
        booking.setApprovedBy(null);

        Booking savedBooking = bookingRepository.save(booking);
        notificationService.sendBookingRejectedNotification(savedBooking);
        return savedBooking;
    }

    public Booking cancelBooking(UUID bookingId) {
        validateUuid(bookingId, "Booking ID is required");

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

        if (booking.getStatus() == BookingStatus.AUTO_CANCELLED) {
            throw new RuntimeException("Auto-cancelled booking cannot be cancelled");
        }

        booking.setStatus(BookingStatus.CANCELLED);

        Booking savedBooking = bookingRepository.save(booking);
        notificationService.sendBookingCancelledNotification(savedBooking);
        return savedBooking;
    }

    public Booking autoCancelBooking(UUID bookingId) {
        validateUuid(bookingId, "Booking ID is required");

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + bookingId));

        if (booking.getStatus() == BookingStatus.AUTO_CANCELLED) {
            return booking;
        }

        if (booking.getStatus() == BookingStatus.COMPLETED) {
            throw new RuntimeException("Completed booking cannot be auto-cancelled");
        }

        booking.setStatus(BookingStatus.AUTO_CANCELLED);

        Booking savedBooking = bookingRepository.save(booking);
        notificationService.sendBookingCancelledNotification(savedBooking);
        return savedBooking;
    }

    public Booking completeBooking(UUID bookingId) {
        validateUuid(bookingId, "Booking ID is required");

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

        if (booking.getStatus() == BookingStatus.AUTO_CANCELLED) {
            throw new RuntimeException("Auto-cancelled booking cannot be completed");
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
            OffsetDateTime startAt,
            OffsetDateTime endAt
    ) {
        validateUuid(bookingId, "Booking ID is required");

        if (startAt == null || endAt == null) {
            throw new RuntimeException("Start time and end time are required");
        }

        if (!endAt.isAfter(startAt)) {
            throw new RuntimeException("End time must be after start time");
        }

        if (!startAt.isAfter(OffsetDateTime.now(APP_ZONE))) {
            throw new RuntimeException("Start time must be in the future");
        }

        validateBookingDuration(startAt, endAt);

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

        if (booking.getStatus() == BookingStatus.AUTO_CANCELLED) {
            throw new RuntimeException("Auto-cancelled booking cannot be rescheduled");
        }

        boolean roomAlreadyBooked =
                bookingRepository.existsByRoom_IdAndStatusInAndStartAtLessThanAndEndAtGreaterThanAndIdNot(
                        booking.getRoom().getId(),
                        ACTIVE_BOOKING_STATUSES,
                        endAt,
                        startAt,
                        booking.getId()
                );

        if (roomAlreadyBooked) {
            throw new RuntimeException("Room is already booked for the selected time slot");
        }

        boolean userAlreadyBooked =
                bookingRepository.existsByUser_IdAndStatusInAndStartAtLessThanAndEndAtGreaterThanAndIdNot(
                        booking.getUser().getId(),
                        ACTIVE_BOOKING_STATUSES,
                        endAt,
                        startAt,
                        booking.getId()
                );

        if (userAlreadyBooked) {
            throw new RuntimeException("User already has another active booking for the selected time slot");
        }

        booking.setStartAt(startAt);
        booking.setEndAt(endAt);
        booking.setCheckinStatus("not_checked_in");
        booking.setCancellationReason(null);
        booking.setReminderSent(false);

        if (booking.getRoom().isApprovalRequired()) {
            booking.setStatus(BookingStatus.PENDING);
            booking.setApprovalTime(null);
            booking.setApprovedBy(null);
        } else {
            booking.setStatus(BookingStatus.APPROVED);
            booking.setApprovalTime(OffsetDateTime.now(APP_ZONE));
            booking.setApprovedBy(booking.getUser());
        }

        Booking savedBooking = bookingRepository.save(booking);

        if (savedBooking.getStatus() == BookingStatus.APPROVED) {
            notificationService.sendBookingConfirmedNotification(savedBooking);
        }

        return savedBooking;
    }

    // ---------------- DELETE ----------------

    public void deleteBooking(UUID bookingId) {
        validateUuid(bookingId, "Booking ID is required");

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + bookingId));

        bookingRepository.delete(booking);
    }

    // ---------------- VALIDATION ----------------

    private void validateBookingInputs(
            UUID roomId,
            UUID userId,
            OffsetDateTime startAt,
            OffsetDateTime endAt,
            Integer attendeeCount
    ) {
        validateUuid(roomId, "Room ID is required");
        validateUuid(userId, "User ID is required");

        if (startAt == null) {
            throw new RuntimeException("Start time is required");
        }

        if (endAt == null) {
            throw new RuntimeException("End time is required");
        }

        if (!endAt.isAfter(startAt)) {
            throw new RuntimeException("End time must be after start time");
        }

        OffsetDateTime now = OffsetDateTime.now(APP_ZONE);

        if (!startAt.isAfter(now)) {
            throw new RuntimeException("Booking start time must be in the future");
        }

        validateBookingDuration(startAt, endAt);

        if (attendeeCount != null && attendeeCount <= 0) {
            throw new RuntimeException("Attendee count must be greater than 0");
        }
    }

    private void validateBookingDuration(OffsetDateTime startAt, OffsetDateTime endAt) {
        long durationMinutes = Duration.between(startAt, endAt).toMinutes();

        if (durationMinutes < MIN_BOOKING_DURATION_MINUTES) {
            throw new RuntimeException("Minimum booking duration is 30 minutes");
        }

        if (durationMinutes > MAX_BOOKING_DURATION_MINUTES) {
            throw new RuntimeException("Maximum booking duration is 4 hours");
        }
    }

    private void validateSingleDate(LocalDate date) {
        if (date == null) {
            throw new RuntimeException("Date is required");
        }
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null) {
            throw new RuntimeException("Start date is required");
        }

        if (endDate == null) {
            throw new RuntimeException("End date is required");
        }

        if (endDate.isBefore(startDate)) {
            throw new RuntimeException("End date must be on or after start date");
        }
    }

    private void validateStatus(BookingStatus status) {
        if (status == null) {
            throw new RuntimeException("Booking status is required");
        }
    }

    private void validateUuid(UUID id, String message) {
        if (id == null) {
            throw new RuntimeException(message);
        }
    }
}
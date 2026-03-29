package com.studyroom.booking.service;

import com.studyroom.booking.model.Booking;
import com.studyroom.booking.model.BookingStatus;
import com.studyroom.booking.model.StudyRoom;
import com.studyroom.booking.model.TimeSlot;
import com.studyroom.booking.model.User;
import com.studyroom.booking.repository.BookingRepository;
import com.studyroom.booking.repository.StudyRoomRepository;
import com.studyroom.booking.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class BookingService {

    private final BookingRepository bookingRepository;
    private final StudyRoomRepository studyRoomRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final TimeSlotService timeSlotService;
    private final MaintenanceService maintenanceService;
    private final WaitlistService waitlistService;
    

    private static final ZoneId APP_ZONE = ZoneId.of("Asia/Kolkata");

    private static final int CHECKIN_EARLY_MINUTES = 15;
    private static final int CHECKIN_LATE_MINUTES = 30;

    private static final List<BookingStatus> ACTIVE_STATUSES =
            List.of(BookingStatus.PENDING, BookingStatus.APPROVED, BookingStatus.CHECKED_IN);

    public BookingService(
            BookingRepository bookingRepository,
            StudyRoomRepository studyRoomRepository,
            UserRepository userRepository,
            NotificationService notificationService,
            TimeSlotService timeSlotService,
            MaintenanceService maintenanceService,
            WaitlistService waitlistService
    ) {
        this.bookingRepository = bookingRepository;
        this.studyRoomRepository = studyRoomRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.timeSlotService = timeSlotService;
        this.maintenanceService = maintenanceService;
        this.waitlistService = waitlistService;
    }

    // =========================
    // BASIC GET METHODS
    // =========================

    @Transactional(readOnly = true)
    public List<Booking> getAllBookings() {
        return bookingRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(
                        Booking::getCreatedAt,
                        Comparator.nullsLast(Comparator.reverseOrder())
                ))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Booking getBookingById(UUID id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
    }

    @Transactional(readOnly = true)
    public List<Booking> getBookingsByRoomId(UUID roomId) {
        return bookingRepository.findByRoom_Id(roomId)
                .stream()
                .sorted(byStartAtDesc())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Booking> getBookingsByUserId(UUID userId) {
        return bookingRepository.findByUser_Id(userId)
                .stream()
                .sorted(byStartAtDesc())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Booking> getCurrentBookings(UUID userId) {
        OffsetDateTime now = OffsetDateTime.now(APP_ZONE);

        return getBookingsByUserId(userId)
                .stream()
                .filter(booking -> booking.getEndAt() != null && booking.getEndAt().isAfter(now))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Booking> getPastBookings(UUID userId) {
        OffsetDateTime now = OffsetDateTime.now(APP_ZONE);

        return getBookingsByUserId(userId)
                .stream()
                .filter(booking -> booking.getEndAt() != null && !booking.getEndAt().isAfter(now))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Booking> getActiveCurrentBookings(UUID userId) {
        OffsetDateTime now = OffsetDateTime.now(APP_ZONE);

        return getBookingsByUserId(userId)
                .stream()
                .filter(booking -> booking.getEndAt() != null && booking.getEndAt().isAfter(now))
                .filter(booking -> booking.getStatus() != null && ACTIVE_STATUSES.contains(booking.getStatus()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Booking> getBookingsByDate(LocalDate date) {
        return bookingRepository.findAll()
                .stream()
                .filter(booking -> booking.getStartAt() != null
                        && booking.getStartAt().atZoneSameInstant(APP_ZONE).toLocalDate().equals(date))
                .sorted(byStartAtDesc())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Booking> getBookingsByStatus(BookingStatus status) {
        return bookingRepository.findByStatus(status)
                .stream()
                .sorted(byStartAtDesc())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Booking> getBookingsByRoomIdAndDate(UUID roomId, LocalDate date) {
        return bookingRepository.findByRoom_Id(roomId)
                .stream()
                .filter(booking -> booking.getStartAt() != null
                        && booking.getStartAt().atZoneSameInstant(APP_ZONE).toLocalDate().equals(date))
                .sorted(byStartAtDesc())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Booking> getBookingsByUserIdAndStatus(UUID userId, BookingStatus status) {
        return bookingRepository.findByUser_IdAndStatus(userId, status)
                .stream()
                .sorted(byStartAtDesc())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Booking> getBookingsByRoomIdAndStatus(UUID roomId, BookingStatus status) {
        return bookingRepository.findByRoom_IdAndStatus(roomId, status)
                .stream()
                .sorted(byStartAtDesc())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Booking> getBookingsByDateAndStatus(LocalDate date, BookingStatus status) {
        return bookingRepository.findByStatus(status)
                .stream()
                .filter(booking -> booking.getStartAt() != null
                        && booking.getStartAt().atZoneSameInstant(APP_ZONE).toLocalDate().equals(date))
                .sorted(byStartAtDesc())
                .collect(Collectors.toList());
    }

    // =========================
    // STATUS SHORTCUT METHODS
    // =========================

    @Transactional(readOnly = true)
    public List<Booking> getPendingBookings() {
        return getBookingsByStatus(BookingStatus.PENDING);
    }

    @Transactional(readOnly = true)
    public List<Booking> getApprovedBookings() {
        return getBookingsByStatus(BookingStatus.APPROVED);
    }

    @Transactional(readOnly = true)
    public List<Booking> getRejectedBookings() {
        return getBookingsByStatus(BookingStatus.REJECTED);
    }

    @Transactional(readOnly = true)
    public List<Booking> getCancelledBookings() {
        return getBookingsByStatus(BookingStatus.CANCELLED);
    }

    @Transactional(readOnly = true)
    public List<Booking> getCheckedInBookings() {
        return getBookingsByStatus(BookingStatus.CHECKED_IN);
    }

    @Transactional(readOnly = true)
    public List<Booking> getCompletedBookings() {
        return getBookingsByStatus(BookingStatus.COMPLETED);
    }

    @Transactional(readOnly = true)
    public List<Booking> getNoShowBookings() {
        return getBookingsByStatus(BookingStatus.NO_SHOW);
    }

    @Transactional(readOnly = true)
    public List<Booking> getAutoCancelledBookings() {
        return getBookingsByStatus(BookingStatus.AUTO_CANCELLED);
    }

    // =========================
    // HISTORY METHODS
    // =========================

    @Transactional(readOnly = true)
    public List<Booking> getBookingHistory(UUID userId) {
        return getBookingsByUserId(userId)
                .stream()
                .filter(this::isHistoryBooking)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Booking> getCompletedBookingsByUserId(UUID userId) {
        return getBookingsByUserIdAndStatus(userId, BookingStatus.COMPLETED);
    }

    @Transactional(readOnly = true)
    public List<Booking> getCancelledBookingsByUserId(UUID userId) {
        return getBookingsByUserIdAndStatus(userId, BookingStatus.CANCELLED);
    }

    @Transactional(readOnly = true)
    public List<Booking> getRejectedBookingsByUserId(UUID userId) {
        return getBookingsByUserIdAndStatus(userId, BookingStatus.REJECTED);
    }

    @Transactional(readOnly = true)
    public List<Booking> getNoShowBookingsByUserId(UUID userId) {
        return getBookingsByUserIdAndStatus(userId, BookingStatus.NO_SHOW);
    }

    @Transactional(readOnly = true)
    public List<Booking> getAutoCancelledBookingsByUserId(UUID userId) {
        return getBookingsByUserIdAndStatus(userId, BookingStatus.AUTO_CANCELLED);
    }

    @Transactional(readOnly = true)
    public List<Booking> getBookingHistoryByDate(UUID userId, LocalDate date) {
        return getBookingHistory(userId)
                .stream()
                .filter(booking -> booking.getStartAt() != null
                        && booking.getStartAt().atZoneSameInstant(APP_ZONE).toLocalDate().equals(date))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Booking> getBookingHistoryByDateRange(UUID userId, LocalDate startDate, LocalDate endDate) {
        return getBookingHistory(userId)
                .stream()
                .filter(booking -> {
                    if (booking.getStartAt() == null) {
                        return false;
                    }
                    LocalDate bookingDate = booking.getStartAt().atZoneSameInstant(APP_ZONE).toLocalDate();
                    return !bookingDate.isBefore(startDate) && !bookingDate.isAfter(endDate);
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Booking> getAllBookingHistoryForAdmin() {
        return bookingRepository.findAll()
                .stream()
                .filter(this::isHistoryBooking)
                .sorted(byStartAtDesc())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Booking> getAllBookingHistoryForAdminByDateRange(LocalDate startDate, LocalDate endDate) {
        return getAllBookingHistoryForAdmin()
                .stream()
                .filter(booking -> {
                    if (booking.getStartAt() == null) {
                        return false;
                    }
                    LocalDate bookingDate = booking.getStartAt().atZoneSameInstant(APP_ZONE).toLocalDate();
                    return !bookingDate.isBefore(startDate) && !bookingDate.isAfter(endDate);
                })
                .collect(Collectors.toList());
    }

    // =========================
    // ROOM HELPERS
    // =========================

    @Transactional(readOnly = true)
    public String getRoomNameById(UUID roomId) {
        StudyRoom room = studyRoomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        if (room.getDisplayName() != null && !room.getDisplayName().isBlank()) {
            return room.getDisplayName();
        }

        return "Room";
    }

    // =========================
    // CREATE BOOKING - TIME RANGE BASED
    // =========================

    public Booking createBooking(
        UUID roomId,
        UUID userId,
        OffsetDateTime startAt,
        OffsetDateTime endAt,
        String purpose,
        Integer attendeeCount
) {
    // 1️⃣ Validate inputs
    validateCreateBookingInputs(roomId, userId, startAt, endAt, attendeeCount);

    // 2️⃣ Fetch room and user
    StudyRoom room = studyRoomRepository.findById(roomId)
            .orElseThrow(() -> new RuntimeException("Room not found"));

    User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

    // 3️⃣ Validate slot and maintenance
    timeSlotService.validateBookingSlot(roomId, startAt, endAt);
    validateRoomNotUnderMaintenance(room.getId(), startAt, endAt);

    // 4️⃣ Validate room availability & handle waitlist
    validateRoomBookingAvailability(room, startAt, endAt, userId, attendeeCount, null);

    // 5️⃣ Build booking based on role (STAFF / STUDENT)
    Booking booking = buildNewBooking(room, user, startAt, endAt, purpose, attendeeCount);

    // 6️⃣ Save booking
    Booking saved = bookingRepository.save(booking);

    // 7️⃣ Send notifications based on status
    if (BookingStatus.PENDING.equals(saved.getStatus())) {
        notifyAdminsForNewBooking(saved);
    } else if (BookingStatus.APPROVED.equals(saved.getStatus())) {
        notificationService.sendBookingConfirmedNotification(saved);
    }

    return saved;
}

    // =========================
    // CREATE BOOKING - SLOT ID BASED
    // =========================

 public Booking createBooking(
        UUID roomId,
        UUID userId,
        UUID timeSlotId,
        Integer attendeeCount,
        String purpose
) {
    // 1️⃣ Validate basic inputs
    if (roomId == null) throw new RuntimeException("Room ID is required");
    if (userId == null) throw new RuntimeException("User ID is required");
    if (timeSlotId == null) throw new RuntimeException("Time slot ID is required");
    if (attendeeCount == null || attendeeCount <= 0)
        throw new RuntimeException("Attendee count must be greater than 0");

    // 2️⃣ Fetch room, user, and slot
    StudyRoom room = studyRoomRepository.findById(roomId)
            .orElseThrow(() -> new RuntimeException("Room not found"));
    User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
    TimeSlot slot = timeSlotService.getSlotById(timeSlotId);

    if (slot.getRoom() == null || !roomId.equals(slot.getRoom().getId()))
        throw new RuntimeException("Selected slot does not belong to this room");
    if (slot.getStartAt() == null || slot.getEndAt() == null || !slot.getEndAt().isAfter(slot.getStartAt()))
        throw new RuntimeException("Invalid time slot range");
    if (!slot.getStartAt().isAfter(OffsetDateTime.now(APP_ZONE)))
        throw new RuntimeException("Start time must be in the future");

    // 3️⃣ Validate maintenance & availability
    validateRoomNotUnderMaintenance(room.getId(), slot.getStartAt(), slot.getEndAt());

    // ✅ Directly validate availability & auto-add to waitlist if needed
    validateRoomBookingAvailability(room, slot.getStartAt(), slot.getEndAt(), userId, attendeeCount, null);

    // 4️⃣ Build booking
    Booking booking = buildNewBooking(room, user, slot.getStartAt(), slot.getEndAt(), purpose, attendeeCount);

    // 5️⃣ Save booking
    Booking saved = bookingRepository.save(booking);

    // 6️⃣ Send notifications
    if (BookingStatus.PENDING.equals(saved.getStatus())) {
        notifyAdminsForNewBooking(saved);
    } else if (BookingStatus.APPROVED.equals(saved.getStatus())) {
        notificationService.sendBookingConfirmedNotification(saved);
    }

    return saved;
}
    @Transactional(readOnly = true)
    public boolean canCheckIn(UUID bookingId, UUID userId) {
        if (bookingId == null || userId == null) {
            return false;
        }

        return bookingRepository.findByIdAndUser_Id(bookingId, userId)
                .map(booking -> BookingStatus.APPROVED.equals(booking.getStatus())
                        && !"checked_in".equalsIgnoreCase(booking.getCheckinStatus())
                        && booking.getCheckedInAt() == null
                        && isWithinCheckInWindow(booking, OffsetDateTime.now(APP_ZONE)))
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public boolean isWithinCheckInWindow(Booking booking) {
        return isWithinCheckInWindow(booking, OffsetDateTime.now(APP_ZONE));
    }

    private boolean isWithinCheckInWindow(Booking booking, OffsetDateTime now) {
        if (booking == null || booking.getStartAt() == null || now == null) {
            return false;
        }

        OffsetDateTime allowedFrom = booking.getStartAt().minusMinutes(CHECKIN_EARLY_MINUTES);
        OffsetDateTime allowedUntil = booking.getStartAt().plusMinutes(CHECKIN_LATE_MINUTES);

        return !now.isBefore(allowedFrom) && !now.isAfter(allowedUntil);
    }

    // =========================
    // APPROVE / REJECT / CANCEL / COMPLETE
    // =========================

    public Booking approveBooking(UUID bookingId, UUID approverId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        User approver = userRepository.findById(approverId)
                .orElseThrow(() -> new RuntimeException("Approver not found"));

        if (BookingStatus.CANCELLED.equals(booking.getStatus())
                || BookingStatus.REJECTED.equals(booking.getStatus())
                || BookingStatus.COMPLETED.equals(booking.getStatus())
                || BookingStatus.NO_SHOW.equals(booking.getStatus())
                || BookingStatus.AUTO_CANCELLED.equals(booking.getStatus())) {
            throw new RuntimeException("This booking cannot be approved");
        }

        if (booking.getRoom() == null) {
            throw new RuntimeException("Booking room is missing");
        }

        validateRoomNotUnderMaintenance(booking.getRoom().getId(), booking.getStartAt(), booking.getEndAt());
        validateRoomBookingAvailability(
                booking.getRoom(),
                booking.getStartAt(),
                booking.getEndAt(),
                booking.getUser() != null ? booking.getUser().getId() : null,
                booking.getAttendeeCount(),
                booking.getId()
        );

        booking.approve(approver);
        booking.setReminderSent(false);

        Booking saved = bookingRepository.save(booking);
        notificationService.sendBookingApprovedNotification(saved);

        return saved;
    }

    public Booking rejectBooking(UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (BookingStatus.CANCELLED.equals(booking.getStatus())
                || BookingStatus.COMPLETED.equals(booking.getStatus())
                || BookingStatus.CHECKED_IN.equals(booking.getStatus())
                || BookingStatus.NO_SHOW.equals(booking.getStatus())
                || BookingStatus.AUTO_CANCELLED.equals(booking.getStatus())) {
            throw new RuntimeException("This booking cannot be rejected");
        }

        booking.reject("Rejected by admin");

        Booking saved = bookingRepository.save(booking);
        notificationService.sendBookingRejectedNotification(saved);
        waitlistService.processReleasedBooking(saved);

        return saved;
    }

    public Booking cancelBooking(UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (BookingStatus.COMPLETED.equals(booking.getStatus())
                || BookingStatus.NO_SHOW.equals(booking.getStatus())) {
            throw new RuntimeException("This booking cannot be cancelled");
        }

        booking.cancel("Cancelled by user/admin");

        Booking saved = bookingRepository.save(booking);
        notificationService.sendBookingCancelledNotification(saved);
        waitlistService.processReleasedBooking(saved);

        return saved;
    }

    public Booking completeBooking(UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        booking.complete();
        return bookingRepository.save(booking);
    }

    public Booking updateBookingStatus(UUID bookingId, BookingStatus status) {
        if (status == null) {
            throw new RuntimeException("Status is required");
        }

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        boolean releasedSlot = false;

        if (BookingStatus.APPROVED.equals(status)) {
            if (booking.getRoom() == null) {
                throw new RuntimeException("Booking room is missing");
            }

            validateRoomNotUnderMaintenance(booking.getRoom().getId(), booking.getStartAt(), booking.getEndAt());
            validateRoomBookingAvailability(
                    booking.getRoom(),
                    booking.getStartAt(),
                    booking.getEndAt(),
                    booking.getUser() != null ? booking.getUser().getId() : null,
                    booking.getAttendeeCount(),
                    booking.getId()
            );

            booking.setStatus(BookingStatus.APPROVED);
            if (booking.getApprovalTime() == null) {
                booking.setApprovalTime(OffsetDateTime.now(APP_ZONE));
            }
            booking.setReminderSent(false);
            if (booking.getCheckInDeadline() == null && booking.getStartAt() != null) {
                booking.setCheckInDeadline(booking.getStartAt().plusMinutes(15));
            }
        } else if (BookingStatus.CHECKED_IN.equals(status)) {
            booking.markCheckedIn();
        } else if (BookingStatus.NO_SHOW.equals(status)) {
            booking.markNoShow();
            releasedSlot = true;
        } else if (BookingStatus.CANCELLED.equals(status)) {
            booking.cancel("Status updated to cancelled");
            releasedSlot = true;
        } else if (BookingStatus.REJECTED.equals(status)) {
            booking.reject("Status updated to rejected");
            releasedSlot = true;
        } else if (BookingStatus.AUTO_CANCELLED.equals(status)) {
            booking.autoCancel("Status updated to auto-cancelled");
            releasedSlot = true;
        } else if (BookingStatus.COMPLETED.equals(status)) {
            booking.complete();
        } else {
            booking.setStatus(status);
        }

        Booking saved = bookingRepository.save(booking);

        if (BookingStatus.APPROVED.equals(status)) {
            notificationService.sendBookingApprovedNotification(saved);
        } else if (BookingStatus.REJECTED.equals(status)) {
            notificationService.sendBookingRejectedNotification(saved);
        } else if (BookingStatus.CANCELLED.equals(status)) {
            notificationService.sendBookingCancelledNotification(saved);
        } else if (BookingStatus.AUTO_CANCELLED.equals(status)) {
            notificationService.sendAutoCancelledNotification(saved);
        } else if (BookingStatus.CHECKED_IN.equals(status)) {
            notificationService.sendCheckInSuccessNotification(saved);
        }

        if (releasedSlot) {
            waitlistService.processReleasedBooking(saved);
        }

        return saved;
    }

    // =========================
    // AUTO STATUS UPDATE METHODS
    // =========================

    public int markCompletedBookings() {
        OffsetDateTime now = OffsetDateTime.now(APP_ZONE);

        List<Booking> bookings = bookingRepository.findBookingsToMarkCompleted(BookingStatus.CHECKED_IN, now);

        for (Booking booking : bookings) {
            booking.complete();
        }

        if (!bookings.isEmpty()) {
            bookingRepository.saveAll(bookings);
        }

        return bookings.size();
    }

    public int markNoShowBookings() {
        OffsetDateTime now = OffsetDateTime.now(APP_ZONE);

        List<Booking> bookings = bookingRepository.findBookingsToMarkNoShow(BookingStatus.APPROVED, now);

        for (Booking booking : bookings) {
            booking.markNoShow();
        }

        if (!bookings.isEmpty()) {
            bookingRepository.saveAll(bookings);
            for (Booking booking : bookings) {
                waitlistService.processReleasedBooking(booking);
            }
        }

        return bookings.size();
    }

    // =========================
    // RESCHEDULE
    // =========================

    public Booking rescheduleBooking(UUID bookingId, OffsetDateTime startAt, OffsetDateTime endAt) {
        if (startAt == null || endAt == null || !endAt.isAfter(startAt)) {
            throw new RuntimeException("Invalid reschedule time range");
        }

        if (!startAt.isAfter(OffsetDateTime.now(APP_ZONE))) {
            throw new RuntimeException("Start time must be in the future");
        }

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        OffsetDateTime oldStartAt = booking.getStartAt();
        OffsetDateTime oldEndAt = booking.getEndAt();
        UUID oldRoomId = booking.getRoom() != null ? booking.getRoom().getId() : null;

        if (BookingStatus.CANCELLED.equals(booking.getStatus())
                || BookingStatus.COMPLETED.equals(booking.getStatus())
                || BookingStatus.CHECKED_IN.equals(booking.getStatus())
                || BookingStatus.NO_SHOW.equals(booking.getStatus())
                || BookingStatus.AUTO_CANCELLED.equals(booking.getStatus())) {
            throw new RuntimeException("This booking cannot be rescheduled");
        }

        if (booking.getRoom() == null) {
            throw new RuntimeException("Booking room is missing");
        }

        UUID roomId = booking.getRoom().getId();

        timeSlotService.validateBookingSlot(roomId, startAt, endAt);
        validateRoomNotUnderMaintenance(roomId, startAt, endAt);

        validateRoomBookingAvailability(
                booking.getRoom(),
                startAt,
                endAt,
                booking.getUser() != null ? booking.getUser().getId() : null,
                booking.getAttendeeCount(),
                booking.getId()
        );

        booking.setStartAt(startAt);
        booking.setEndAt(endAt);
        booking.setReminderSent(false);
        booking.setCheckinStatus("not_checked_in");
        booking.setCheckedInAt(null);
        booking.setCheckInDeadline(null);
        booking.setIsPresent(false);
        booking.setAttendanceMarkedAt(null);
        booking.setAutoCancelledAt(null);

        if (Boolean.TRUE.equals(booking.getRoom().getApprovalRequired())) {
            booking.setStatus(BookingStatus.PENDING);
            booking.setApprovedBy(null);
            booking.setApprovalTime(null);
        } else {
            booking.setStatus(BookingStatus.APPROVED);
            booking.setApprovalTime(OffsetDateTime.now(APP_ZONE));
        }

        Booking saved = bookingRepository.save(booking);

        if (BookingStatus.PENDING.equals(saved.getStatus())) {
            notifyAdminsForNewBooking(saved);
        } else if (BookingStatus.APPROVED.equals(saved.getStatus())) {
            notificationService.sendBookingConfirmedNotification(saved);
        }

        if (oldRoomId != null && oldStartAt != null && oldEndAt != null) {
            waitlistService.processReleasedSlot(oldRoomId, oldStartAt, oldEndAt);
        }

        return saved;
    }

    // =========================
    // DELETE
    // =========================

    public void deleteBooking(UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        UUID roomId = booking.getRoom() != null ? booking.getRoom().getId() : null;
        OffsetDateTime startAt = booking.getStartAt();
        OffsetDateTime endAt = booking.getEndAt();

        bookingRepository.delete(booking);

        if (roomId != null && startAt != null && endAt != null) {
            waitlistService.processReleasedSlot(roomId, startAt, endAt);
        }
    }

    // =========================
    // INTERNAL HELPERS
    // =========================

    private void validateCreateBookingInputs(UUID roomId,
                                             UUID userId,
                                             OffsetDateTime startAt,
                                             OffsetDateTime endAt,
                                             Integer attendeeCount) {
        if (roomId == null) {
            throw new RuntimeException("Room ID is required");
        }

        if (userId == null) {
            throw new RuntimeException("User ID is required");
        }

        if (attendeeCount == null || attendeeCount <= 0) {
            throw new RuntimeException("Attendee count must be greater than 0");
        }

        if (startAt == null || endAt == null || !endAt.isAfter(startAt)) {
            throw new RuntimeException("Invalid booking time range");
        }

        if (!startAt.isAfter(OffsetDateTime.now(APP_ZONE))) {
            throw new RuntimeException("Start time must be in the future");
        }
    }

    private Booking buildNewBooking(StudyRoom room,
                               User user,
                               OffsetDateTime startAt,
                               OffsetDateTime endAt,
                               String purpose,
                               Integer attendeeCount) {

    Booking booking = new Booking();
    booking.setRoom(room);
    booking.setUser(user);
    booking.setStartAt(startAt);
    booking.setEndAt(endAt);
    booking.setPurpose(purpose);
    booking.setAttendeeCount(attendeeCount);
    booking.setReminderSent(false);
    booking.setCheckinStatus("not_checked_in");
    booking.setCheckedInAt(null);
    booking.setCheckInDeadline(null);
    booking.setAutoCancelledAt(null);
    booking.setIsPresent(false);
    booking.setAttendanceMarkedAt(null);
    booking.setFeedbackSubmitted(false);
    booking.setCancellationReason(null);
    booking.setQrToken(null);
    booking.setApprovedBy(null);
    booking.setApprovalTime(null);

    // 🔥 IMPORTANT LOGIC STARTS HERE

    // STAFF → Always auto approved
    if (user.getRole() != null && user.getRole().name().equals("STAFF")) {
        booking.setStatus(BookingStatus.APPROVED);
        booking.setApprovalTime(OffsetDateTime.now(APP_ZONE));
        booking.setApprovedBy(user);
        booking.setCheckInDeadline(startAt.plusMinutes(15));
    }

    // STUDENT / OTHERS → Follow normal rule
    else {
        if (Boolean.TRUE.equals(room.getApprovalRequired())) {
            booking.setStatus(BookingStatus.PENDING);
        } else {
            booking.setStatus(BookingStatus.APPROVED);
            booking.setApprovalTime(OffsetDateTime.now(APP_ZONE));
            booking.setApprovedBy(user);
            booking.setCheckInDeadline(startAt.plusMinutes(15));
        }
    }

    return booking;
}

    private void validateRoomBookingAvailability(
        StudyRoom room,
        OffsetDateTime startAt,
        OffsetDateTime endAt,
        UUID userId,
        Integer attendeeCount,
        UUID excludeBookingId
) {
    if (room == null) {
        throw new RuntimeException("Room is required");
    }

    if (startAt == null || endAt == null || !endAt.isAfter(startAt)) {
        throw new RuntimeException("Invalid booking time range");
    }

    if (attendeeCount == null || attendeeCount <= 0) {
        throw new RuntimeException("Attendee count must be greater than 0");
    }

    // 🔍 Fetch user (needed for STAFF priority)
    User user = null;
    if (userId != null) {
        user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // Check if user already has an overlapping booking
    if (userId != null) {
        boolean alreadyBookedBySameUser;

        if (excludeBookingId == null) {
            alreadyBookedBySameUser = bookingRepository
                    .findByUser_IdAndStartAtBetweenAndStatusIn(
                            userId,
                            startAt.minusDays(1),
                            endAt.plusDays(1),
                            ACTIVE_STATUSES
                    )
                    .stream()
                    .anyMatch(existing ->
                            existing.getStartAt() != null &&
                            existing.getEndAt() != null &&
                            existing.getStartAt().isBefore(endAt) &&
                            existing.getEndAt().isAfter(startAt)
                    );
        } else {
            alreadyBookedBySameUser = bookingRepository
                    .existsByUser_IdAndStatusInAndStartAtLessThanAndEndAtGreaterThanAndIdNot(
                            userId,
                            ACTIVE_STATUSES,
                            endAt,
                            startAt,
                            excludeBookingId
                    );
        }

        if (alreadyBookedBySameUser) {
            throw new RuntimeException("You already have another booking during this time");
        }
    }

    // Calculate already booked seats for this room & time
    int alreadyBookedCount = bookingRepository
            .findByRoom_IdAndStartAtLessThanAndEndAtGreaterThanAndStatusIn(
                    room.getId(),
                    endAt,
                    startAt,
                    ACTIVE_STATUSES
            )
            .stream()
            .filter(existing -> excludeBookingId == null || !excludeBookingId.equals(existing.getId()))
            .map(Booking::getAttendeeCount)
            .filter(count -> count != null)
            .mapToInt(Integer::intValue)
            .sum();

    int capacity = room.getSeatingCapacity() != null ? room.getSeatingCapacity() : 0;
    int remainingSeats = capacity - alreadyBookedCount;

    // =========================
    // 🔥 WAITLIST & STAFF PRIORITY
    // =========================

    // STAFF bypass capacity checks
    if (user != null && user.getRole() != null && user.getRole().name().equals("STAFF")) {
        return;
    }

    // If room full or not enough seats → add to waitlist
    if (remainingSeats <= 0 || attendeeCount > remainingSeats) {
        waitlistService.addToWaitlist(room, userId, startAt, endAt);
        throw new RuntimeException("Room full / insufficient seats. You have been added to the waitlist.");
    }
}

    private void validateRoomNotUnderMaintenance(UUID roomId, OffsetDateTime startAt, OffsetDateTime endAt) {
        if (roomId == null || startAt == null || endAt == null) {
            throw new RuntimeException("Room and booking time are required for maintenance validation");
        }

        boolean underMaintenance = maintenanceService.isRoomUnderMaintenance(roomId, startAt, endAt);

        if (underMaintenance) {
            throw new RuntimeException("Selected room is under maintenance for the chosen dates");
        }
    }

    private boolean isHistoryBooking(Booking booking) {
        if (booking == null || booking.getStatus() == null) {
            return false;
        }

        if (BookingStatus.COMPLETED.equals(booking.getStatus())
                || BookingStatus.CANCELLED.equals(booking.getStatus())
                || BookingStatus.REJECTED.equals(booking.getStatus())
                || BookingStatus.NO_SHOW.equals(booking.getStatus())
                || BookingStatus.AUTO_CANCELLED.equals(booking.getStatus())) {
            return true;
        }

        OffsetDateTime now = OffsetDateTime.now(APP_ZONE);
        return booking.getEndAt() != null && !booking.getEndAt().isAfter(now);
    }

    private Comparator<Booking> byStartAtDesc() {
        return Comparator.comparing(
                Booking::getStartAt,
                Comparator.nullsLast(Comparator.reverseOrder())
        );
    }

    private void notifyAdminsForNewBooking(Booking booking) {
        if (booking == null) {
            return;
        }

        List<User> admins = userRepository.findAll()
                .stream()
                .filter(this::isAdminUser)
                .collect(Collectors.toList());

        if (!admins.isEmpty()) {
            notificationService.sendNewBookingRequestNotificationToAdmins(admins, booking);
        }
    }

    private boolean isAdminUser(User user) {
        return user != null && user.isAdmin();
    }

    public Booking checkInBooking(UUID bookingId, UUID userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        // logic for checking in
        booking.setCheckedInAt(OffsetDateTime.now());
        booking.setCheckinStatus("checked_in"); // use string instead of boolean
        booking.setIsPresent(true);
        
        return bookingRepository.save(booking);
    }

    @Transactional
    public void processSingleBooking(Booking booking) {

        // Auto cancel
        booking.autoCancel("Auto-cancelled due to no check-in");

        bookingRepository.save(booking);

        // Trigger waitlist
        waitlistService.processReleasedBooking(booking);
    }
}
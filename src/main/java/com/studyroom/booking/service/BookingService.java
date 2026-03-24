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
            MaintenanceService maintenanceService
    ) {
        this.bookingRepository = bookingRepository;
        this.studyRoomRepository = studyRoomRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.timeSlotService = timeSlotService;
        this.maintenanceService = maintenanceService;
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

        StudyRoom room = studyRoomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        timeSlotService.validateBookingSlot(roomId, startAt, endAt);
        validateRoomNotUnderMaintenance(room.getId(), startAt, endAt);
        validateRoomBookingAvailability(room, startAt, endAt, userId, attendeeCount, null);

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
        booking.setIsPresent(false);
        booking.setAttendanceMarkedAt(null);
        booking.setFeedbackSubmitted(false);
        booking.setCancellationReason(null);
        booking.setQrToken(null);
        booking.setApprovedBy(null);
        booking.setApprovalTime(null);

        if (Boolean.TRUE.equals(room.getApprovalRequired())) {
            booking.setStatus(BookingStatus.PENDING);
        } else {
            booking.setStatus(BookingStatus.APPROVED);
            booking.setApprovalTime(OffsetDateTime.now(APP_ZONE));
            booking.setApprovedBy(user);
        }

        Booking saved = bookingRepository.save(booking);

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
        if (roomId == null) {
            throw new RuntimeException("Room ID is required");
        }

        if (userId == null) {
            throw new RuntimeException("User ID is required");
        }

        if (timeSlotId == null) {
            throw new RuntimeException("Time slot ID is required");
        }

        if (attendeeCount == null || attendeeCount <= 0) {
            throw new RuntimeException("Attendee count must be greater than 0");
        }

        StudyRoom room = studyRoomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        TimeSlot slot = timeSlotService.getSlotById(timeSlotId);

        if (slot.getRoom() == null || !roomId.equals(slot.getRoom().getId())) {
            throw new RuntimeException("Selected slot does not belong to this room");
        }

        if (slot.getStartAt() == null || slot.getEndAt() == null || !slot.getEndAt().isAfter(slot.getStartAt())) {
            throw new RuntimeException("Invalid time slot range");
        }

        if (!slot.getStartAt().isAfter(OffsetDateTime.now(APP_ZONE))) {
            throw new RuntimeException("Start time must be in the future");
        }

        validateRoomNotUnderMaintenance(room.getId(), slot.getStartAt(), slot.getEndAt());
        validateRoomBookingAvailability(room, slot.getStartAt(), slot.getEndAt(), userId, attendeeCount, null);

        Booking booking = new Booking();
        booking.setRoom(room);
        booking.setUser(user);
        booking.setStartAt(slot.getStartAt());
        booking.setEndAt(slot.getEndAt());
        booking.setPurpose(purpose);
        booking.setAttendeeCount(attendeeCount);
        booking.setReminderSent(false);
        booking.setCheckinStatus("not_checked_in");
        booking.setCheckedInAt(null);
        booking.setIsPresent(false);
        booking.setAttendanceMarkedAt(null);
        booking.setFeedbackSubmitted(false);
        booking.setCancellationReason(null);
        booking.setQrToken(null);
        booking.setApprovedBy(null);
        booking.setApprovalTime(null);

        if (Boolean.TRUE.equals(room.getApprovalRequired())) {
            booking.setStatus(BookingStatus.PENDING);
        } else {
            booking.setStatus(BookingStatus.APPROVED);
            booking.setApprovalTime(OffsetDateTime.now(APP_ZONE));
            booking.setApprovedBy(user);
        }

        Booking saved = bookingRepository.save(booking);

        if (BookingStatus.PENDING.equals(saved.getStatus())) {
            notifyAdminsForNewBooking(saved);
        } else if (BookingStatus.APPROVED.equals(saved.getStatus())) {
            notificationService.sendBookingConfirmedNotification(saved);
        }

        return saved;
    }

    // =========================
    // CHECK-IN
    // =========================

    public Booking checkInBooking(UUID bookingId, UUID userId) {
        if (bookingId == null) {
            throw new RuntimeException("Booking ID is required");
        }

        if (userId == null) {
            throw new RuntimeException("User ID is required");
        }

        Booking booking = bookingRepository.findByIdAndUser_Id(bookingId, userId)
                .orElseThrow(() -> new RuntimeException("Booking not found for this user"));

        if (!BookingStatus.APPROVED.equals(booking.getStatus())) {
            throw new RuntimeException("Only approved bookings can be checked in");
        }

        if ("checked_in".equalsIgnoreCase(booking.getCheckinStatus())
                || BookingStatus.CHECKED_IN.equals(booking.getStatus())
                || booking.getCheckedInAt() != null) {
            throw new RuntimeException("Booking already checked in");
        }

        OffsetDateTime now = OffsetDateTime.now(APP_ZONE);

        if (!isWithinCheckInWindow(booking, now)) {
            OffsetDateTime allowedFrom = booking.getStartAt().minusMinutes(CHECKIN_EARLY_MINUTES);
            OffsetDateTime allowedUntil = booking.getStartAt().plusMinutes(CHECKIN_LATE_MINUTES);
            throw new RuntimeException(
                    "Check-in allowed only from " + allowedFrom + " to " + allowedUntil
            );
        }

        booking.setCheckinStatus("checked_in");
        booking.setCheckedInAt(now);
        booking.setIsPresent(true);
        booking.setAttendanceMarkedAt(now);
        booking.setStatus(BookingStatus.CHECKED_IN);

        return bookingRepository.save(booking);
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

        booking.setStatus(BookingStatus.APPROVED);
        booking.setApprovedBy(approver);
        booking.setApprovalTime(OffsetDateTime.now(APP_ZONE));
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

        booking.setStatus(BookingStatus.REJECTED);

        Booking saved = bookingRepository.save(booking);
        notificationService.sendBookingRejectedNotification(saved);

        return saved;
    }

    public Booking cancelBooking(UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (BookingStatus.COMPLETED.equals(booking.getStatus())
                || BookingStatus.NO_SHOW.equals(booking.getStatus())) {
            throw new RuntimeException("This booking cannot be cancelled");
        }

        booking.setStatus(BookingStatus.CANCELLED);

        if (booking.getCancellationReason() == null || booking.getCancellationReason().isBlank()) {
            booking.setCancellationReason("Cancelled by user/admin");
        }

        Booking saved = bookingRepository.save(booking);
        notificationService.sendBookingCancelledNotification(saved);

        return saved;
    }

    public Booking completeBooking(UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        booking.setStatus(BookingStatus.COMPLETED);

        return bookingRepository.save(booking);
    }

    public Booking updateBookingStatus(UUID bookingId, BookingStatus status) {
        if (status == null) {
            throw new RuntimeException("Status is required");
        }

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        booking.setStatus(status);

        if (BookingStatus.APPROVED.equals(status) && booking.getApprovalTime() == null) {
            booking.setApprovalTime(OffsetDateTime.now(APP_ZONE));
            booking.setReminderSent(false);
        }

        if (BookingStatus.CHECKED_IN.equals(status)) {
            OffsetDateTime now = OffsetDateTime.now(APP_ZONE);
            booking.setCheckinStatus("checked_in");
            if (booking.getCheckedInAt() == null) {
                booking.setCheckedInAt(now);
            }
            booking.setIsPresent(true);
            if (booking.getAttendanceMarkedAt() == null) {
                booking.setAttendanceMarkedAt(now);
            }
        }

        if (BookingStatus.NO_SHOW.equals(status)) {
            booking.setCheckinStatus("not_checked_in");
            if (booking.getIsPresent() == null) {
                booking.setIsPresent(false);
            }
        }

        if (BookingStatus.CANCELLED.equals(status)
                && (booking.getCancellationReason() == null || booking.getCancellationReason().isBlank())) {
            booking.setCancellationReason("Status updated to cancelled");
        }

        Booking saved = bookingRepository.save(booking);

        if (BookingStatus.APPROVED.equals(status)) {
            notificationService.sendBookingApprovedNotification(saved);
        } else if (BookingStatus.REJECTED.equals(status)) {
            notificationService.sendBookingRejectedNotification(saved);
        } else if (BookingStatus.CANCELLED.equals(status)) {
            notificationService.sendBookingCancelledNotification(saved);
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
            booking.setStatus(BookingStatus.COMPLETED);
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
            booking.setStatus(BookingStatus.NO_SHOW);
            booking.setCheckinStatus("not_checked_in");
            booking.setIsPresent(false);
        }

        if (!bookings.isEmpty()) {
            bookingRepository.saveAll(bookings);
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
        booking.setIsPresent(false);
        booking.setAttendanceMarkedAt(null);

        if (Boolean.TRUE.equals(booking.getRoom().getApprovalRequired())) {
            booking.setStatus(BookingStatus.PENDING);
            booking.setApprovedBy(null);
            booking.setApprovalTime(null);
        } else {
            booking.setStatus(BookingStatus.APPROVED);
        }

        Booking saved = bookingRepository.save(booking);

        if (BookingStatus.PENDING.equals(saved.getStatus())) {
            notifyAdminsForNewBooking(saved);
        } else if (BookingStatus.APPROVED.equals(saved.getStatus())) {
            notificationService.sendBookingConfirmedNotification(saved);
        }

        return saved;
    }

    // =========================
    // DELETE
    // =========================

    public void deleteBooking(UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        bookingRepository.delete(booking);
    }

    // =========================
    // INTERNAL HELPERS
    // =========================

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

        if (userId != null && excludeBookingId == null) {
            boolean alreadyBookedBySameUser = bookingRepository
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

            if (alreadyBookedBySameUser) {
                throw new RuntimeException("You already have another booking during this time");
            }
        }

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
        int remaining = capacity - alreadyBookedCount;

        if (remaining <= 0) {
            throw new RuntimeException("Selected time is full");
        }

        if (attendeeCount > remaining) {
            throw new RuntimeException("Only " + remaining + " seats available");
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
}
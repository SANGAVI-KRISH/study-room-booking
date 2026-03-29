package com.studyroom.booking.service;

import com.studyroom.booking.model.Booking;
import com.studyroom.booking.model.BookingStatus;
import com.studyroom.booking.model.StudyRoom;
import com.studyroom.booking.model.User;
import com.studyroom.booking.model.Waitlist;
import com.studyroom.booking.model.WaitlistStatus;
import com.studyroom.booking.repository.BookingRepository;
import com.studyroom.booking.repository.StudyRoomRepository;
import com.studyroom.booking.repository.UserRepository;
import com.studyroom.booking.repository.WaitlistRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class WaitlistService {

    private static final long DEFAULT_RESPONSE_WINDOW_MINUTES = 10;

    private final WaitlistRepository waitlistRepository;
    private final BookingRepository bookingRepository;
    private final StudyRoomRepository studyRoomRepository;
    private final UserRepository userRepository;

    public WaitlistService(WaitlistRepository waitlistRepository,
                           BookingRepository bookingRepository,
                           StudyRoomRepository studyRoomRepository,
                           UserRepository userRepository) {
        this.waitlistRepository = waitlistRepository;
        this.bookingRepository = bookingRepository;
        this.studyRoomRepository = studyRoomRepository;
        this.userRepository = userRepository;
    }

    // =========================
    // NEW METHOD TO FIX addToWaitlist
    // =========================
    public Waitlist addToWaitlist(StudyRoom room, UUID userId, OffsetDateTime startAt, OffsetDateTime endAt) {
        if (room == null || userId == null || startAt == null || endAt == null) {
            throw new RuntimeException("Room, user, and slot times are required for waitlist");
        }
        // AutoAssign = false by default
        return joinWaitlist(userId, room.getId(), startAt, endAt, false);
    }

    // ================= JOIN WAITLIST =================

    public Waitlist joinWaitlist(UUID userId,
                                 UUID roomId,
                                 OffsetDateTime startAt,
                                 OffsetDateTime endAt,
                                 Boolean autoAssign) {

        validateSlot(startAt, endAt);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        StudyRoom room = studyRoomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Study room not found"));

        boolean alreadyBooked = bookingRepository.existsByUser_IdAndStatusInAndStartAtLessThanAndEndAtGreaterThan(
                userId,
                getActiveBookingStatuses(),
                endAt,
                startAt
        );

        if (alreadyBooked) {
            throw new RuntimeException("You already have a booking for an overlapping time slot");
        }

        boolean slotStillBlocked = bookingRepository.existsByRoom_IdAndStatusInAndStartAtLessThanAndEndAtGreaterThan(
                roomId,
                getBlockingStatuses(),
                endAt,
                startAt
        );

        if (!slotStillBlocked) {
            throw new RuntimeException("This slot is available now. Please book directly instead of joining waitlist");
        }

        boolean alreadyInWaitlist = waitlistRepository
                .existsByUser_IdAndRoom_IdAndStartAtAndEndAtAndStatusIn(
                        userId,
                        roomId,
                        startAt,
                        endAt,
                        getActiveWaitlistStatuses()
                );

        if (alreadyInWaitlist) {
            throw new RuntimeException("You are already in the waitlist for this slot");
        }

        int nextPosition = waitlistRepository.countByRoom_IdAndStartAtAndEndAtAndStatusIn(
                roomId,
                startAt,
                endAt,
                getActiveWaitlistStatuses()
        ) + 1;

        Waitlist waitlist = new Waitlist();
        waitlist.setUser(user);
        waitlist.setRoom(room);
        waitlist.setStartAt(startAt);
        waitlist.setEndAt(endAt);
        waitlist.setAutoAssign(autoAssign != null && autoAssign);
        waitlist.setStatus(WaitlistStatus.WAITING);
        waitlist.setPositionNumber(nextPosition);

        return waitlistRepository.save(waitlist);
    }

    // ================= FETCH METHODS =================

    public List<Waitlist> getAllByUser(UUID userId) {
        return waitlistRepository.findByUser_IdOrderByCreatedAtDesc(userId);
    }

    public List<Waitlist> getActiveByUser(UUID userId) {
        return waitlistRepository.findByUser_IdAndStatusInOrderByCreatedAtDesc(
                userId,
                getActiveWaitlistStatuses()
        );
    }

    public List<Waitlist> getSlotWaitlist(UUID roomId, OffsetDateTime startAt, OffsetDateTime endAt) {
        return waitlistRepository.findByRoom_IdAndStartAtAndEndAtAndStatusInOrderByCreatedAtAsc(
                roomId,
                startAt,
                endAt,
                getActiveWaitlistStatuses()
        );
    }

    public int getWaitlistPosition(UUID userId, UUID roomId, OffsetDateTime startAt, OffsetDateTime endAt) {
        List<Waitlist> entries = waitlistRepository.findByRoom_IdAndStartAtAndEndAtAndStatusInOrderByCreatedAtAsc(
                roomId,
                startAt,
                endAt,
                getActiveWaitlistStatuses()
        );

        for (int i = 0; i < entries.size(); i++) {
            Waitlist entry = entries.get(i);
            if (entry.getUser() != null && userId.equals(entry.getUser().getId())) {
                return i + 1;
            }
        }

        return -1;
    }

    public Waitlist getById(UUID waitlistId) {
        return waitlistRepository.findById(waitlistId)
                .orElseThrow(() -> new RuntimeException("Waitlist entry not found"));
    }

    // ================= CANCEL WAITLIST =================

    public Waitlist cancelWaitlist(UUID waitlistId, UUID userId) {
        Waitlist waitlist = waitlistRepository.findById(waitlistId)
                .orElseThrow(() -> new RuntimeException("Waitlist entry not found"));

        if (waitlist.getUser() == null || !userId.equals(waitlist.getUser().getId())) {
            throw new RuntimeException("You are not allowed to cancel this waitlist entry");
        }

        if (waitlist.isAssigned()) {
            throw new RuntimeException("Assigned waitlist entry cannot be cancelled");
        }

        if (waitlist.isCancelled() || waitlist.isExpired()) {
            return waitlist;
        }

        waitlist.cancel("Cancelled by user");
        Waitlist saved = waitlistRepository.save(waitlist);

        refreshPositions(waitlist.getRoom().getId(), waitlist.getStartAt(), waitlist.getEndAt());
        return saved;
    }

    // ================= PROCESS WAITLIST =================

    public void processReleasedSlot(UUID roomId, OffsetDateTime startAt, OffsetDateTime endAt) {
        validateSlot(startAt, endAt);

        boolean stillBlocked = bookingRepository.existsByRoom_IdAndStatusInAndStartAtLessThanAndEndAtGreaterThan(
                roomId,
                getBlockingStatuses(),
                endAt,
                startAt
        );

        if (stillBlocked) return;

        List<Waitlist> queue = waitlistRepository.findByRoom_IdAndStartAtAndEndAtAndStatusInOrderByCreatedAtAsc(
                roomId,
                startAt,
                endAt,
                getActiveWaitlistStatuses()
        );

        queue.removeIf(entry -> entry.isCancelled() || entry.isExpired());

        if (queue.isEmpty()) return;

        Waitlist nextEntry = queue.get(0);

        if (Boolean.TRUE.equals(nextEntry.getAutoAssign())) {
            assignBookingFromWaitlist(nextEntry);
        } else {
            nextEntry.markNotified(DEFAULT_RESPONSE_WINDOW_MINUTES);
            waitlistRepository.save(nextEntry);
        }

        refreshPositions(roomId, startAt, endAt);
    }

    public void processReleasedBooking(Booking booking) {
        if (booking == null || booking.getRoom() == null) return;

        processReleasedSlot(
                booking.getRoom().getId(),
                booking.getStartAt(),
                booking.getEndAt()
        );
    }

    public void processExpiredNotifications() {
        List<Waitlist> notifiedEntries = waitlistRepository.findByStatusOrderByCreatedAtAsc(WaitlistStatus.NOTIFIED);

        for (Waitlist entry : notifiedEntries) {
            if (entry.hasExpired()) {
                entry.markExpired();
                waitlistRepository.save(entry);

                processReleasedSlot(
                        entry.getRoom().getId(),
                        entry.getStartAt(),
                        entry.getEndAt()
                );
            }
        }
    }

    public Booking confirmNotifiedWaitlist(UUID waitlistId, UUID userId) {
        Waitlist waitlist = waitlistRepository.findById(waitlistId)
                .orElseThrow(() -> new RuntimeException("Waitlist entry not found"));

        if (waitlist.getUser() == null || !userId.equals(waitlist.getUser().getId())) {
            throw new RuntimeException("You are not allowed to confirm this waitlist entry");
        }

        if (!waitlist.isNotified()) {
            throw new RuntimeException("This waitlist entry is not currently notified");
        }

        if (waitlist.hasExpired()) {
            waitlist.markExpired();
            waitlistRepository.save(waitlist);
            throw new RuntimeException("Waitlist confirmation window has expired");
        }

        return assignBookingFromWaitlist(waitlist);
    }

    // ================= INTERNAL BOOKING ASSIGNMENT =================

    public Booking assignBookingFromWaitlist(Waitlist waitlist) {
        if (waitlist == null) throw new RuntimeException("Waitlist entry is required");

        UUID roomId = waitlist.getRoom().getId();
        OffsetDateTime startAt = waitlist.getStartAt();
        OffsetDateTime endAt = waitlist.getEndAt();

        boolean roomConflict = bookingRepository.existsByRoom_IdAndStatusInAndStartAtLessThanAndEndAtGreaterThan(
                roomId,
                getBlockingStatuses(),
                endAt,
                startAt
        );

        if (roomConflict) {
            throw new RuntimeException("Slot is no longer available for waitlist assignment");
        }

        boolean userConflict = bookingRepository.existsByUser_IdAndStatusInAndStartAtLessThanAndEndAtGreaterThan(
                waitlist.getUser().getId(),
                getActiveBookingStatuses(),
                endAt,
                startAt
        );

        if (userConflict) {
            waitlist.cancel("Waitlist auto-assignment skipped due to user booking conflict");
            waitlistRepository.save(waitlist);
            refreshPositions(roomId, startAt, endAt);
            throw new RuntimeException("User has another overlapping booking");
        }

        Booking booking = new Booking();
        booking.setRoom(waitlist.getRoom());
        booking.setUser(waitlist.getUser());
        booking.setStartAt(startAt);
        booking.setEndAt(endAt);
        booking.setPurpose("Assigned from waitlist");
        booking.setAttendeeCount(1);
        booking.setStatus(BookingStatus.APPROVED);
        booking.setCheckinStatus("not_checked_in");
        booking.setApprovalTime(OffsetDateTime.now());

        Booking savedBooking = bookingRepository.save(booking);

        waitlist.markAssigned();
        waitlistRepository.save(waitlist);

        refreshPositions(roomId, startAt, endAt);
        return savedBooking;
    }

    // ================= POSITION REFRESH =================

    public void refreshPositions(UUID roomId, OffsetDateTime startAt, OffsetDateTime endAt) {
        List<Waitlist> activeEntries = waitlistRepository.findByRoom_IdAndStartAtAndEndAtAndStatusInOrderByCreatedAtAsc(
                roomId,
                startAt,
                endAt,
                getActiveWaitlistStatuses()
        );

        int position = 1;
        for (Waitlist entry : activeEntries) {
            entry.setPositionNumber(position++);
        }

        if (!activeEntries.isEmpty()) {
            waitlistRepository.saveAll(activeEntries);
        }
    }

    // ================= HELPERS =================

    // ================= HELPERS =================

    private void validateSlot(OffsetDateTime startAt, OffsetDateTime endAt) {
        if (startAt == null || endAt == null) {
            throw new RuntimeException("Start time and end time are required");
        }

        if (!endAt.isAfter(startAt)) {
            throw new RuntimeException("End time must be after start time");
        }

        if (startAt.isBefore(OffsetDateTime.now())) {
            throw new RuntimeException("Cannot join waitlist for a past time slot");
        }
    }

    private List<BookingStatus> getBlockingStatuses() {
        return List.of(
                BookingStatus.PENDING,
                BookingStatus.APPROVED,
                BookingStatus.CHECKED_IN
        );
    }

    private List<BookingStatus> getActiveBookingStatuses() {
        return List.of(
                BookingStatus.PENDING,
                BookingStatus.APPROVED,
                BookingStatus.CHECKED_IN
        );
    }

    private List<WaitlistStatus> getActiveWaitlistStatuses() {
        return List.of(
                WaitlistStatus.WAITING,
                WaitlistStatus.NOTIFIED
        );
    }
}
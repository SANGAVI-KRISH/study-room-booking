package com.studyroom.booking.repository;

import com.studyroom.booking.model.Booking;
import com.studyroom.booking.model.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID> {

    // ================= BASIC FIND METHODS =================

    List<Booking> findByRoom_Id(UUID roomId);

    List<Booking> findByRoom_IdOrderByStartAtAsc(UUID roomId);

    List<Booking> findByRoom_IdAndStatus(UUID roomId, BookingStatus status);

    List<Booking> findByRoom_IdAndStatusIn(UUID roomId, List<BookingStatus> statuses);

    List<Booking> findByRoom_IdAndStatusInOrderByStartAtAsc(UUID roomId, List<BookingStatus> statuses);

    List<Booking> findByUser_Id(UUID userId);

    List<Booking> findByUser_IdOrderByStartAtDesc(UUID userId);

    List<Booking> findByUser_IdAndStatus(UUID userId, BookingStatus status);

    List<Booking> findByUser_IdAndStatusIn(UUID userId, List<BookingStatus> statuses);

    List<Booking> findByStatus(BookingStatus status);

    List<Booking> findByStatusIn(List<BookingStatus> statuses);

    Optional<Booking> findByIdAndUser_Id(UUID bookingId, UUID userId);

    // ================= REMINDER / UPCOMING =================

    @Query("""
        SELECT b
        FROM Booking b
        WHERE b.status = :status
          AND (b.reminderSent = false OR b.reminderSent IS NULL)
        ORDER BY b.startAt ASC
    """)
    List<Booking> findPendingReminderBookings(BookingStatus status);

    List<Booking> findByStartAtBetween(OffsetDateTime start, OffsetDateTime end);

    List<Booking> findByStatusAndStartAtBetween(
            BookingStatus status,
            OffsetDateTime start,
            OffsetDateTime end
    );

    List<Booking> findByStartAtBetweenAndStatusIn(
            OffsetDateTime start,
            OffsetDateTime end,
            List<BookingStatus> statuses
    );

    List<Booking> findByRoom_IdAndStartAtBetween(
            UUID roomId,
            OffsetDateTime start,
            OffsetDateTime end
    );

    List<Booking> findByRoom_IdAndStartAtBetweenAndStatusIn(
            UUID roomId,
            OffsetDateTime start,
            OffsetDateTime end,
            List<BookingStatus> statuses
    );

    List<Booking> findByUser_IdAndStartAtBetween(
            UUID userId,
            OffsetDateTime start,
            OffsetDateTime end
    );

    List<Booking> findByUser_IdAndStartAtBetweenAndStatusIn(
            UUID userId,
            OffsetDateTime start,
            OffsetDateTime end,
            List<BookingStatus> statuses
    );

    // ================= CONFLICT / OVERLAP CHECKS =================

    List<Booking> findByRoom_IdAndStartAtLessThanAndEndAtGreaterThanAndStatusIn(
            UUID roomId,
            OffsetDateTime endAt,
            OffsetDateTime startAt,
            List<BookingStatus> statuses
    );

    List<Booking> findByUser_IdAndStartAtLessThanAndEndAtGreaterThanAndStatusIn(
            UUID userId,
            OffsetDateTime endAt,
            OffsetDateTime startAt,
            List<BookingStatus> statuses
    );

    boolean existsByRoom_Id(UUID roomId);

    boolean existsByRoom_IdAndStatusIn(UUID roomId, List<BookingStatus> statuses);

    boolean existsByRoom_IdAndStatusInAndStartAtLessThanAndEndAtGreaterThan(
            UUID roomId,
            List<BookingStatus> statuses,
            OffsetDateTime endAt,
            OffsetDateTime startAt
    );

    boolean existsByUser_IdAndStatusInAndStartAtLessThanAndEndAtGreaterThan(
            UUID userId,
            List<BookingStatus> statuses,
            OffsetDateTime endAt,
            OffsetDateTime startAt
    );

    boolean existsByRoom_IdAndStatusInAndStartAtLessThanAndEndAtGreaterThanAndIdNot(
            UUID roomId,
            List<BookingStatus> statuses,
            OffsetDateTime endAt,
            OffsetDateTime startAt,
            UUID bookingId
    );

    boolean existsByUser_IdAndStatusInAndStartAtLessThanAndEndAtGreaterThanAndIdNot(
            UUID userId,
            List<BookingStatus> statuses,
            OffsetDateTime endAt,
            OffsetDateTime startAt,
            UUID bookingId
    );

    // ================= CHECK-IN / AUTO-CANCELLATION =================

    List<Booking> findByStatusAndStartAtLessThanEqualAndStartAtGreaterThanEqual(
            BookingStatus status,
            OffsetDateTime latestAllowedStart,
            OffsetDateTime earliestAllowedStart
    );

    List<Booking> findByStatusAndEndAtBefore(BookingStatus status, OffsetDateTime time);

    List<Booking> findByStatusInAndEndAtBefore(List<BookingStatus> statuses, OffsetDateTime time);

    @Query("""
        SELECT b
        FROM Booking b
        WHERE b.status = :status
          AND b.checkInDeadline IS NOT NULL
          AND b.checkInDeadline <= :now
          AND (b.checkinStatus = 'not_checked_in' OR b.checkinStatus IS NULL)
        ORDER BY b.checkInDeadline ASC
    """)
    List<Booking> findBookingsEligibleForAutoCancellation(BookingStatus status, OffsetDateTime now);

    @Query("""
        SELECT b
        FROM Booking b
        WHERE b.status = :status
          AND b.startAt <= :now
          AND b.endAt > :now
        ORDER BY b.startAt ASC
    """)
    List<Booking> findOngoingBookingsByStatus(BookingStatus status, OffsetDateTime now);

    @Query("""
        SELECT b
        FROM Booking b
        WHERE b.status IN :statuses
          AND b.startAt <= :now
          AND b.endAt > :now
        ORDER BY b.startAt ASC
    """)
    List<Booking> findOngoingBookingsByStatuses(List<BookingStatus> statuses, OffsetDateTime now);

    @Query("""
        SELECT b
        FROM Booking b
        WHERE b.room.id = :roomId
          AND b.status IN :statuses
          AND b.startAt <= :now
          AND b.endAt > :now
        ORDER BY b.startAt ASC
    """)
    List<Booking> findCurrentRoomBookings(UUID roomId, List<BookingStatus> statuses, OffsetDateTime now);

    // ================= ATTENDANCE / COMPLETION / FEEDBACK =================

    List<Booking> findByStatusAndFeedbackSubmittedFalse(BookingStatus status);

    List<Booking> findByUser_IdAndStatusAndFeedbackSubmittedFalse(UUID userId, BookingStatus status);

    @Query("""
        SELECT b
        FROM Booking b
        WHERE b.status = :status
          AND b.endAt <= :now
          AND (b.isPresent = true OR b.checkinStatus = 'checked_in')
        ORDER BY b.endAt ASC
    """)
    List<Booking> findBookingsToMarkCompleted(BookingStatus status, OffsetDateTime now);

    @Query("""
        SELECT b
        FROM Booking b
        WHERE b.status = :status
          AND b.endAt <= :now
          AND (b.isPresent = false OR b.isPresent IS NULL)
          AND (b.checkinStatus = 'not_checked_in' OR b.checkinStatus IS NULL)
        ORDER BY b.endAt ASC
    """)
    List<Booking> findBookingsToMarkNoShow(BookingStatus status, OffsetDateTime now);

    @Query("""
        SELECT b
        FROM Booking b
        WHERE b.user.id = :userId
          AND b.status = :status
          AND (b.feedbackSubmitted = false OR b.feedbackSubmitted IS NULL)
        ORDER BY b.endAt DESC
    """)
    List<Booking> findPendingFeedbackBookings(UUID userId, BookingStatus status);

    @Query("""
        SELECT b
        FROM Booking b
        WHERE b.user.id = :userId
          AND b.status IN :statuses
        ORDER BY b.startAt DESC
    """)
    List<Booking> findUserBookingsByStatusesOrderByStartAtDesc(UUID userId, List<BookingStatus> statuses);

    @Query("""
        SELECT b
        FROM Booking b
        WHERE b.user.id = :userId
          AND b.endAt < :now
        ORDER BY b.endAt DESC
    """)
    List<Booking> findPastBookings(UUID userId, OffsetDateTime now);

    @Query("""
        SELECT b
        FROM Booking b
        WHERE b.user.id = :userId
          AND b.endAt >= :now
        ORDER BY b.startAt ASC
    """)
    List<Booking> findCurrentAndUpcomingBookings(UUID userId, OffsetDateTime now);

    // ================= WAITLIST SUPPORT =================

    @Query("""
        SELECT b
        FROM Booking b
        WHERE b.room.id = :roomId
          AND b.status IN :statuses
          AND b.startAt < :endAt
          AND b.endAt > :startAt
        ORDER BY b.startAt ASC
    """)
    List<Booking> findConflictingBookingsForSlot(
            UUID roomId,
            OffsetDateTime startAt,
            OffsetDateTime endAt,
            List<BookingStatus> statuses
    );

    @Query("""
        SELECT COUNT(b) > 0
        FROM Booking b
        WHERE b.room.id = :roomId
          AND b.status IN :statuses
          AND b.startAt < :endAt
          AND b.endAt > :startAt
    """)
    boolean hasConflictingBookingsForSlot(
            UUID roomId,
            OffsetDateTime startAt,
            OffsetDateTime endAt,
            List<BookingStatus> statuses
    );

    // ================= COUNT METHODS =================

    long countByStatus(BookingStatus status);

    long countByStatusIn(List<BookingStatus> statuses);

    long countByStartAtBetween(OffsetDateTime start, OffsetDateTime end);

    long countByStatusAndStartAtBetween(
            BookingStatus status,
            OffsetDateTime start,
            OffsetDateTime end
    );

    long countByRoom_Id(UUID roomId);

    // ================= DASHBOARD / ANALYTICS =================

    @Query("""
        SELECT b.room.displayName, COUNT(b)
        FROM Booking b
        GROUP BY b.room.displayName
        ORDER BY COUNT(b) DESC
    """)
    List<Object[]> findMostBookedRoom();

    @Query(value = """
        SELECT EXTRACT(HOUR FROM start_at) AS booking_hour, COUNT(id) AS booking_count
        FROM bookings
        GROUP BY EXTRACT(HOUR FROM start_at)
        ORDER BY booking_count DESC
    """, nativeQuery = true)
    List<Object[]> findPeakBookingHours();

    @Query("""
        SELECT b.room.displayName, COUNT(b)
        FROM Booking b
        GROUP BY b.room.displayName
        ORDER BY COUNT(b) DESC
    """)
    List<Object[]> findRoomUsageTrend();

    // ================= REPORT METHODS =================

    @Query(value = """
        SELECT
            COALESCE(sr.display_name, 'Unknown Room') AS room_name,
            COUNT(b.id) AS booking_count,
            COALESCE(SUM(EXTRACT(EPOCH FROM (b.end_at - b.start_at)) / 3600), 0) AS total_hours
        FROM bookings b
        LEFT JOIN rooms sr ON b.room_id = sr.id
        GROUP BY sr.display_name
        ORDER BY booking_count DESC
    """, nativeQuery = true)
    List<Object[]> findRoomUtilizationReport();

    @Query("""
        SELECT
            COALESCE(b.room.displayName, 'Unknown Room'),
            COUNT(b)
        FROM Booking b
        GROUP BY b.room.displayName
        ORDER BY COUNT(b) DESC
    """)
    List<Object[]> findFrequentlyUsedRooms();

    @Query("""
        SELECT
            COALESCE(b.user.name, 'Unknown User'),
            COALESCE(b.user.email, 'No Email'),
            COUNT(b)
        FROM Booking b
        GROUP BY b.user.name, b.user.email
        ORDER BY COUNT(b) DESC
    """)
    List<Object[]> findUserActivityReport();

    @Query("""
        SELECT b FROM Booking b
        WHERE b.status = :status
        AND b.checkInDeadline < :now
        AND b.checkedInAt IS NULL
    """)
    List<Booking> findBookingsEligibleForAutoCancellation(
            @Param("status") BookingStatus status,
            @Param("now") LocalDateTime now
    );
}
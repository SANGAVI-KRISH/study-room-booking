package com.studyroom.booking.repository;

import com.studyroom.booking.model.Booking;
import com.studyroom.booking.model.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
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

    List<Booking> findByStatus(BookingStatus status);

    List<Booking> findByStatusIn(List<BookingStatus> statuses);

    List<Booking> findByStatusAndReminderSentFalse(BookingStatus status);

    List<Booking> findByUser_IdAndStatus(UUID userId, BookingStatus status);

    List<Booking> findByUser_IdAndStatusIn(UUID userId, List<BookingStatus> statuses);

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
}
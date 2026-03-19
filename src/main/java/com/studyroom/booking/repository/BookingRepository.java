package com.studyroom.booking.repository;

import com.studyroom.booking.model.Booking;
import com.studyroom.booking.model.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID> {

    List<Booking> findByRoom_Id(UUID roomId);

    List<Booking> findByRoom_IdOrderByStartAtAsc(UUID roomId);

    List<Booking> findByUser_Id(UUID userId);

    List<Booking> findByUser_IdOrderByStartAtDesc(UUID userId);

    List<Booking> findByStatus(BookingStatus status);

    List<Booking> findByStatusIn(List<BookingStatus> statuses);

    List<Booking> findByRoom_IdAndStatus(UUID roomId, BookingStatus status);

    List<Booking> findByRoom_IdAndStatusIn(UUID roomId, List<BookingStatus> statuses);

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

    List<Booking> findByStartAtBefore(OffsetDateTime dateTime);

    List<Booking> findByEndAtBefore(OffsetDateTime dateTime);

    List<Booking> findByStartAtGreaterThanEqual(OffsetDateTime dateTime);

    List<Booking> findByUser_IdAndStartAtBefore(UUID userId, OffsetDateTime dateTime);

    List<Booking> findByUser_IdAndStartAtBeforeOrderByStartAtDesc(UUID userId, OffsetDateTime dateTime);

    List<Booking> findByUser_IdAndStartAtGreaterThanEqual(UUID userId, OffsetDateTime dateTime);

    List<Booking> findByUser_IdAndStartAtGreaterThanEqualOrderByStartAtAsc(UUID userId, OffsetDateTime dateTime);

    List<Booking> findByUser_IdAndStartAtGreaterThanEqualAndStatusIn(
            UUID userId,
            OffsetDateTime dateTime,
            List<BookingStatus> statuses
    );

    List<Booking> findByUser_IdAndStartAtGreaterThanEqualAndStatusInOrderByStartAtAsc(
            UUID userId,
            OffsetDateTime dateTime,
            List<BookingStatus> statuses
    );

    List<Booking> findByUser_IdAndStartAtBeforeAndStatusIn(
            UUID userId,
            OffsetDateTime dateTime,
            List<BookingStatus> statuses
    );

    List<Booking> findByUser_IdAndStartAtBeforeAndStatusInOrderByStartAtDesc(
            UUID userId,
            OffsetDateTime dateTime,
            List<BookingStatus> statuses
    );

    boolean existsByRoom_IdAndStatusInAndStartAtLessThanAndEndAtGreaterThan(
            UUID roomId,
            List<BookingStatus> statuses,
            OffsetDateTime endAt,
            OffsetDateTime startAt
    );

    boolean existsByRoom_IdAndStatusInAndStartAtLessThanAndEndAtGreaterThanAndIdNot(
            UUID roomId,
            List<BookingStatus> statuses,
            OffsetDateTime endAt,
            OffsetDateTime startAt,
            UUID id
    );

    boolean existsByUser_IdAndStatusInAndStartAtLessThanAndEndAtGreaterThan(
            UUID userId,
            List<BookingStatus> statuses,
            OffsetDateTime endAt,
            OffsetDateTime startAt
    );

    boolean existsByUser_IdAndStatusInAndStartAtLessThanAndEndAtGreaterThanAndIdNot(
            UUID userId,
            List<BookingStatus> statuses,
            OffsetDateTime endAt,
            OffsetDateTime startAt,
            UUID id
    );
}
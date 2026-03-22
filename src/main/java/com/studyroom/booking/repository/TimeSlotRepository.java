package com.studyroom.booking.repository;

import com.studyroom.booking.model.TimeSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TimeSlotRepository extends JpaRepository<TimeSlot, UUID> {

    Optional<TimeSlot> findByRoom_IdAndStartAtAndEndAt(
            UUID roomId,
            OffsetDateTime startAt,
            OffsetDateTime endAt
    );

    List<TimeSlot> findByRoom_Id(UUID roomId);

    List<TimeSlot> findByRoom_IdOrderByStartAtAsc(UUID roomId);

    List<TimeSlot> findByIsActiveTrue();

    List<TimeSlot> findByRoom_IdAndIsActiveTrue(UUID roomId);

    List<TimeSlot> findByStartAtBetween(OffsetDateTime startAt, OffsetDateTime endAt);

    List<TimeSlot> findByRoom_IdAndStartAtBetween(
            UUID roomId,
            OffsetDateTime startAt,
            OffsetDateTime endAt
    );

    List<TimeSlot> findByRoom_IdAndIsActiveTrueOrderByStartAtAsc(UUID roomId);
}
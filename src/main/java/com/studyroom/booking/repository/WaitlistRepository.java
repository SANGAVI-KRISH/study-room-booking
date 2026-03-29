package com.studyroom.booking.repository;

import com.studyroom.booking.model.Waitlist;
import com.studyroom.booking.model.WaitlistStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface WaitlistRepository extends JpaRepository<Waitlist, UUID> {

    // ================= BASIC =================

    List<Waitlist> findByUser_IdOrderByCreatedAtDesc(UUID userId);

    List<Waitlist> findByUser_IdAndStatusInOrderByCreatedAtDesc(
            UUID userId,
            List<WaitlistStatus> statuses
    );

    List<Waitlist> findByStatusOrderByCreatedAtAsc(WaitlistStatus status);

    // ================= SLOT BASED =================

    List<Waitlist> findByRoom_IdAndStartAtAndEndAtAndStatusInOrderByCreatedAtAsc(
            UUID roomId,
            OffsetDateTime startAt,
            OffsetDateTime endAt,
            List<WaitlistStatus> statuses
    );

    List<Waitlist> findByRoom_IdAndStartAtAndEndAtOrderByCreatedAtAsc(
            UUID roomId,
            OffsetDateTime startAt,
            OffsetDateTime endAt
    );

    // ================= EXISTENCE =================

    boolean existsByUser_IdAndRoom_IdAndStartAtAndEndAtAndStatusIn(
            UUID userId,
            UUID roomId,
            OffsetDateTime startAt,
            OffsetDateTime endAt,
            List<WaitlistStatus> statuses
    );

    // ================= COUNT =================

    int countByRoom_IdAndStartAtAndEndAtAndStatusIn(
            UUID roomId,
            OffsetDateTime startAt,
            OffsetDateTime endAt,
            List<WaitlistStatus> statuses
    );

    // ================= POSITION SUPPORT =================

    List<Waitlist> findByRoom_IdAndStartAtAndEndAtAndStatusOrderByCreatedAtAsc(
            UUID roomId,
            OffsetDateTime startAt,
            OffsetDateTime endAt,
            WaitlistStatus status
    );

    // ================= CLEANUP / EXPIRE =================

    List<Waitlist> findByStatus(WaitlistStatus status);
}
package com.studyroom.booking.repository;

import com.studyroom.booking.model.MaintenanceBlock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface MaintenanceBlockRepository extends JpaRepository<MaintenanceBlock, UUID> {

    List<MaintenanceBlock> findByRoom_IdAndStartAtLessThanAndEndAtGreaterThanAndStatus(
            UUID roomId,
            OffsetDateTime endAt,
            OffsetDateTime startAt,
            String status
    );
}
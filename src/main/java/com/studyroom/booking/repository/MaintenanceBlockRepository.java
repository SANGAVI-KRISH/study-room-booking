package com.studyroom.booking.repository;

import com.studyroom.booking.model.MaintenanceBlock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface MaintenanceBlockRepository extends JpaRepository<MaintenanceBlock, UUID> {

    List<MaintenanceBlock> findByRoom_IdAndStartAtLessThanAndEndAtGreaterThan(
            UUID roomId,
            OffsetDateTime endAt,
            OffsetDateTime startAt
    );
}
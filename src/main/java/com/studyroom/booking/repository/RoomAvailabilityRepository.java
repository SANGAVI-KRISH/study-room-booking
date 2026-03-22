package com.studyroom.booking.repository;

import com.studyroom.booking.model.RoomAvailability;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RoomAvailabilityRepository extends JpaRepository<RoomAvailability, UUID> {

    List<RoomAvailability> findAllByRoom_IdAndDayOfWeek(UUID roomId, Integer dayOfWeek);

    List<RoomAvailability> findByRoom_Id(UUID roomId);
}
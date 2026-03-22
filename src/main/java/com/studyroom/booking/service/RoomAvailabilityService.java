package com.studyroom.booking.service;

import com.studyroom.booking.model.BookingStatus;
import com.studyroom.booking.model.StudyRoom;
import com.studyroom.booking.repository.BookingRepository;
import com.studyroom.booking.repository.StudyRoomRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
public class RoomAvailabilityService {

    private final StudyRoomRepository studyRoomRepository;
    private final BookingRepository bookingRepository;

    private static final ZoneId APP_ZONE = ZoneId.of("Asia/Kolkata");

    private static final List<BookingStatus> BLOCKING_STATUSES =
            List.of(BookingStatus.PENDING, BookingStatus.APPROVED);

    public RoomAvailabilityService(
            StudyRoomRepository studyRoomRepository,
            BookingRepository bookingRepository
    ) {
        this.studyRoomRepository = studyRoomRepository;
        this.bookingRepository = bookingRepository;
    }

    public List<StudyRoom> getAvailableRooms(
            LocalDate date,
            LocalTime startTime,
            LocalTime endTime,
            Integer seatingCapacity,
            String district,
            String location,
            String facility
    ) {
        if (date == null) {
            throw new RuntimeException("Date is required");
        }

        if (startTime == null || endTime == null) {
            throw new RuntimeException("Start time and end time are required");
        }

        OffsetDateTime startAt = date.atTime(startTime)
                .atZone(APP_ZONE)
                .toOffsetDateTime();

        OffsetDateTime endAt;

        // Normal same-day case
        if (endTime.isAfter(startTime)) {
            endAt = date.atTime(endTime)
                    .atZone(APP_ZONE)
                    .toOffsetDateTime();
        }
        // Cross-midnight case: e.g. 16:00 -> 01:00 next day
        else if (endTime.isBefore(startTime)) {
            endAt = date.plusDays(1)
                    .atTime(endTime)
                    .atZone(APP_ZONE)
                    .toOffsetDateTime();
        }
        // Same start and end not allowed
        else {
            throw new RuntimeException("Start time and end time cannot be the same");
        }

        if (!startAt.isAfter(OffsetDateTime.now(APP_ZONE))) {
            throw new RuntimeException("Start time must be in the future");
        }

        List<StudyRoom> rooms = studyRoomRepository.findAll();

        return rooms.stream()
                .filter(room -> !bookingRepository
                        .existsByRoom_IdAndStatusInAndStartAtLessThanAndEndAtGreaterThan(
                                room.getId(),
                                BLOCKING_STATUSES,
                                endAt,
                                startAt
                        ))
                .filter(room ->
                        seatingCapacity == null ||
                        (room.getSeatingCapacity() != null &&
                         room.getSeatingCapacity() >= seatingCapacity))
                .filter(room ->
                        district == null || district.isBlank() ||
                        (room.getDistrict() != null &&
                         room.getDistrict().equalsIgnoreCase(district)))
                .filter(room ->
                        location == null || location.isBlank() ||
                        (room.getLocation() != null &&
                         room.getLocation().toLowerCase().contains(location.toLowerCase())))
                .filter(room ->
                        facility == null || facility.isBlank() ||
                        (room.getFacilities() != null &&
                         room.getFacilities().toLowerCase().contains(facility.toLowerCase())))
                .toList();
    }
}
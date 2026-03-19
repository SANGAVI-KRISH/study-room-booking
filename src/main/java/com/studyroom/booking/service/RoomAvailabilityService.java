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

        if (!endTime.isAfter(startTime)) {
            throw new RuntimeException("End time must be after start time");
        }

        OffsetDateTime startAt = date.atTime(startTime).atZone(APP_ZONE).toOffsetDateTime();
        OffsetDateTime endAt = date.atTime(endTime).atZone(APP_ZONE).toOffsetDateTime();

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
                        (room.getSeatingCapacity() != null && room.getSeatingCapacity() >= seatingCapacity))
                .filter(room ->
                        district == null || district.isBlank() ||
                        (room.getDistrict() != null && room.getDistrict().equalsIgnoreCase(district)))
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
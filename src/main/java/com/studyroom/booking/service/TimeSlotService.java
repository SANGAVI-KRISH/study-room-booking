package com.studyroom.booking.service;

import com.studyroom.booking.dto.AvailableSlotsResponse;
import com.studyroom.booking.dto.MaintenanceBlockRequest;
import com.studyroom.booking.dto.RoomAvailabilityRequest;
import com.studyroom.booking.dto.SlotItem;
import com.studyroom.booking.model.Booking;
import com.studyroom.booking.model.BookingStatus;
import com.studyroom.booking.model.MaintenanceBlock;
import com.studyroom.booking.model.RoomAvailability;
import com.studyroom.booking.model.StudyRoom;
import com.studyroom.booking.model.User;
import com.studyroom.booking.repository.BookingRepository;
import com.studyroom.booking.repository.MaintenanceBlockRepository;
import com.studyroom.booking.repository.RoomAvailabilityRepository;
import com.studyroom.booking.repository.StudyRoomRepository;
import com.studyroom.booking.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class TimeSlotService {

    private final RoomAvailabilityRepository roomAvailabilityRepository;
    private final MaintenanceBlockRepository maintenanceBlockRepository;
    private final BookingRepository bookingRepository;
    private final StudyRoomRepository studyRoomRepository;
    private final UserRepository userRepository;

    public TimeSlotService(
            RoomAvailabilityRepository roomAvailabilityRepository,
            MaintenanceBlockRepository maintenanceBlockRepository,
            BookingRepository bookingRepository,
            StudyRoomRepository studyRoomRepository,
            UserRepository userRepository
    ) {
        this.roomAvailabilityRepository = roomAvailabilityRepository;
        this.maintenanceBlockRepository = maintenanceBlockRepository;
        this.bookingRepository = bookingRepository;
        this.studyRoomRepository = studyRoomRepository;
        this.userRepository = userRepository;
    }

    public RoomAvailability saveRoomAvailability(UUID roomId, RoomAvailabilityRequest request) {
        StudyRoom room = studyRoomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        if (request.getDayOfWeek() == null || request.getDayOfWeek() < 0 || request.getDayOfWeek() > 6) {
            throw new RuntimeException("dayOfWeek must be between 0 and 6");
        }

        if (request.getOpenTime() == null || request.getCloseTime() == null) {
            throw new RuntimeException("Open time and close time are required");
        }

        if (!request.getOpenTime().isBefore(request.getCloseTime())) {
            throw new RuntimeException("Open time must be before close time");
        }

        if (request.getSlotDurationMins() == null || request.getSlotDurationMins() <= 0) {
            throw new RuntimeException("slotDurationMins must be greater than 0");
        }

        RoomAvailability availability = roomAvailabilityRepository
                .findByRoom_IdAndDayOfWeek(roomId, request.getDayOfWeek())
                .orElse(new RoomAvailability());

        availability.setRoom(room);
        availability.setDayOfWeek(request.getDayOfWeek());
        availability.setOpenTime(request.getOpenTime());
        availability.setCloseTime(request.getCloseTime());
        availability.setSlotDurationMins(request.getSlotDurationMins());
        availability.setIsAvailable(request.getIsAvailable() != null ? request.getIsAvailable() : true);

        return roomAvailabilityRepository.save(availability);
    }

    public MaintenanceBlock addMaintenanceBlock(UUID roomId, MaintenanceBlockRequest request) {
        StudyRoom room = studyRoomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        if (request.getStartAt() == null || request.getEndAt() == null) {
            throw new RuntimeException("Start time and end time are required");
        }

        if (!request.getStartAt().isBefore(request.getEndAt())) {
            throw new RuntimeException("startAt must be before endAt");
        }

        MaintenanceBlock block = new MaintenanceBlock();
        block.setRoom(room);
        block.setStartAt(request.getStartAt());
        block.setEndAt(request.getEndAt());
        block.setReason(request.getReason());
        block.setStatus("blocked");

        if (request.getCreatedBy() != null) {
            User user = userRepository.findById(request.getCreatedBy())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            block.setCreatedBy(user);
        }

        return maintenanceBlockRepository.save(block);
    }

    public AvailableSlotsResponse getAvailableSlots(UUID roomId, LocalDate date) {
        int dayOfWeek = mapJavaDayToDb(date.getDayOfWeek().getValue());

        RoomAvailability availability = roomAvailabilityRepository
                .findByRoom_IdAndDayOfWeek(roomId, dayOfWeek)
                .orElseThrow(() -> new RuntimeException("Room availability not configured for this day"));

        if (Boolean.FALSE.equals(availability.getIsAvailable())) {
            return new AvailableSlotsResponse(
                    roomId,
                    date,
                    dayOfWeek,
                    availability.getSlotDurationMins(),
                    new ArrayList<>()
            );
        }

        List<SlotItem> slots = generateSlots(
                roomId,
                date,
                availability.getOpenTime(),
                availability.getCloseTime(),
                availability.getSlotDurationMins()
        );

        return new AvailableSlotsResponse(
                roomId,
                date,
                dayOfWeek,
                availability.getSlotDurationMins(),
                slots
        );
    }

    private List<SlotItem> generateSlots(
            UUID roomId,
            LocalDate date,
            LocalTime openTime,
            LocalTime closeTime,
            int slotDurationMins
    ) {
        List<SlotItem> slots = new ArrayList<>();

        ZoneId zone = ZoneId.of("Asia/Kolkata");
        OffsetDateTime dayStart = date.atStartOfDay(zone).toOffsetDateTime();
        OffsetDateTime dayEnd = date.plusDays(1).atStartOfDay(zone).toOffsetDateTime();

        List<MaintenanceBlock> blockedPeriods =
                maintenanceBlockRepository.findByRoom_IdAndStartAtLessThanAndEndAtGreaterThanAndStatus(
                        roomId, dayEnd, dayStart, "blocked"
                );

        List<BookingStatus> activeStatuses = List.of(
                BookingStatus.PENDING,
                BookingStatus.APPROVED,
                BookingStatus.COMPLETED
        );

        List<Booking> activeBookings =
                bookingRepository.findByRoom_IdAndStartAtLessThanAndEndAtGreaterThanAndStatusIn(
                        roomId,
                        dayEnd,
                        dayStart,
                        activeStatuses
                );

        LocalTime current = openTime;

        while (!current.plusMinutes(slotDurationMins).isAfter(closeTime)) {
            LocalTime slotEnd = current.plusMinutes(slotDurationMins);

            OffsetDateTime slotStartAt = date.atTime(current).atZone(zone).toOffsetDateTime();
            OffsetDateTime slotEndAt = date.atTime(slotEnd).atZone(zone).toOffsetDateTime();

            boolean blocked = blockedPeriods.stream().anyMatch(block ->
                    overlaps(slotStartAt, slotEndAt, block.getStartAt(), block.getEndAt())
            );

            boolean booked = activeBookings.stream().anyMatch(booking ->
                    overlaps(slotStartAt, slotEndAt, booking.getStartAt(), booking.getEndAt())
            );

            slots.add(new SlotItem(current, slotEnd, !blocked && !booked));

            current = slotEnd;
        }

        return slots;
    }

    public void validateBookingSlot(UUID roomId, OffsetDateTime startAt, OffsetDateTime endAt) {
        if (startAt == null || endAt == null) {
            throw new RuntimeException("Start time and end time are required");
        }

        if (!startAt.isBefore(endAt)) {
            throw new RuntimeException("Start time must be before end time");
        }

        if (startAt.isBefore(OffsetDateTime.now())) {
            throw new RuntimeException("Cannot book past time slot");
        }

        LocalDate bookingDate = startAt.toLocalDate();
        AvailableSlotsResponse response = getAvailableSlots(roomId, bookingDate);

        boolean valid = response.getSlots().stream().anyMatch(slot ->
                slot.isAvailable()
                        && slot.getStartTime().equals(startAt.toLocalTime())
                        && slot.getEndTime().equals(endAt.toLocalTime())
        );

        if (!valid) {
            throw new RuntimeException("Selected slot is not available");
        }
    }

    private boolean overlaps(
            OffsetDateTime start1,
            OffsetDateTime end1,
            OffsetDateTime start2,
            OffsetDateTime end2
    ) {
        return start1.isBefore(end2) && end1.isAfter(start2);
    }

    private int mapJavaDayToDb(int javaDay) {
        return javaDay % 7;
    }
}
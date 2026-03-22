package com.studyroom.booking.service;

import com.studyroom.booking.dto.AvailableSlotsResponse;
import com.studyroom.booking.dto.MaintenanceBlockRequest;
import com.studyroom.booking.dto.RoomAvailabilityRequest;
import com.studyroom.booking.dto.SlotItem;
import com.studyroom.booking.dto.TimeSlotResponse;
import com.studyroom.booking.model.Booking;
import com.studyroom.booking.model.BookingStatus;
import com.studyroom.booking.model.MaintenanceBlock;
import com.studyroom.booking.model.RoomAvailability;
import com.studyroom.booking.model.StudyRoom;
import com.studyroom.booking.model.TimeSlot;
import com.studyroom.booking.repository.BookingRepository;
import com.studyroom.booking.repository.MaintenanceBlockRepository;
import com.studyroom.booking.repository.RoomAvailabilityRepository;
import com.studyroom.booking.repository.StudyRoomRepository;
import com.studyroom.booking.repository.TimeSlotRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class TimeSlotService {

    private static final ZoneId APP_ZONE = ZoneId.of("Asia/Kolkata");
    private static final int DEFAULT_SLOT_DURATION_MINS = 60;
    private static final int BOOKING_STEP_MINS = 30;

    private final RoomAvailabilityRepository roomAvailabilityRepository;
    private final MaintenanceBlockRepository maintenanceBlockRepository;
    private final BookingRepository bookingRepository;
    private final StudyRoomRepository studyRoomRepository;
    private final TimeSlotRepository timeSlotRepository;

    public TimeSlotService(
            RoomAvailabilityRepository roomAvailabilityRepository,
            MaintenanceBlockRepository maintenanceBlockRepository,
            BookingRepository bookingRepository,
            StudyRoomRepository studyRoomRepository,
            TimeSlotRepository timeSlotRepository
    ) {
        this.roomAvailabilityRepository = roomAvailabilityRepository;
        this.maintenanceBlockRepository = maintenanceBlockRepository;
        this.bookingRepository = bookingRepository;
        this.studyRoomRepository = studyRoomRepository;
        this.timeSlotRepository = timeSlotRepository;
    }

    // ======================================================
    // ROOM AVAILABILITY
    // ======================================================

    public RoomAvailability saveRoomAvailability(UUID roomId, RoomAvailabilityRequest request) {
        if (roomId == null) {
            throw new RuntimeException("Room ID is required");
        }

        if (request == null) {
            throw new RuntimeException("Room availability request is required");
        }

        StudyRoom room = studyRoomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        if (request.getDayOfWeek() == null || request.getDayOfWeek() < 0 || request.getDayOfWeek() > 6) {
            throw new RuntimeException("dayOfWeek must be between 0 and 6");
        }

        if (request.getOpenTime() == null || request.getCloseTime() == null) {
            throw new RuntimeException("Open time and close time are required");
        }

        if (request.getOpenTime().equals(request.getCloseTime())) {
            throw new RuntimeException("Open time and close time cannot be the same");
        }

        int slotDurationMins = normalizeSlotDuration(request.getSlotDurationMins());

        List<RoomAvailability> existingList =
                roomAvailabilityRepository.findAllByRoom_IdAndDayOfWeek(roomId, request.getDayOfWeek());

        RoomAvailability availability;
        if (existingList != null && !existingList.isEmpty()) {
            availability = existingList.get(0);
        } else {
            availability = new RoomAvailability();
            availability.setRoom(room);
            availability.setDayOfWeek(request.getDayOfWeek());
        }

        availability.setOpenTime(request.getOpenTime());
        availability.setCloseTime(request.getCloseTime());
        availability.setSlotDurationMins(slotDurationMins);
        availability.setIsAvailable(request.getIsAvailable() != null ? request.getIsAvailable() : true);

        return roomAvailabilityRepository.save(availability);
    }

    // ======================================================
    // MAINTENANCE BLOCK
    // ======================================================

    public MaintenanceBlock addMaintenanceBlock(UUID roomId, MaintenanceBlockRequest request) {
        if (roomId == null) {
            throw new RuntimeException("Room ID is required");
        }

        if (request == null) {
            throw new RuntimeException("Maintenance block request is required");
        }

        StudyRoom room = studyRoomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        if (request.getStartAt() == null || request.getEndAt() == null) {
            throw new RuntimeException("Start time and end time are required");
        }

        if (request.getStartAt().isAfter(request.getEndAt())) {
            throw new RuntimeException("startAt must be before or equal to endAt");
        }

        MaintenanceBlock block = new MaintenanceBlock();
        block.setRoom(room);
        block.setStartAt(request.getStartAt());
        block.setEndAt(request.getEndAt());
        block.setReason(request.getReason());
        block.setStatus("ACTIVE");
        block.setCreatedBy(request.getCreatedBy());

        return maintenanceBlockRepository.save(block);
    }

    // ======================================================
    // AVAILABLE WINDOWS FOR A DATE
    // ======================================================

    @Transactional(readOnly = true)
    public AvailableSlotsResponse getAvailableSlots(UUID roomId, LocalDate date) {
        if (roomId == null) {
            throw new RuntimeException("Room ID is required");
        }

        if (date == null) {
            throw new RuntimeException("Date is required");
        }

        StudyRoom room = studyRoomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        List<SlotWindow> slotWindows = collectSlotWindowsForRequestedDate(room, date);
        Integer slotDurationMins = resolveSlotDurationForDate(roomId, date);

        List<SlotItem> slots = slotWindows.stream()
                .sorted(Comparator.comparing(SlotWindow::getStartAt))
                .map(window -> new SlotItem(
                        window.getStartAt().toLocalTime(),
                        window.getEndAt().toLocalTime(),
                        window.isAvailable()
                ))
                .collect(Collectors.toList());

        return new AvailableSlotsResponse(
                roomId,
                date,
                mapJavaDayToDb(date.getDayOfWeek().getValue()),
                slotDurationMins,
                slots
        );
    }

    private List<SlotWindow> collectSlotWindowsForRequestedDate(StudyRoom room, LocalDate requestedDate) {
        int requestedDayOfWeek = mapJavaDayToDb(requestedDate.getDayOfWeek().getValue());
        int previousDayOfWeek = (requestedDayOfWeek + 6) % 7;

        List<RoomAvailability> requestedDayAvailabilities =
                roomAvailabilityRepository.findAllByRoom_IdAndDayOfWeek(room.getId(), requestedDayOfWeek);

        List<RoomAvailability> previousDayAvailabilities =
                roomAvailabilityRepository.findAllByRoom_IdAndDayOfWeek(room.getId(), previousDayOfWeek);

        List<SlotWindow> slotWindows = new ArrayList<>();

        for (RoomAvailability availability : requestedDayAvailabilities) {
            if (Boolean.FALSE.equals(availability.getIsAvailable())) {
                continue;
            }

            SlotWindow window = generateAvailabilityWindow(
                    room,
                    requestedDate,
                    availability.getOpenTime(),
                    availability.getCloseTime()
            );

            if (window != null) {
                slotWindows.add(window);
            }
        }

        LocalDate previousDate = requestedDate.minusDays(1);

        for (RoomAvailability availability : previousDayAvailabilities) {
            if (Boolean.FALSE.equals(availability.getIsAvailable())) {
                continue;
            }

            if (!crossesMidnight(availability.getOpenTime(), availability.getCloseTime())) {
                continue;
            }

            SlotWindow previousDayWindow = generateAvailabilityWindow(
                    room,
                    previousDate,
                    availability.getOpenTime(),
                    availability.getCloseTime()
            );

            if (previousDayWindow == null) {
                continue;
            }

            OffsetDateTime clippedStart = max(
                    previousDayWindow.getStartAt(),
                    requestedDate.atStartOfDay(APP_ZONE).toOffsetDateTime()
            );

            OffsetDateTime clippedEnd = min(
                    previousDayWindow.getEndAt(),
                    requestedDate.plusDays(1).atStartOfDay(APP_ZONE).toOffsetDateTime()
            );

            if (clippedStart.isBefore(clippedEnd)) {
                int bookedSeats = calculateBookedSeats(room.getId(), clippedStart, clippedEnd);
                boolean blocked = isBlocked(room.getId(), clippedStart, clippedEnd);
                int capacity = resolveEffectiveCapacity(null, room);
                int remainingSeats = Math.max(capacity - bookedSeats, 0);
                boolean available = !blocked && remainingSeats > 0;

                slotWindows.add(new SlotWindow(
                        clippedStart,
                        clippedEnd,
                        available,
                        blocked,
                        bookedSeats,
                        capacity,
                        remainingSeats
                ));
            }
        }

        return mergeAdjacentWindows(slotWindows).stream()
                .sorted(Comparator.comparing(SlotWindow::getStartAt))
                .collect(Collectors.toList());
    }

    private SlotWindow generateAvailabilityWindow(
            StudyRoom room,
            LocalDate availabilityBaseDate,
            LocalTime openTime,
            LocalTime closeTime
    ) {
        if (room == null || openTime == null || closeTime == null) {
            return null;
        }

        OffsetDateTime rangeStart = availabilityBaseDate.atTime(openTime)
                .atZone(APP_ZONE)
                .toOffsetDateTime();

        LocalDate endDate = crossesMidnight(openTime, closeTime)
                ? availabilityBaseDate.plusDays(1)
                : availabilityBaseDate;

        OffsetDateTime rangeEnd = endDate.atTime(closeTime)
                .atZone(APP_ZONE)
                .toOffsetDateTime();

        if (!rangeEnd.isAfter(rangeStart)) {
            return null;
        }

        int bookedSeats = calculateBookedSeats(room.getId(), rangeStart, rangeEnd);
        boolean blocked = isBlocked(room.getId(), rangeStart, rangeEnd);
        int capacity = resolveEffectiveCapacity(null, room);
        int remainingSeats = Math.max(capacity - bookedSeats, 0);
        boolean available = !blocked && remainingSeats > 0;

        return new SlotWindow(
                rangeStart,
                rangeEnd,
                available,
                blocked,
                bookedSeats,
                capacity,
                remainingSeats
        );
    }

    private List<SlotWindow> mergeAdjacentWindows(List<SlotWindow> windows) {
        if (windows == null || windows.isEmpty()) {
            return List.of();
        }

        List<SlotWindow> sorted = windows.stream()
                .sorted(Comparator.comparing(SlotWindow::getStartAt))
                .collect(Collectors.toList());

        List<SlotWindow> merged = new ArrayList<>();
        SlotWindow current = sorted.get(0);

        for (int i = 1; i < sorted.size(); i++) {
            SlotWindow next = sorted.get(i);

            boolean sameState =
                    current.isAvailable() == next.isAvailable()
                            && current.isBlocked() == next.isBlocked()
                            && current.getCapacity() == next.getCapacity()
                            && current.getRemainingSeats() == next.getRemainingSeats();

            boolean touches = current.getEndAt().isEqual(next.getStartAt());

            if (sameState && touches) {
                current = new SlotWindow(
                        current.getStartAt(),
                        next.getEndAt(),
                        current.isAvailable(),
                        current.isBlocked(),
                        Math.max(current.getBookedCount(), next.getBookedCount()),
                        current.getCapacity(),
                        Math.min(current.getRemainingSeats(), next.getRemainingSeats())
                );
            } else {
                merged.add(current);
                current = next;
            }
        }

        merged.add(current);
        return merged;
    }

    private Integer resolveSlotDurationForDate(UUID roomId, LocalDate date) {
        int dayOfWeek = mapJavaDayToDb(date.getDayOfWeek().getValue());
        int previousDayOfWeek = (dayOfWeek + 6) % 7;

        List<RoomAvailability> todayAvailabilities =
                roomAvailabilityRepository.findAllByRoom_IdAndDayOfWeek(roomId, dayOfWeek);

        for (RoomAvailability availability : todayAvailabilities) {
            if (Boolean.FALSE.equals(availability.getIsAvailable())) {
                continue;
            }
            return normalizeSlotDuration(availability.getSlotDurationMins());
        }

        List<RoomAvailability> previousDayAvailabilities =
                roomAvailabilityRepository.findAllByRoom_IdAndDayOfWeek(roomId, previousDayOfWeek);

        for (RoomAvailability availability : previousDayAvailabilities) {
            if (Boolean.FALSE.equals(availability.getIsAvailable())) {
                continue;
            }
            if (crossesMidnight(availability.getOpenTime(), availability.getCloseTime())) {
                return normalizeSlotDuration(availability.getSlotDurationMins());
            }
        }

        return DEFAULT_SLOT_DURATION_MINS;
    }

    private boolean crossesMidnight(LocalTime openTime, LocalTime closeTime) {
        return !closeTime.isAfter(openTime);
    }

    private int normalizeSlotDuration(Integer slotDurationMins) {
        if (slotDurationMins == null || slotDurationMins <= 0) {
            return DEFAULT_SLOT_DURATION_MINS;
        }
        return slotDurationMins;
    }

    // ======================================================
    // TIME SLOT LISTING WITH REMAINING SEATS / STATUS
    // ======================================================

    @Transactional(readOnly = true)
    public List<TimeSlotResponse> getAllSlots() {
        return timeSlotRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(TimeSlot::getStartAt))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TimeSlotResponse> getSlotsByRoom(UUID roomId) {
        if (roomId == null) {
            throw new RuntimeException("Room ID is required");
        }

        studyRoomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        return timeSlotRepository.findByRoom_IdOrderByStartAtAsc(roomId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TimeSlotResponse> getActiveSlotsByRoom(UUID roomId) {
        if (roomId == null) {
            throw new RuntimeException("Room ID is required");
        }

        studyRoomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        return timeSlotRepository.findByRoom_IdAndIsActiveTrueOrderByStartAtAsc(roomId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private TimeSlotResponse mapToResponse(TimeSlot slot) {
        StudyRoom room = slot.getRoom();

        int effectiveCapacity = resolveEffectiveCapacity(slot, room);

        int bookedCount = bookingRepository
                .findByRoom_IdAndStartAtLessThanAndEndAtGreaterThanAndStatusIn(
                        room.getId(),
                        slot.getEndAt(),
                        slot.getStartAt(),
                        getActiveBookingStatuses()
                )
                .stream()
                .mapToInt(booking -> booking.getAttendeeCount() != null ? booking.getAttendeeCount() : 1)
                .sum();

        int remainingSeats = Math.max(effectiveCapacity - bookedCount, 0);

        String availabilityStatus;
        if (Boolean.FALSE.equals(slot.getIsActive())) {
            availabilityStatus = "INACTIVE";
        } else if (remainingSeats <= 0) {
            availabilityStatus = "FULL";
        } else {
            availabilityStatus = "AVAILABLE";
        }

        TimeSlotResponse response = new TimeSlotResponse();
        response.setId(slot.getId());
        response.setRoomId(room.getId());
        response.setRoomName(room.getDisplayName());
        response.setStartAt(slot.getStartAt());
        response.setEndAt(slot.getEndAt());
        response.setIsActive(slot.getIsActive());
        response.setCapacity(effectiveCapacity);
        response.setBookedCount(bookedCount);
        response.setRemainingSeats(remainingSeats);
        response.setAvailabilityStatus(availabilityStatus);

        double effectivePrice = 0.0;
        if (slot.getPrice() != null) {
            effectivePrice = slot.getPrice();
        } else if (room.getFeePerHour() != null) {
            effectivePrice = room.getFeePerHour().doubleValue();
        }
        response.setPrice(effectivePrice);

        return response;
    }

    private int resolveEffectiveCapacity(TimeSlot slot, StudyRoom room) {
        if (slot != null && slot.getCapacity() != null && slot.getCapacity() > 0) {
            return slot.getCapacity();
        }

        if (room != null && room.getSeatingCapacity() != null && room.getSeatingCapacity() > 0) {
            return room.getSeatingCapacity();
        }

        return 0;
    }

    private List<BookingStatus> getActiveBookingStatuses() {
        return List.of(
                BookingStatus.PENDING,
                BookingStatus.APPROVED
        );
    }

    // ======================================================
    // METHODS REQUIRED BY BOOKING SERVICE
    // ======================================================

    @Transactional(readOnly = true)
    public TimeSlot getSlotById(UUID timeSlotId) {
        if (timeSlotId == null) {
            throw new RuntimeException("Time slot ID is required");
        }

        return timeSlotRepository.findById(timeSlotId)
                .orElseThrow(() -> new RuntimeException("Time slot not found"));
    }

    @Transactional(readOnly = true)
    public TimeSlot getSlotByRoomAndTime(UUID roomId, OffsetDateTime startAt, OffsetDateTime endAt) {
        if (roomId == null) {
            throw new RuntimeException("Room ID is required");
        }

        if (startAt == null || endAt == null) {
            throw new RuntimeException("Start time and end time are required");
        }

        if (!endAt.isAfter(startAt)) {
            throw new RuntimeException("End time must be after start time");
        }

        validateBookingSlot(roomId, startAt, endAt);

        return timeSlotRepository.findByRoom_IdAndStartAtAndEndAt(roomId, startAt, endAt)
                .orElseThrow(() -> new RuntimeException("Matching time slot not found"));
    }

    public void validateBookingSlot(UUID roomId, OffsetDateTime startAt, OffsetDateTime endAt) {
        if (roomId == null) {
            throw new RuntimeException("Room ID is required");
        }

        if (startAt == null || endAt == null) {
            throw new RuntimeException("Start time and end time are required");
        }

        if (!startAt.isBefore(endAt)) {
            throw new RuntimeException("Start time must be before end time");
        }

        OffsetDateTime now = OffsetDateTime.now(APP_ZONE);
        if (!startAt.isAfter(now)) {
            throw new RuntimeException("Cannot book past time slot");
        }

        long minutes = java.time.Duration.between(startAt, endAt).toMinutes();
        if (minutes <= 0) {
            throw new RuntimeException("Invalid booking duration");
        }

        if (minutes % BOOKING_STEP_MINS != 0) {
            throw new RuntimeException("Booking time must be in 30 minute steps");
        }

        LocalDateTime localStart = startAt.atZoneSameInstant(APP_ZONE).toLocalDateTime();
        LocalDateTime localEnd = endAt.atZoneSameInstant(APP_ZONE).toLocalDateTime();

        if (localStart.getMinute() % BOOKING_STEP_MINS != 0 || localEnd.getMinute() % BOOKING_STEP_MINS != 0) {
            throw new RuntimeException("Start time and end time must be in 30 minute steps");
        }

        StudyRoom room = studyRoomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        boolean fitsAnyWindow = collectCandidateDates(startAt, endAt).stream()
                .flatMap(date -> collectSlotWindowsForRequestedDate(room, date).stream())
                .anyMatch(window ->
                        window.isAvailable()
                                && !window.isBlocked()
                                && !startAt.isBefore(window.getStartAt())
                                && !endAt.isAfter(window.getEndAt())
                );

        if (!fitsAnyWindow) {
            throw new RuntimeException("Selected time range is not within an available slot window");
        }

        boolean overlapsExistingBooking = bookingRepository
                .findByRoom_IdAndStartAtLessThanAndEndAtGreaterThanAndStatusIn(
                        roomId,
                        endAt,
                        startAt,
                        getActiveBookingStatuses()
                )
                .stream()
                .anyMatch(booking -> overlaps(startAt, endAt, booking.getStartAt(), booking.getEndAt()));

        if (overlapsExistingBooking) {
            throw new RuntimeException("Selected time overlaps with an existing booking");
        }

        boolean overlapsMaintenance = maintenanceBlockRepository
                .findByRoom_IdAndStartAtLessThanAndEndAtGreaterThan(
                        roomId,
                        endAt,
                        startAt
                )
                .stream()
                .filter(block -> "ACTIVE".equalsIgnoreCase(block.getStatus()))
                .anyMatch(block -> overlaps(startAt, endAt, block.getStartAt(), block.getEndAt()));

        if (overlapsMaintenance) {
            throw new RuntimeException("Selected time overlaps with maintenance block");
        }
    }

    private List<LocalDate> collectCandidateDates(OffsetDateTime startAt, OffsetDateTime endAt) {
        LocalDate startDate = startAt.atZoneSameInstant(APP_ZONE).toLocalDate();
        LocalDate endDate = endAt.atZoneSameInstant(APP_ZONE).toLocalDate();

        List<LocalDate> dates = new ArrayList<>();
        dates.add(startDate);

        if (!endDate.equals(startDate)) {
            dates.add(endDate);
        }

        LocalDate previousOfStart = startDate.minusDays(1);
        if (!dates.contains(previousOfStart)) {
            dates.add(previousOfStart);
        }

        return dates;
    }

    private int calculateBookedSeats(UUID roomId, OffsetDateTime rangeStart, OffsetDateTime rangeEnd) {
        return bookingRepository
                .findByRoom_IdAndStartAtLessThanAndEndAtGreaterThanAndStatusIn(
                        roomId,
                        rangeEnd,
                        rangeStart,
                        getActiveBookingStatuses()
                )
                .stream()
                .mapToInt(booking -> booking.getAttendeeCount() != null ? booking.getAttendeeCount() : 1)
                .sum();
    }

    private boolean isBlocked(UUID roomId, OffsetDateTime rangeStart, OffsetDateTime rangeEnd) {
        return maintenanceBlockRepository
                .findByRoom_IdAndStartAtLessThanAndEndAtGreaterThan(
                        roomId,
                        rangeEnd,
                        rangeStart
                )
                .stream()
                .filter(block -> "ACTIVE".equalsIgnoreCase(block.getStatus()))
                .anyMatch(block -> overlaps(rangeStart, rangeEnd, block.getStartAt(), block.getEndAt()));
    }

    private OffsetDateTime max(OffsetDateTime a, OffsetDateTime b) {
        return a.isAfter(b) ? a : b;
    }

    private OffsetDateTime min(OffsetDateTime a, OffsetDateTime b) {
        return a.isBefore(b) ? a : b;
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

    private static class SlotWindow {
        private final OffsetDateTime startAt;
        private final OffsetDateTime endAt;
        private final boolean available;
        private final boolean blocked;
        private final int bookedCount;
        private final int capacity;
        private final int remainingSeats;

        public SlotWindow(
                OffsetDateTime startAt,
                OffsetDateTime endAt,
                boolean available,
                boolean blocked,
                int bookedCount,
                int capacity,
                int remainingSeats
        ) {
            this.startAt = startAt;
            this.endAt = endAt;
            this.available = available;
            this.blocked = blocked;
            this.bookedCount = bookedCount;
            this.capacity = capacity;
            this.remainingSeats = remainingSeats;
        }

        public OffsetDateTime getStartAt() {
            return startAt;
        }

        public OffsetDateTime getEndAt() {
            return endAt;
        }

        public boolean isAvailable() {
            return available;
        }

        public boolean isBlocked() {
            return blocked;
        }

        public int getBookedCount() {
            return bookedCount;
        }

        public int getCapacity() {
            return capacity;
        }

        public int getRemainingSeats() {
            return remainingSeats;
        }
    }
}
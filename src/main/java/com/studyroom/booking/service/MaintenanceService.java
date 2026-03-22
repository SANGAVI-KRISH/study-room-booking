package com.studyroom.booking.service;

import com.studyroom.booking.dto.MaintenanceRequest;
import com.studyroom.booking.dto.MaintenanceResponse;
import com.studyroom.booking.model.MaintenanceBlock;
import com.studyroom.booking.model.StudyRoom;
import com.studyroom.booking.repository.MaintenanceBlockRepository;
import com.studyroom.booking.repository.StudyRoomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class MaintenanceService {

    private static final ZoneId APP_ZONE = ZoneId.of("Asia/Kolkata");

    private final MaintenanceBlockRepository maintenanceBlockRepository;
    private final StudyRoomRepository studyRoomRepository;

    public MaintenanceService(MaintenanceBlockRepository maintenanceBlockRepository,
                              StudyRoomRepository studyRoomRepository) {
        this.maintenanceBlockRepository = maintenanceBlockRepository;
        this.studyRoomRepository = studyRoomRepository;
    }

    /* ================= CREATE ================= */
    public MaintenanceResponse createMaintenanceBlock(MaintenanceRequest request) {
        if (request == null) {
            throw new RuntimeException("Maintenance request is required");
        }

        if (request.getRoomId() == null) {
            throw new RuntimeException("Room ID is required");
        }

        if (request.getFromDate() == null || request.getToDate() == null) {
            throw new RuntimeException("From date and To date are required");
        }

        if (request.getToDate().isBefore(request.getFromDate())) {
            throw new RuntimeException("To date cannot be before From date");
        }

        StudyRoom room = studyRoomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new RuntimeException("Room not found"));

        OffsetDateTime maintenanceStart = request.getFromDate()
                .atStartOfDay(APP_ZONE)
                .toOffsetDateTime();

        OffsetDateTime maintenanceEnd = request.getToDate()
                .plusDays(1)
                .atStartOfDay(APP_ZONE)
                .toOffsetDateTime();

        boolean overlapExists = !maintenanceBlockRepository
                .findByRoom_IdAndStartAtLessThanAndEndAtGreaterThan(
                        request.getRoomId(),
                        maintenanceEnd,
                        maintenanceStart
                )
                .isEmpty();

        if (overlapExists) {
            throw new RuntimeException("Maintenance already exists for this room in selected date range");
        }

        MaintenanceBlock block = new MaintenanceBlock();
        block.setRoom(room);
        block.setStartAt(maintenanceStart);
        block.setEndAt(maintenanceEnd);
        block.setReason(request.getReason());
        block.setStatus("ACTIVE");

        MaintenanceBlock saved = maintenanceBlockRepository.save(block);
        return mapToResponse(saved);
    }

    /* ================= GET ACTIVE / UPCOMING ================= */
    @Transactional(readOnly = true)
    public List<MaintenanceResponse> getActiveMaintenanceBlocks() {
        OffsetDateTime now = OffsetDateTime.now(APP_ZONE);

        List<MaintenanceBlock> blocks = maintenanceBlockRepository.findAll()
                .stream()
                .filter(block ->
                        block.getEndAt() != null &&
                        !block.getEndAt().isBefore(now) &&
                        "ACTIVE".equalsIgnoreCase(block.getStatus())
                )
                .sorted((a, b) -> a.getStartAt().compareTo(b.getStartAt()))
                .collect(Collectors.toList());

        return blocks.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /* ================= GET BY ID ================= */
    @Transactional(readOnly = true)
    public MaintenanceResponse getMaintenanceById(UUID maintenanceId) {
        if (maintenanceId == null) {
            throw new RuntimeException("Maintenance ID is required");
        }

        MaintenanceBlock block = maintenanceBlockRepository.findById(maintenanceId)
                .orElseThrow(() -> new RuntimeException("Maintenance block not found"));

        OffsetDateTime now = OffsetDateTime.now(APP_ZONE);

        if (!"ACTIVE".equalsIgnoreCase(block.getStatus()) || block.getEndAt().isBefore(now)) {
            throw new RuntimeException("Maintenance is already finished or inactive");
        }

        return mapToResponse(block);
    }

    /* ================= DELETE ================= */
    public void deleteMaintenanceBlock(UUID maintenanceId) {
        if (maintenanceId == null) {
            throw new RuntimeException("Maintenance ID is required");
        }

        MaintenanceBlock block = maintenanceBlockRepository.findById(maintenanceId)
                .orElseThrow(() -> new RuntimeException("Maintenance block not found"));

        maintenanceBlockRepository.delete(block);
    }

    /* ================= BOOKING VALIDATION ================= */
    @Transactional(readOnly = true)
    public boolean isRoomUnderMaintenance(UUID roomId, OffsetDateTime bookingStart, OffsetDateTime bookingEnd) {
        if (roomId == null || bookingStart == null || bookingEnd == null) {
            return false;
        }

        if (!bookingEnd.isAfter(bookingStart)) {
            return false;
        }

        return !maintenanceBlockRepository
                .findByRoom_IdAndStartAtLessThanAndEndAtGreaterThan(
                        roomId,
                        bookingEnd,
                        bookingStart
                )
                .stream()
                .filter(block -> "ACTIVE".equalsIgnoreCase(block.getStatus()))
                .collect(Collectors.toList())
                .isEmpty();
    }

    /* ================= OPTIONAL MANUAL DEACTIVATE ================= */
    public MaintenanceResponse deactivateMaintenanceBlock(UUID maintenanceId) {
        if (maintenanceId == null) {
            throw new RuntimeException("Maintenance ID is required");
        }

        MaintenanceBlock block = maintenanceBlockRepository.findById(maintenanceId)
                .orElseThrow(() -> new RuntimeException("Maintenance block not found"));

        block.setStatus("INACTIVE");

        MaintenanceBlock saved = maintenanceBlockRepository.save(block);
        return mapToResponse(saved);
    }

    /* ================= DTO MAPPING ================= */
    private MaintenanceResponse mapToResponse(MaintenanceBlock block) {
        StudyRoom room = block.getRoom();

        MaintenanceResponse response = new MaintenanceResponse();
        response.setMaintenanceId(block.getId());
        response.setRoomId(room != null ? room.getId() : null);

        response.setRoomName(room != null ? room.getDisplayName() : null);
        response.setRoomNumber(room != null ? room.getRoomNumber() : null);
        response.setBlockName(room != null ? room.getBlockName() : null);
        response.setDistrict(room != null ? room.getDistrict() : null);
        response.setExactLocation(room != null ? room.getLocation() : null);

        response.setFromDate(block.getStartAt() != null ? block.getStartAt().toLocalDate() : null);
        response.setToDate(
                block.getEndAt() != null
                        ? block.getEndAt().minusSeconds(1).toLocalDate()
                        : null
        );
        response.setReason(block.getReason());
        response.setIsActive("ACTIVE".equalsIgnoreCase(block.getStatus()));
        response.setCreatedAt(
            block.getCreatedAt() != null ? block.getCreatedAt().toLocalDateTime() : null
        );

        return response;
    }
}
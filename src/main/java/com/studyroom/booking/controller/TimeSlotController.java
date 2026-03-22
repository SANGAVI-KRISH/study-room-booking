package com.studyroom.booking.controller;

import com.studyroom.booking.dto.AvailableSlotsResponse;
import com.studyroom.booking.dto.MaintenanceBlockRequest;
import com.studyroom.booking.dto.RoomAvailabilityRequest;
import com.studyroom.booking.dto.TimeSlotResponse;
import com.studyroom.booking.model.MaintenanceBlock;
import com.studyroom.booking.model.RoomAvailability;
import com.studyroom.booking.service.TimeSlotService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/time-slots")
@CrossOrigin(origins = "*")
public class TimeSlotController {

    private final TimeSlotService timeSlotService;

    public TimeSlotController(TimeSlotService timeSlotService) {
        this.timeSlotService = timeSlotService;
    }

    // ======================================================
    // ADMIN - ROOM AVAILABILITY
    // ======================================================

    @PostMapping("/admin/availability/{roomId}")
    public ResponseEntity<RoomAvailability> saveAvailability(
            @PathVariable UUID roomId,
            @RequestBody RoomAvailabilityRequest request
    ) {
        RoomAvailability response = timeSlotService.saveRoomAvailability(roomId, request);
        return ResponseEntity.ok(response);
    }

    // ======================================================
    // ADMIN - MAINTENANCE BLOCK
    // ======================================================

    @PostMapping("/admin/maintenance/{roomId}")
    public ResponseEntity<MaintenanceBlock> addMaintenanceBlock(
            @PathVariable UUID roomId,
            @RequestBody MaintenanceBlockRequest request
    ) {
        MaintenanceBlock response = timeSlotService.addMaintenanceBlock(roomId, request);
        return ResponseEntity.ok(response);
    }

    // ======================================================
    // PUBLIC - AVAILABLE SLOTS FOR A DATE
    // ======================================================

    @GetMapping("/{roomId}")
    public ResponseEntity<AvailableSlotsResponse> getAvailableSlots(
            @PathVariable UUID roomId,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        AvailableSlotsResponse response = timeSlotService.getAvailableSlots(roomId, date);
        return ResponseEntity.ok(response);
    }

    // ======================================================
    // LIST ALL STORED TIME SLOTS
    // ======================================================

    @GetMapping
    public ResponseEntity<List<TimeSlotResponse>> getAllSlots() {
        List<TimeSlotResponse> response = timeSlotService.getAllSlots();
        return ResponseEntity.ok(response);
    }

    // ======================================================
    // LIST ALL STORED TIME SLOTS FOR A ROOM
    // ======================================================

    @GetMapping("/room/{roomId}")
    public ResponseEntity<List<TimeSlotResponse>> getSlotsByRoom(@PathVariable UUID roomId) {
        List<TimeSlotResponse> response = timeSlotService.getSlotsByRoom(roomId);
        return ResponseEntity.ok(response);
    }

    // ======================================================
    // LIST ACTIVE STORED TIME SLOTS FOR A ROOM
    // ======================================================

    @GetMapping("/room/{roomId}/active")
    public ResponseEntity<List<TimeSlotResponse>> getActiveSlotsByRoom(@PathVariable UUID roomId) {
        List<TimeSlotResponse> response = timeSlotService.getActiveSlotsByRoom(roomId);
        return ResponseEntity.ok(response);
    }
}
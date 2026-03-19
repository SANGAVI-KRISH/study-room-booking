package com.studyroom.booking.controller;

import com.studyroom.booking.dto.AvailableSlotsResponse;
import com.studyroom.booking.dto.MaintenanceBlockRequest;
import com.studyroom.booking.dto.RoomAvailabilityRequest;
import com.studyroom.booking.model.MaintenanceBlock;
import com.studyroom.booking.model.RoomAvailability;
import com.studyroom.booking.service.TimeSlotService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/time-slots")
@CrossOrigin(origins = "*")
public class TimeSlotController {

    private final TimeSlotService timeSlotService;

    public TimeSlotController(TimeSlotService timeSlotService) {
        this.timeSlotService = timeSlotService;
    }

    @PostMapping("/admin/availability/{roomId}")
    public ResponseEntity<RoomAvailability> saveAvailability(
            @PathVariable UUID roomId,
            @RequestBody RoomAvailabilityRequest request
    ) {
        return ResponseEntity.ok(timeSlotService.saveRoomAvailability(roomId, request));
    }

    @PostMapping("/admin/maintenance/{roomId}")
    public ResponseEntity<MaintenanceBlock> addMaintenanceBlock(
            @PathVariable UUID roomId,
            @RequestBody MaintenanceBlockRequest request
    ) {
        return ResponseEntity.ok(timeSlotService.addMaintenanceBlock(roomId, request));
    }

    @GetMapping("/{roomId}")
    public ResponseEntity<AvailableSlotsResponse> getAvailableSlots(
            @PathVariable UUID roomId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return ResponseEntity.ok(timeSlotService.getAvailableSlots(roomId, date));
    }
}
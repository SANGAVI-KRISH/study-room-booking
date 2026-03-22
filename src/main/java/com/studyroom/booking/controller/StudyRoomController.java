package com.studyroom.booking.controller;

import com.studyroom.booking.dto.StudyRoomRequest;
import com.studyroom.booking.dto.StudyRoomResponse;
import com.studyroom.booking.service.StudyRoomService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/rooms")
@CrossOrigin(origins = "*")
public class StudyRoomController {

    private final StudyRoomService studyRoomService;

    public StudyRoomController(StudyRoomService studyRoomService) {
        this.studyRoomService = studyRoomService;
    }

    /* ================= CREATE ================= */

    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<StudyRoomResponse> addRoom(@ModelAttribute StudyRoomRequest request) {
        StudyRoomResponse response = studyRoomService.addRoom(request);
        return ResponseEntity.ok(response);
    }

    /* ================= READ ================= */

    @GetMapping
    public ResponseEntity<List<StudyRoomResponse>> getAllRooms() {
        return ResponseEntity.ok(studyRoomService.getAllRooms());
    }

    @GetMapping("/active")
    public ResponseEntity<List<StudyRoomResponse>> getAllActiveRooms() {
        return ResponseEntity.ok(studyRoomService.getAllActiveRooms());
    }

    @GetMapping("/{id}")
    public ResponseEntity<StudyRoomResponse> getRoomById(@PathVariable UUID id) {
        Optional<StudyRoomResponse> room = studyRoomService.getRoomById(id);
        return room.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /* ================= UPDATE ================= */

    @PutMapping(value = "/{id}", consumes = {"multipart/form-data"})
    public ResponseEntity<StudyRoomResponse> updateRoom(
            @PathVariable UUID id,
            @ModelAttribute StudyRoomRequest request
    ) {
        StudyRoomResponse response = studyRoomService.updateRoom(id, request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/deactivate")
    public ResponseEntity<String> deactivateRoom(@PathVariable UUID id) {
        String response = studyRoomService.deactivateRoom(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/activate")
    public ResponseEntity<String> activateRoom(@PathVariable UUID id) {
        String response = studyRoomService.activateRoom(id);
        return ResponseEntity.ok(response);
    }

    /* ================= DELETE ================= */

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteRoom(@PathVariable UUID id) {
        String response = studyRoomService.deleteRoom(id);
        return ResponseEntity.ok(response);
    }

    /* ================= FILTER API ================= */

    @GetMapping("/filter")
    public ResponseEntity<List<StudyRoomResponse>> filterRooms(
            @RequestParam String district,
            @RequestParam String location
    ) {
        return ResponseEntity.ok(studyRoomService.getFilteredRoomResponses(district, location));
    }
}
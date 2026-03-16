package com.studyroom.booking.controller;

import com.studyroom.booking.model.StudyRoom;
import com.studyroom.booking.service.StudyRoomService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/rooms")
@CrossOrigin(origins = "http://localhost:5173")
public class StudyRoomController {

    private final StudyRoomService studyRoomService;

    public StudyRoomController(StudyRoomService studyRoomService) {
        this.studyRoomService = studyRoomService;
    }

    // Add new room
    @PostMapping
    public ResponseEntity<StudyRoom> addRoom(@RequestBody StudyRoom studyRoom) {
        StudyRoom savedRoom = studyRoomService.addRoom(studyRoom);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedRoom);
    }

    // Get all rooms
    @GetMapping
    public ResponseEntity<List<StudyRoom>> getAllRooms() {
        List<StudyRoom> rooms = studyRoomService.getAllRooms();
        return ResponseEntity.ok(rooms);
    }

    // Get room by id
    @GetMapping("/{id}")
    public ResponseEntity<?> getRoomById(@PathVariable UUID id) {
        return studyRoomService.getRoomById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Room not found with id: " + id));
    }

    // Update room
    @PutMapping("/{id}")
    public ResponseEntity<?> updateRoom(@PathVariable UUID id, @RequestBody StudyRoom studyRoom) {
        try {
            StudyRoom updatedRoom = studyRoomService.updateRoom(id, studyRoom);
            return ResponseEntity.ok(updatedRoom);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        }
    }

    // Delete room
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRoom(@PathVariable UUID id) {
        try {
            studyRoomService.deleteRoom(id);
            return ResponseEntity.ok("Room deleted successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        }
    }
}
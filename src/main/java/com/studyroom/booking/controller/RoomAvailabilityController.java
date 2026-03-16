package com.studyroom.booking.controller;

import com.studyroom.booking.model.StudyRoom;
import com.studyroom.booking.service.RoomAvailabilityService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/rooms")
@CrossOrigin(origins = "http://localhost:5173")
public class RoomAvailabilityController {

    private final RoomAvailabilityService roomAvailabilityService;

    public RoomAvailabilityController(RoomAvailabilityService roomAvailabilityService) {
        this.roomAvailabilityService = roomAvailabilityService;
    }

    @GetMapping("/available")
    public List<StudyRoom> getAvailableRooms(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
            LocalTime startTime,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
            LocalTime endTime,

            @RequestParam(required = false) Integer seatingCapacity,
            @RequestParam(required = false) String district,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String facility
    ) {
        if (date == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Date is required");
        }

        if (startTime == null || endTime == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Start time and end time are required");
        }

        if (!endTime.isAfter(startTime)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "End time must be after start time");
        }

        if (seatingCapacity != null && seatingCapacity <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Seating capacity must be greater than 0");
        }

        return roomAvailabilityService.getAvailableRooms(
                date,
                startTime,
                endTime,
                seatingCapacity,
                district,
                location,
                facility
        );
    }
}
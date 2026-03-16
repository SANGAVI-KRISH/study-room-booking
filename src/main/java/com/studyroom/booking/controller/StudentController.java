package com.studyroom.booking.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/student")
public class StudentController {

    // Student dashboard
    @GetMapping("/dashboard")
    public String studentDashboard() {
        return "Welcome Student! Secure endpoint accessed.";
    }

    // Student can view available study rooms
    @GetMapping("/rooms")
    public String viewRooms() {
        return "Students can view available study rooms.";
    }

    // Student can create a booking
    @GetMapping("/book")
    public String bookRoom() {
        return "Students can book study rooms.";
    }
}
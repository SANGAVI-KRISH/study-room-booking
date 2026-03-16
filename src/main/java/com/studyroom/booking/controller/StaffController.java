package com.studyroom.booking.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/staff")
public class StaffController {

    // Staff dashboard
    @GetMapping("/dashboard")
    public String staffDashboard() {
        return "Welcome Staff! Secure endpoint accessed.";
    }

    // Staff can monitor study room usage
    @GetMapping("/rooms")
    public String monitorRooms() {
        return "Staff can monitor study room availability and usage.";
    }

    // Staff can view student bookings
    @GetMapping("/bookings")
    public String viewBookings() {
        return "Staff can view student study room bookings.";
    }
}
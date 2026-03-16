package com.studyroom.booking.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @GetMapping("/dashboard")
    public String adminDashboard() {
        return "Welcome Admin! Secure endpoint accessed.";
    }

    @GetMapping("/rooms")
    public String manageRooms() {
        return "Admin can manage study rooms here.";
    }

    @GetMapping("/users")
    public String manageUsers() {
        return "Admin can view and manage registered users.";
    }
}
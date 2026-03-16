package com.studyroom.booking.controller;

import com.studyroom.booking.model.User;
import com.studyroom.booking.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
@CrossOrigin(origins = "http://localhost:5173")
public class ProfileController {

    @Autowired
    private UserService userService;

    // Get user profile by email
    @GetMapping("/{email}")
    public ResponseEntity<?> getProfile(@PathVariable String email) {

        User user = userService.getProfile(email);

        if (user == null) {
            return ResponseEntity.badRequest().body("User not found");
        }

        return ResponseEntity.ok(user);
    }

    // Update user profile
    @PutMapping("/{email}")
    public ResponseEntity<?> updateProfile(
            @PathVariable String email,
            @RequestBody User updatedUser) {

        String result = userService.updateProfile(email, updatedUser);

        if (result.equals("Profile updated successfully")) {
            return ResponseEntity.ok(result);
        }

        return ResponseEntity.badRequest().body(result);
    }
}
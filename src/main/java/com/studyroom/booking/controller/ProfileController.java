package com.studyroom.booking.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.studyroom.booking.dto.UpdatePasswordRequest;
import com.studyroom.booking.model.User;
import com.studyroom.booking.service.UserService;

@RestController
@RequestMapping("/api/profile")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"})
public class ProfileController {

    @Autowired
    private UserService userService;

    // Get user profile by email
    @GetMapping("/{email}")
    public ResponseEntity<?> getProfile(@PathVariable String email) {
        try {
            User user = userService.getProfile(email);

            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "message", "User not found"
                ));
            }

            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "message", e.getMessage()
            ));
        }
    }

    // Update password by email
    @PutMapping("/{email}/password")
    public ResponseEntity<?> updatePassword(
            @PathVariable String email,
            @RequestBody UpdatePasswordRequest request) {
        try {
            String result = userService.updatePassword(email, request);

            return ResponseEntity.ok(Map.of(
                    "message", result
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "message", e.getMessage()
            ));
        }
    }
}
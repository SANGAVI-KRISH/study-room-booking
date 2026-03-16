package com.studyroom.booking.controller;

import com.studyroom.booking.model.Notification;
import com.studyroom.booking.model.User;
import com.studyroom.booking.repository.NotificationRepository;
import com.studyroom.booking.repository.UserRepository;
import com.studyroom.booking.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "http://localhost:5173")
public class NotificationController {

    private final NotificationService notificationService;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationController(NotificationService notificationService,
                                  NotificationRepository notificationRepository,
                                  UserRepository userRepository) {
        this.notificationService = notificationService;
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/my")
    public ResponseEntity<List<Notification>> getMyNotifications(Authentication authentication) {
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.ok(notificationService.getUserNotifications(user.getId()));
    }

    @GetMapping("/my/unread-count")
    public ResponseEntity<Long> getUnreadCount(Authentication authentication) {
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.ok(notificationService.getUnreadCount(user.getId()));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<String> markAsRead(@PathVariable Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        notification.setRead(true);
        notificationRepository.save(notification);

        return ResponseEntity.ok("Notification marked as read");
    }
}
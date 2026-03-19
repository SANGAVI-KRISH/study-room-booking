package com.studyroom.booking.controller;

import com.studyroom.booking.model.Notification;
import com.studyroom.booking.model.User;
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
    private final UserRepository userRepository;

    public NotificationController(NotificationService notificationService,
                                  UserRepository userRepository) {
        this.notificationService = notificationService;
        this.userRepository = userRepository;
    }

    @GetMapping("/my")
    public ResponseEntity<List<Notification>> getMyNotifications(Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        return ResponseEntity.ok(notificationService.getUserNotifications(user.getId()));
    }

    @GetMapping("/my/unread-count")
    public ResponseEntity<Long> getUnreadCount(Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        return ResponseEntity.ok(notificationService.getUnreadCount(user.getId()));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Notification> markAsRead(@PathVariable Long id,
                                                   Authentication authentication) {
        User user = getAuthenticatedUser(authentication);

        Notification notification = notificationService.markAsRead(id);

        if (!notification.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("You are not allowed to update this notification");
        }

        return ResponseEntity.ok(notification);
    }

    @PutMapping("/{id}/unread")
    public ResponseEntity<Notification> markAsUnread(@PathVariable Long id,
                                                     Authentication authentication) {
        User user = getAuthenticatedUser(authentication);

        Notification notification = notificationService.markAsUnread(id);

        if (!notification.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("You are not allowed to update this notification");
        }

        return ResponseEntity.ok(notification);
    }

    private User getAuthenticatedUser(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new RuntimeException("Unauthorized");
        }

        String email = authentication.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
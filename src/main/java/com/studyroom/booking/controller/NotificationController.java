package com.studyroom.booking.controller;

import com.studyroom.booking.model.Notification;
import com.studyroom.booking.model.User;
import com.studyroom.booking.repository.UserRepository;
import com.studyroom.booking.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

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

    @GetMapping("/my/unread")
    public ResponseEntity<List<Notification>> getMyUnreadNotifications(Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        return ResponseEntity.ok(notificationService.getUnreadNotifications(user.getId()));
    }

    @GetMapping("/my/unread-count")
    public ResponseEntity<Long> getUnreadCount(Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        return ResponseEntity.ok(notificationService.getUnreadCount(user.getId()));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Notification> markAsRead(@PathVariable UUID id,
                                                   Authentication authentication) {
        User user = getAuthenticatedUser(authentication);

        Notification existing = notificationService.getNotificationById(id);
        validateNotificationOwner(existing, user);

        Notification updated = notificationService.markAsRead(id);
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/{id}/unread")
    public ResponseEntity<Notification> markAsUnread(@PathVariable UUID id,
                                                     Authentication authentication) {
        User user = getAuthenticatedUser(authentication);

        Notification existing = notificationService.getNotificationById(id);
        validateNotificationOwner(existing, user);

        Notification updated = notificationService.markAsUnread(id);
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/my/read-all")
    public ResponseEntity<String> markAllAsRead(Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        notificationService.markAllAsRead(user.getId());
        return ResponseEntity.ok("All notifications marked as read");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteNotification(@PathVariable UUID id,
                                                     Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        notificationService.deleteNotificationForUser(id, user.getId());
        return ResponseEntity.ok("Notification deleted successfully");
    }

    @DeleteMapping("/my/clear-all")
    public ResponseEntity<String> clearAllNotifications(Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        notificationService.clearAllNotifications(user.getId());
        return ResponseEntity.ok("All notifications cleared successfully");
    }

    private void validateNotificationOwner(Notification notification, User user) {
        if (notification == null || notification.getUser() == null) {
            throw new RuntimeException("Notification or notification user not found");
        }

        if (!notification.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("You are not allowed to access this notification");
        }
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
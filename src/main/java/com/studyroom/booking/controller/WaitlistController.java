package com.studyroom.booking.controller;

import com.studyroom.booking.model.Booking;
import com.studyroom.booking.model.Waitlist;
import com.studyroom.booking.service.WaitlistService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/waitlist")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"})
public class WaitlistController {

    private final WaitlistService waitlistService;

    public WaitlistController(WaitlistService waitlistService) {
        this.waitlistService = waitlistService;
    }

    // ================= JOIN WAITLIST =================

    @PostMapping("/join")
    public ResponseEntity<?> joinWaitlist(
            @RequestParam UUID userId,
            @RequestParam UUID roomId,
            @RequestParam OffsetDateTime startAt,
            @RequestParam OffsetDateTime endAt,
            @RequestParam(required = false, defaultValue = "false") Boolean autoAssign
    ) {
        try {
            Waitlist waitlist = waitlistService.joinWaitlist(userId, roomId, startAt, endAt, autoAssign);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Joined waitlist successfully");
            response.put("waitlist", waitlist);
            response.put("position", waitlist.getPositionNumber());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(errorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse("Failed to join waitlist"));
        }
    }

    // ================= FETCH METHODS =================

    @GetMapping("/{waitlistId}")
    public ResponseEntity<?> getWaitlistById(@PathVariable UUID waitlistId) {
        try {
            Waitlist waitlist = waitlistService.getById(waitlistId);
            return ResponseEntity.ok(waitlist);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse("Failed to fetch waitlist entry"));
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserWaitlist(@PathVariable UUID userId) {
        try {
            List<Waitlist> entries = waitlistService.getAllByUser(userId);
            return ResponseEntity.ok(entries);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse("Failed to fetch user waitlist"));
        }
    }

    @GetMapping("/user/{userId}/active")
    public ResponseEntity<?> getActiveUserWaitlist(@PathVariable UUID userId) {
        try {
            List<Waitlist> entries = waitlistService.getActiveByUser(userId);
            return ResponseEntity.ok(entries);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse("Failed to fetch active waitlist entries"));
        }
    }

    @GetMapping("/slot")
    public ResponseEntity<?> getSlotWaitlist(
            @RequestParam UUID roomId,
            @RequestParam OffsetDateTime startAt,
            @RequestParam OffsetDateTime endAt
    ) {
        try {
            List<Waitlist> entries = waitlistService.getSlotWaitlist(roomId, startAt, endAt);
            return ResponseEntity.ok(entries);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse("Failed to fetch slot waitlist"));
        }
    }

    @GetMapping("/position")
    public ResponseEntity<?> getWaitlistPosition(
            @RequestParam UUID userId,
            @RequestParam UUID roomId,
            @RequestParam OffsetDateTime startAt,
            @RequestParam OffsetDateTime endAt
    ) {
        try {
            int position = waitlistService.getWaitlistPosition(userId, roomId, startAt, endAt);

            Map<String, Object> response = new HashMap<>();
            response.put("userId", userId);
            response.put("roomId", roomId);
            response.put("startAt", startAt);
            response.put("endAt", endAt);
            response.put("position", position);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse("Failed to fetch waitlist position"));
        }
    }

    // ================= CANCEL WAITLIST =================

    @PutMapping("/{waitlistId}/cancel")
    public ResponseEntity<?> cancelWaitlist(
            @PathVariable UUID waitlistId,
            @RequestParam UUID userId
    ) {
        try {
            Waitlist waitlist = waitlistService.cancelWaitlist(waitlistId, userId);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Waitlist entry cancelled successfully");
            response.put("waitlist", waitlist);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(errorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse("Failed to cancel waitlist entry"));
        }
    }

    // ================= CONFIRM NOTIFIED WAITLIST =================

    @PostMapping("/{waitlistId}/confirm")
    public ResponseEntity<?> confirmWaitlist(
            @PathVariable UUID waitlistId,
            @RequestParam UUID userId
    ) {
        try {
            Booking booking = waitlistService.confirmNotifiedWaitlist(waitlistId, userId);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Waitlist confirmed and booking created successfully");
            response.put("booking", booking);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(errorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse("Failed to confirm waitlist entry"));
        }
    }

    // ================= PROCESS / SCHEDULER SUPPORT =================

    @PostMapping("/process-released-slot")
    public ResponseEntity<?> processReleasedSlot(
            @RequestParam UUID roomId,
            @RequestParam OffsetDateTime startAt,
            @RequestParam OffsetDateTime endAt
    ) {
        try {
            waitlistService.processReleasedSlot(roomId, startAt, endAt);
            return ResponseEntity.ok(successResponse("Released slot processed successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(errorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse("Failed to process released slot"));
        }
    }

    @PostMapping("/process-expired")
    public ResponseEntity<?> processExpiredNotifications() {
        try {
            waitlistService.processExpiredNotifications();
            return ResponseEntity.ok(successResponse("Expired waitlist notifications processed successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse("Failed to process expired waitlist notifications"));
        }
    }

    // ================= HELPER METHODS =================

    private Map<String, Object> successResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        return response;
    }

    private Map<String, Object> errorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", message);
        return response;
    }
}
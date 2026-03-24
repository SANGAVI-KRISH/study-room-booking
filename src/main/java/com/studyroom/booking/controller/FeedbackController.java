package com.studyroom.booking.controller;

import com.studyroom.booking.dto.FeedbackRequest;
import com.studyroom.booking.dto.FeedbackResponse;
import com.studyroom.booking.dto.FeedbackSummaryResponse;
import com.studyroom.booking.service.FeedbackService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/feedback")
@CrossOrigin(origins = {
        "http://localhost:5173",
        "http://127.0.0.1:5173"
})
public class FeedbackController {

    private final FeedbackService feedbackService;

    public FeedbackController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    @PostMapping
    public ResponseEntity<?> submitFeedback(
            @Valid @RequestBody FeedbackRequest request,
            @RequestParam UUID userId
    ) {
        try {
            FeedbackResponse response = feedbackService.submitFeedback(request, userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return buildErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (RuntimeException e) {
            return buildErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to submit feedback");
        }
    }

    @GetMapping("/room/{roomId}")
    public ResponseEntity<?> getRoomFeedback(@PathVariable UUID roomId) {
        try {
            FeedbackSummaryResponse response = feedbackService.getRoomFeedback(roomId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return buildErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (RuntimeException e) {
            return buildErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to fetch room feedback");
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserFeedback(@PathVariable UUID userId) {
        try {
            List<FeedbackResponse> response = feedbackService.getUserFeedback(userId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return buildErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (RuntimeException e) {
            return buildErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to fetch user feedback");
        }
    }

    // For admin page: get all feedback
    @GetMapping
    public ResponseEntity<?> getAllFeedback() {
        try {
            List<FeedbackResponse> response = feedbackService.getAllFeedback();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to fetch all feedback");
        }
    }

    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<?> getFeedbackByBooking(@PathVariable UUID bookingId) {
        try {
            FeedbackResponse response = feedbackService.getFeedbackByBooking(bookingId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return buildErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (RuntimeException e) {
            return buildErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to fetch booking feedback");
        }
    }

    @GetMapping("/booking/{bookingId}/exists")
    public ResponseEntity<?> hasBookingFeedback(@PathVariable UUID bookingId) {
        try {
            boolean exists = feedbackService.hasBookingFeedback(bookingId);
            return ResponseEntity.ok(Map.of("exists", exists));
        } catch (IllegalArgumentException e) {
            return buildErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (RuntimeException e) {
            return buildErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to check feedback status");
        }
    }

    @DeleteMapping("/{feedbackId}")
    public ResponseEntity<?> deleteFeedback(@PathVariable UUID feedbackId) {
        try {
            feedbackService.deleteFeedback(feedbackId);
            return ResponseEntity.ok(Map.of(
                    "message", "Feedback deleted successfully"
            ));
        } catch (IllegalArgumentException e) {
            return buildErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (RuntimeException e) {
            return buildErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to delete feedback");
        }
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();

        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }

        String message = "Validation failed";
        if (!fieldErrors.isEmpty()) {
            message = fieldErrors.values().iterator().next();
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("message", message);
        body.put("errors", fieldErrors);
        body.put("status", HttpStatus.BAD_REQUEST.value());

        return ResponseEntity.badRequest().body(body);
    }

    private ResponseEntity<Map<String, Object>> buildErrorResponse(HttpStatus status, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("message", message != null && !message.isBlank() ? message : "Unexpected error");
        body.put("status", status.value());
        return ResponseEntity.status(status).body(body);
    }
}
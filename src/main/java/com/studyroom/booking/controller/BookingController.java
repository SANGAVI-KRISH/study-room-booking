package com.studyroom.booking.controller;

import com.studyroom.booking.model.Booking;
import com.studyroom.booking.model.BookingStatus;
import com.studyroom.booking.service.BookingService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/bookings")
@CrossOrigin(origins = "http://localhost:5173")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping
    public ResponseEntity<List<Booking>> getAllBookings() {
        return ResponseEntity.ok(bookingService.getAllBookings());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Booking> getBookingById(@PathVariable UUID id) {
        return ResponseEntity.ok(bookingService.getBookingById(id));
    }

    @GetMapping("/room/{roomId}")
    public ResponseEntity<List<Booking>> getBookingsByRoomId(@PathVariable UUID roomId) {
        return ResponseEntity.ok(bookingService.getBookingsByRoomId(roomId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Booking>> getBookingsByUserId(@PathVariable UUID userId) {
        return ResponseEntity.ok(bookingService.getBookingsByUserId(userId));
    }

    @GetMapping("/user/{userId}/current")
    public ResponseEntity<List<Booking>> getCurrentBookings(@PathVariable UUID userId) {
        return ResponseEntity.ok(bookingService.getCurrentBookings(userId));
    }

    @GetMapping("/user/{userId}/past")
    public ResponseEntity<List<Booking>> getPastBookings(@PathVariable UUID userId) {
        return ResponseEntity.ok(bookingService.getPastBookings(userId));
    }

    @GetMapping("/user/{userId}/active")
    public ResponseEntity<List<Booking>> getActiveCurrentBookings(@PathVariable UUID userId) {
        return ResponseEntity.ok(bookingService.getActiveCurrentBookings(userId));
    }

    @GetMapping("/date/{date}")
    public ResponseEntity<List<Booking>> getBookingsByDate(
            @PathVariable
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date
    ) {
        return ResponseEntity.ok(bookingService.getBookingsByDate(date));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Booking>> getBookingsByStatus(@PathVariable BookingStatus status) {
        return ResponseEntity.ok(bookingService.getBookingsByStatus(status));
    }

    @GetMapping("/status/pending")
    public ResponseEntity<List<Booking>> getPendingBookings() {
        return ResponseEntity.ok(bookingService.getPendingBookings());
    }

    @GetMapping("/status/approved")
    public ResponseEntity<List<Booking>> getApprovedBookings() {
        return ResponseEntity.ok(bookingService.getApprovedBookings());
    }

    @GetMapping("/status/rejected")
    public ResponseEntity<List<Booking>> getRejectedBookings() {
        return ResponseEntity.ok(bookingService.getRejectedBookings());
    }

    @GetMapping("/status/cancelled")
    public ResponseEntity<List<Booking>> getCancelledBookings() {
        return ResponseEntity.ok(bookingService.getCancelledBookings());
    }

    @GetMapping("/status/completed")
    public ResponseEntity<List<Booking>> getCompletedBookings() {
        return ResponseEntity.ok(bookingService.getCompletedBookings());
    }

    @GetMapping("/room/{roomId}/date/{date}")
    public ResponseEntity<List<Booking>> getBookingsByRoomIdAndDate(
            @PathVariable UUID roomId,
            @PathVariable
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date
    ) {
        return ResponseEntity.ok(bookingService.getBookingsByRoomIdAndDate(roomId, date));
    }

    @GetMapping("/user/{userId}/status/{status}")
    public ResponseEntity<List<Booking>> getBookingsByUserIdAndStatus(
            @PathVariable UUID userId,
            @PathVariable BookingStatus status
    ) {
        return ResponseEntity.ok(bookingService.getBookingsByUserIdAndStatus(userId, status));
    }

    @GetMapping("/room/{roomId}/status/{status}")
    public ResponseEntity<List<Booking>> getBookingsByRoomIdAndStatus(
            @PathVariable UUID roomId,
            @PathVariable BookingStatus status
    ) {
        return ResponseEntity.ok(bookingService.getBookingsByRoomIdAndStatus(roomId, status));
    }

    @GetMapping("/date/{date}/status/{status}")
    public ResponseEntity<List<Booking>> getBookingsByDateAndStatus(
            @PathVariable
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date,
            @PathVariable BookingStatus status
    ) {
        return ResponseEntity.ok(bookingService.getBookingsByDateAndStatus(date, status));
    }

    @GetMapping("/user/{userId}/history")
    public ResponseEntity<List<Booking>> getBookingHistory(@PathVariable UUID userId) {
        return ResponseEntity.ok(bookingService.getBookingHistory(userId));
    }

    @GetMapping("/user/{userId}/history/completed")
    public ResponseEntity<List<Booking>> getCompletedBookingsByUserId(@PathVariable UUID userId) {
        return ResponseEntity.ok(bookingService.getCompletedBookingsByUserId(userId));
    }

    @GetMapping("/user/{userId}/history/cancelled")
    public ResponseEntity<List<Booking>> getCancelledBookingsByUserId(@PathVariable UUID userId) {
        return ResponseEntity.ok(bookingService.getCancelledBookingsByUserId(userId));
    }

    @GetMapping("/user/{userId}/history/rejected")
    public ResponseEntity<List<Booking>> getRejectedBookingsByUserId(@PathVariable UUID userId) {
        return ResponseEntity.ok(bookingService.getRejectedBookingsByUserId(userId));
    }

    @GetMapping("/user/{userId}/history/date/{date}")
    public ResponseEntity<List<Booking>> getBookingHistoryByDate(
            @PathVariable UUID userId,
            @PathVariable
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date
    ) {
        return ResponseEntity.ok(bookingService.getBookingHistoryByDate(userId, date));
    }

    @GetMapping("/user/{userId}/history/filter")
    public ResponseEntity<List<Booking>> getBookingHistoryByDateRange(
            @PathVariable UUID userId,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate
    ) {
        return ResponseEntity.ok(
                bookingService.getBookingHistoryByDateRange(userId, startDate, endDate)
        );
    }

    @GetMapping("/admin/history")
    public ResponseEntity<List<Booking>> getAllBookingHistoryForAdmin() {
        return ResponseEntity.ok(bookingService.getAllBookingHistoryForAdmin());
    }

    @GetMapping("/admin/history/filter")
    public ResponseEntity<List<Booking>> getAllBookingHistoryForAdminByDateRange(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate
    ) {
        return ResponseEntity.ok(
                bookingService.getAllBookingHistoryForAdminByDateRange(startDate, endDate)
        );
    }

    @PostMapping("/summary")
    public ResponseEntity<Map<String, Object>> getBookingSummary(@RequestBody CreateBookingRequest request) {
        validateBookingRequest(request);

        long durationMinutes = Duration.between(request.getStartTime(), request.getEndTime()).toMinutes();

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("roomId", request.getRoomId());
        summary.put("userId", request.getUserId());
        summary.put("bookingDate", request.getBookingDate());
        summary.put("startTime", request.getStartTime());
        summary.put("endTime", request.getEndTime());
        summary.put("durationMinutes", durationMinutes);
        summary.put("message", "Booking summary generated successfully. You can confirm this booking.");

        return ResponseEntity.ok(summary);
    }

    @PostMapping
    public ResponseEntity<Booking> createBooking(@RequestBody CreateBookingRequest request) {
        validateBookingRequest(request);

        Booking createdBooking = bookingService.createBooking(
                request.getRoomId(),
                request.getUserId(),
                request.getBookingDate(),
                request.getStartTime(),
                request.getEndTime()
        );

        return ResponseEntity.ok(createdBooking);
    }

    @PutMapping("/{id}/confirm")
    public ResponseEntity<Booking> confirmBooking(@PathVariable UUID id) {
        return ResponseEntity.ok(bookingService.updateBookingStatus(id, BookingStatus.APPROVED));
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<Booking> approveBooking(@PathVariable UUID id) {
        return ResponseEntity.ok(bookingService.approveBooking(id));
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<Booking> rejectBooking(@PathVariable UUID id) {
        return ResponseEntity.ok(bookingService.rejectBooking(id));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<Booking> cancelBooking(@PathVariable UUID id) {
        return ResponseEntity.ok(bookingService.cancelBooking(id));
    }

    @PutMapping("/{id}/complete")
    public ResponseEntity<Booking> completeBooking(@PathVariable UUID id) {
        return ResponseEntity.ok(bookingService.completeBooking(id));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Booking> updateBookingStatus(
            @PathVariable UUID id,
            @RequestParam BookingStatus status
    ) {
        return ResponseEntity.ok(bookingService.updateBookingStatus(id, status));
    }

    @PutMapping("/{id}/reschedule")
    public ResponseEntity<Booking> rescheduleBooking(
            @PathVariable UUID id,
            @RequestBody RescheduleBookingRequest request
    ) {
        if (request.getBookingDate() == null) {
            throw new IllegalArgumentException("Booking date is required");
        }

        if (request.getStartTime() == null || request.getEndTime() == null) {
            throw new IllegalArgumentException("Start time and end time are required");
        }

        if (request.getBookingDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Booking for past dates is not allowed");
        }

        if (!request.getEndTime().isAfter(request.getStartTime())) {
            throw new IllegalArgumentException("End time must be after start time");
        }

        Booking updatedBooking = bookingService.rescheduleBooking(
                id,
                request.getBookingDate(),
                request.getStartTime(),
                request.getEndTime()
        );

        return ResponseEntity.ok(updatedBooking);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteBooking(@PathVariable UUID id) {
        bookingService.deleteBooking(id);
        return ResponseEntity.ok("Booking deleted successfully");
    }

    private void validateBookingRequest(CreateBookingRequest request) {
        if (request.getRoomId() == null) {
            throw new IllegalArgumentException("Room ID is required");
        }

        if (request.getUserId() == null) {
            throw new IllegalArgumentException("User ID is required");
        }

        if (request.getBookingDate() == null) {
            throw new IllegalArgumentException("Booking date is required");
        }

        if (request.getStartTime() == null || request.getEndTime() == null) {
            throw new IllegalArgumentException("Start time and end time are required");
        }

        if (request.getBookingDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Booking for past dates is not allowed");
        }

        if (!request.getEndTime().isAfter(request.getStartTime())) {
            throw new IllegalArgumentException("End time must be after start time");
        }
    }

    public static class CreateBookingRequest {

        private UUID roomId;
        private UUID userId;

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private LocalDate bookingDate;

        @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
        private LocalTime startTime;

        @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
        private LocalTime endTime;

        public CreateBookingRequest() {
        }

        public UUID getRoomId() {
            return roomId;
        }

        public void setRoomId(UUID roomId) {
            this.roomId = roomId;
        }

        public UUID getUserId() {
            return userId;
        }

        public void setUserId(UUID userId) {
            this.userId = userId;
        }

        public LocalDate getBookingDate() {
            return bookingDate;
        }

        public void setBookingDate(LocalDate bookingDate) {
            this.bookingDate = bookingDate;
        }

        public LocalTime getStartTime() {
            return startTime;
        }

        public void setStartTime(LocalTime startTime) {
            this.startTime = startTime;
        }

        public LocalTime getEndTime() {
            return endTime;
        }

        public void setEndTime(LocalTime endTime) {
            this.endTime = endTime;
        }
    }

    public static class RescheduleBookingRequest {

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private LocalDate bookingDate;

        @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
        private LocalTime startTime;

        @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
        private LocalTime endTime;

        public RescheduleBookingRequest() {
        }

        public LocalDate getBookingDate() {
            return bookingDate;
        }

        public void setBookingDate(LocalDate bookingDate) {
            this.bookingDate = bookingDate;
        }

        public LocalTime getStartTime() {
            return startTime;
        }

        public void setStartTime(LocalTime startTime) {
            this.startTime = startTime;
        }

        public LocalTime getEndTime() {
            return endTime;
        }

        public void setEndTime(LocalTime endTime) {
            this.endTime = endTime;
        }
    }
}
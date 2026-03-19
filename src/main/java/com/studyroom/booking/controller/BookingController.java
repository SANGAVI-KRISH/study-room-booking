package com.studyroom.booking.controller;

import com.studyroom.booking.dto.BookingRequestDto;
import com.studyroom.booking.dto.BookingResponse;
import com.studyroom.booking.dto.BookingSummaryResponse;
import com.studyroom.booking.model.Booking;
import com.studyroom.booking.model.BookingStatus;
import com.studyroom.booking.service.BookingPdfService;
import com.studyroom.booking.service.BookingService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/bookings")
@CrossOrigin(origins = {
        "http://localhost:5173",
        "http://127.0.0.1:5173"
})
public class BookingController {

    private final BookingService bookingService;
    private final BookingPdfService bookingPdfService;
    private static final ZoneId APP_ZONE = ZoneId.of("Asia/Kolkata");

    public BookingController(BookingService bookingService, BookingPdfService bookingPdfService) {
        this.bookingService = bookingService;
        this.bookingPdfService = bookingPdfService;
    }

    @GetMapping
    public ResponseEntity<List<BookingResponse>> getAllBookings() {
        return ResponseEntity.ok(
                bookingService.getAllBookings()
                        .stream()
                        .map(this::mapToBookingResponse)
                        .collect(Collectors.toList())
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingResponse> getBookingById(@PathVariable UUID id) {
        return ResponseEntity.ok(mapToBookingResponse(bookingService.getBookingById(id)));
    }

    @GetMapping("/room/{roomId}")
    public ResponseEntity<List<BookingResponse>> getBookingsByRoomId(@PathVariable UUID roomId) {
        return ResponseEntity.ok(
                bookingService.getBookingsByRoomId(roomId)
                        .stream()
                        .map(this::mapToBookingResponse)
                        .collect(Collectors.toList())
        );
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<BookingResponse>> getBookingsByUserId(@PathVariable UUID userId) {
        return ResponseEntity.ok(
                bookingService.getBookingsByUserId(userId)
                        .stream()
                        .map(this::mapToBookingResponse)
                        .collect(Collectors.toList())
        );
    }

    @GetMapping("/user/{userId}/current")
    public ResponseEntity<List<BookingResponse>> getCurrentBookings(@PathVariable UUID userId) {
        return ResponseEntity.ok(
                bookingService.getCurrentBookings(userId)
                        .stream()
                        .map(this::mapToBookingResponse)
                        .collect(Collectors.toList())
        );
    }

    @GetMapping("/user/{userId}/past")
    public ResponseEntity<List<BookingResponse>> getPastBookings(@PathVariable UUID userId) {
        return ResponseEntity.ok(
                bookingService.getPastBookings(userId)
                        .stream()
                        .map(this::mapToBookingResponse)
                        .collect(Collectors.toList())
        );
    }

    @GetMapping("/user/{userId}/active")
    public ResponseEntity<List<BookingResponse>> getActiveCurrentBookings(@PathVariable UUID userId) {
        return ResponseEntity.ok(
                bookingService.getActiveCurrentBookings(userId)
                        .stream()
                        .map(this::mapToBookingResponse)
                        .collect(Collectors.toList())
        );
    }

    @GetMapping("/date/{date}")
    public ResponseEntity<List<BookingResponse>> getBookingsByDate(
            @PathVariable
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date
    ) {
        return ResponseEntity.ok(
                bookingService.getBookingsByDate(date)
                        .stream()
                        .map(this::mapToBookingResponse)
                        .collect(Collectors.toList())
        );
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<BookingResponse>> getBookingsByStatus(@PathVariable BookingStatus status) {
        return ResponseEntity.ok(
                bookingService.getBookingsByStatus(status)
                        .stream()
                        .map(this::mapToBookingResponse)
                        .collect(Collectors.toList())
        );
    }

    @GetMapping("/room/{roomId}/date/{date}")
    public ResponseEntity<List<BookingResponse>> getBookingsByRoomIdAndDate(
            @PathVariable UUID roomId,
            @PathVariable
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date
    ) {
        return ResponseEntity.ok(
                bookingService.getBookingsByRoomIdAndDate(roomId, date)
                        .stream()
                        .map(this::mapToBookingResponse)
                        .collect(Collectors.toList())
        );
    }

    @GetMapping("/user/{userId}/status/{status}")
    public ResponseEntity<List<BookingResponse>> getBookingsByUserIdAndStatus(
            @PathVariable UUID userId,
            @PathVariable BookingStatus status
    ) {
        return ResponseEntity.ok(
                bookingService.getBookingsByUserIdAndStatus(userId, status)
                        .stream()
                        .map(this::mapToBookingResponse)
                        .collect(Collectors.toList())
        );
    }

    @GetMapping("/room/{roomId}/status/{status}")
    public ResponseEntity<List<BookingResponse>> getBookingsByRoomIdAndStatus(
            @PathVariable UUID roomId,
            @PathVariable BookingStatus status
    ) {
        return ResponseEntity.ok(
                bookingService.getBookingsByRoomIdAndStatus(roomId, status)
                        .stream()
                        .map(this::mapToBookingResponse)
                        .collect(Collectors.toList())
        );
    }

    @GetMapping("/date/{date}/status/{status}")
    public ResponseEntity<List<BookingResponse>> getBookingsByDateAndStatus(
            @PathVariable
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date,
            @PathVariable BookingStatus status
    ) {
        return ResponseEntity.ok(
                bookingService.getBookingsByDateAndStatus(date, status)
                        .stream()
                        .map(this::mapToBookingResponse)
                        .collect(Collectors.toList())
        );
    }

    @GetMapping("/status/pending")
    public ResponseEntity<List<BookingResponse>> getPendingBookings() {
        return ResponseEntity.ok(
                bookingService.getPendingBookings()
                        .stream()
                        .map(this::mapToBookingResponse)
                        .collect(Collectors.toList())
        );
    }

    @GetMapping("/status/approved")
    public ResponseEntity<List<BookingResponse>> getApprovedBookings() {
        return ResponseEntity.ok(
                bookingService.getApprovedBookings()
                        .stream()
                        .map(this::mapToBookingResponse)
                        .collect(Collectors.toList())
        );
    }

    @GetMapping("/status/rejected")
    public ResponseEntity<List<BookingResponse>> getRejectedBookings() {
        return ResponseEntity.ok(
                bookingService.getRejectedBookings()
                        .stream()
                        .map(this::mapToBookingResponse)
                        .collect(Collectors.toList())
        );
    }

    @GetMapping("/status/cancelled")
    public ResponseEntity<List<BookingResponse>> getCancelledBookings() {
        return ResponseEntity.ok(
                bookingService.getCancelledBookings()
                        .stream()
                        .map(this::mapToBookingResponse)
                        .collect(Collectors.toList())
        );
    }

    @GetMapping("/status/completed")
    public ResponseEntity<List<BookingResponse>> getCompletedBookings() {
        return ResponseEntity.ok(
                bookingService.getCompletedBookings()
                        .stream()
                        .map(this::mapToBookingResponse)
                        .collect(Collectors.toList())
        );
    }

    @GetMapping("/user/{userId}/history")
    public ResponseEntity<List<BookingResponse>> getBookingHistory(@PathVariable UUID userId) {
        return ResponseEntity.ok(
                bookingService.getBookingHistory(userId)
                        .stream()
                        .map(this::mapToBookingResponse)
                        .collect(Collectors.toList())
        );
    }

    @GetMapping("/user/{userId}/history/completed")
    public ResponseEntity<List<BookingResponse>> getCompletedBookingsByUserId(@PathVariable UUID userId) {
        return ResponseEntity.ok(
                bookingService.getCompletedBookingsByUserId(userId)
                        .stream()
                        .map(this::mapToBookingResponse)
                        .collect(Collectors.toList())
        );
    }

    @GetMapping("/user/{userId}/history/cancelled")
    public ResponseEntity<List<BookingResponse>> getCancelledBookingsByUserId(@PathVariable UUID userId) {
        return ResponseEntity.ok(
                bookingService.getCancelledBookingsByUserId(userId)
                        .stream()
                        .map(this::mapToBookingResponse)
                        .collect(Collectors.toList())
        );
    }

    @GetMapping("/user/{userId}/history/rejected")
    public ResponseEntity<List<BookingResponse>> getRejectedBookingsByUserId(@PathVariable UUID userId) {
        return ResponseEntity.ok(
                bookingService.getRejectedBookingsByUserId(userId)
                        .stream()
                        .map(this::mapToBookingResponse)
                        .collect(Collectors.toList())
        );
    }

    @GetMapping("/user/{userId}/history/date/{date}")
    public ResponseEntity<List<BookingResponse>> getBookingHistoryByDate(
            @PathVariable UUID userId,
            @PathVariable
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date
    ) {
        return ResponseEntity.ok(
                bookingService.getBookingHistoryByDate(userId, date)
                        .stream()
                        .map(this::mapToBookingResponse)
                        .collect(Collectors.toList())
        );
    }

    @GetMapping("/user/{userId}/history/filter")
    public ResponseEntity<List<BookingResponse>> getBookingHistoryByDateRange(
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
                        .stream()
                        .map(this::mapToBookingResponse)
                        .collect(Collectors.toList())
        );
    }

    @GetMapping("/user/{userId}/history/download/pdf")
    public ResponseEntity<byte[]> downloadBookingHistoryPdf(@PathVariable UUID userId) {
        List<Booking> bookings = bookingService.getBookingHistory(userId);
        byte[] pdfBytes = bookingPdfService.generateBookingHistoryPdf(bookings);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(
                ContentDisposition.attachment()
                        .filename("booking-history.pdf")
                        .build()
        );

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }

    @GetMapping("/admin/history")
    public ResponseEntity<List<BookingResponse>> getAllBookingHistoryForAdmin() {
        return ResponseEntity.ok(
                bookingService.getAllBookingHistoryForAdmin()
                        .stream()
                        .map(this::mapToBookingResponse)
                        .collect(Collectors.toList())
        );
    }

    @GetMapping("/admin/history/filter")
    public ResponseEntity<List<BookingResponse>> getAllBookingHistoryForAdminByDateRange(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate
    ) {
        return ResponseEntity.ok(
                bookingService.getAllBookingHistoryForAdminByDateRange(startDate, endDate)
                        .stream()
                        .map(this::mapToBookingResponse)
                        .collect(Collectors.toList())
        );
    }

    @PostMapping("/summary")
    public ResponseEntity<BookingSummaryResponse> getBookingSummary(@RequestBody BookingRequestDto request) {
        validateBookingRequest(request);

        long durationMinutes = Duration.between(request.getStartAt(), request.getEndAt()).toMinutes();

        BookingSummaryResponse response = new BookingSummaryResponse();
        response.setRoomId(request.getRoomId());
        response.setRoomName(bookingService.getRoomNameById(request.getRoomId()));
        response.setUserId(request.getUserId());
        response.setStartAt(request.getStartAt());
        response.setEndAt(request.getEndAt());
        response.setPurpose(request.getPurpose());
        response.setAttendeeCount(request.getAttendeeCount());
        response.setDurationMinutes(durationMinutes);
        response.setStatus(BookingStatus.PENDING);
        response.setMessage("Booking summary generated successfully");

        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(@RequestBody BookingRequestDto request) {
        validateBookingRequest(request);

        Booking booking = bookingService.createBooking(
                request.getRoomId(),
                request.getUserId(),
                request.getStartAt(),
                request.getEndAt(),
                request.getPurpose(),
                request.getAttendeeCount()
        );

        return ResponseEntity.ok(mapToBookingResponse(booking));
    }

    @PutMapping("/{id}/confirm")
    public ResponseEntity<BookingResponse> confirmBooking(
            @PathVariable UUID id,
            @RequestParam UUID approvedBy
    ) {
        return ResponseEntity.ok(
                mapToBookingResponse(bookingService.approveBooking(id, approvedBy))
        );
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<BookingResponse> approveBooking(
            @PathVariable UUID id,
            @RequestParam UUID approvedBy
    ) {
        return ResponseEntity.ok(
                mapToBookingResponse(bookingService.approveBooking(id, approvedBy))
        );
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<BookingResponse> rejectBooking(@PathVariable UUID id) {
        return ResponseEntity.ok(mapToBookingResponse(bookingService.rejectBooking(id)));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<BookingResponse> cancelBooking(@PathVariable UUID id) {
        return ResponseEntity.ok(mapToBookingResponse(bookingService.cancelBooking(id)));
    }

    @PutMapping("/{id}/complete")
    public ResponseEntity<BookingResponse> completeBooking(@PathVariable UUID id) {
        return ResponseEntity.ok(mapToBookingResponse(bookingService.completeBooking(id)));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<BookingResponse> updateBookingStatus(
            @PathVariable UUID id,
            @RequestParam BookingStatus status
    ) {
        return ResponseEntity.ok(mapToBookingResponse(bookingService.updateBookingStatus(id, status)));
    }

    @PutMapping("/{id}/reschedule")
    public ResponseEntity<BookingResponse> rescheduleBooking(
            @PathVariable UUID id,
            @RequestBody BookingRequestDto request
    ) {
        validateRescheduleRequest(request);

        Booking booking = bookingService.rescheduleBooking(
                id,
                request.getStartAt(),
                request.getEndAt()
        );

        return ResponseEntity.ok(mapToBookingResponse(booking));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteBooking(@PathVariable UUID id) {
        bookingService.deleteBooking(id);
        return ResponseEntity.ok("Booking deleted successfully");
    }

    private void validateBookingRequest(BookingRequestDto request) {
        if (request == null) {
            throw new RuntimeException("Booking request is required");
        }
        if (request.getRoomId() == null) {
            throw new RuntimeException("Room ID is required");
        }
        if (request.getUserId() == null) {
            throw new RuntimeException("User ID is required");
        }
        if (request.getStartAt() == null || request.getEndAt() == null) {
            throw new RuntimeException("Start time and end time are required");
        }
        if (!request.getEndAt().isAfter(request.getStartAt())) {
            throw new RuntimeException("End time must be after start time");
        }
        if (!request.getStartAt().isAfter(OffsetDateTime.now(APP_ZONE))) {
            throw new RuntimeException("Start time must be in the future");
        }
        if (request.getAttendeeCount() != null && request.getAttendeeCount() <= 0) {
            throw new RuntimeException("Attendee count must be greater than 0");
        }
    }

    private void validateRescheduleRequest(BookingRequestDto request) {
        if (request == null) {
            throw new RuntimeException("Reschedule request is required");
        }
        if (request.getStartAt() == null || request.getEndAt() == null) {
            throw new RuntimeException("Start time and end time are required");
        }
        if (!request.getEndAt().isAfter(request.getStartAt())) {
            throw new RuntimeException("End time must be after start time");
        }
        if (!request.getStartAt().isAfter(OffsetDateTime.now(APP_ZONE))) {
            throw new RuntimeException("Start time must be in the future");
        }
    }

    private BookingResponse mapToBookingResponse(Booking booking) {
        BookingResponse response = new BookingResponse();

        response.setBookingId(booking.getId());
        response.setRoomId(booking.getRoom().getId());
        response.setRoomName(
                booking.getRoom().getBlockName() + " - " + booking.getRoom().getRoomNumber()
        );
        response.setUserId(booking.getUser().getId());
        response.setUserName(booking.getUser().getName());
        response.setStartAt(booking.getStartAt());
        response.setEndAt(booking.getEndAt());
        response.setPurpose(booking.getPurpose());
        response.setAttendeeCount(booking.getAttendeeCount());
        response.setStatus(booking.getStatus());
        response.setCheckinStatus(booking.getCheckinStatus());
        response.setCancellationReason(booking.getCancellationReason());
        response.setQrToken(booking.getQrToken());
        response.setApprovalTime(booking.getApprovalTime());
        response.setBookedAt(booking.getBookedAt());
        response.setCreatedAt(booking.getCreatedAt());
        response.setUpdatedAt(booking.getUpdatedAt());

        if (booking.getApprovedBy() != null) {
            response.setApprovedById(booking.getApprovedBy().getId());
            response.setApprovedByName(booking.getApprovedBy().getName());
        }

        long durationMinutes = 0;
        if (booking.getStartAt() != null && booking.getEndAt() != null && booking.getEndAt().isAfter(booking.getStartAt())) {
            durationMinutes = Duration.between(booking.getStartAt(), booking.getEndAt()).toMinutes();
        }
        response.setDurationMinutes(durationMinutes);

        return response;
    }
}
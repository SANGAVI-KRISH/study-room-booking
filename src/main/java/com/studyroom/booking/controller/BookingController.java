package com.studyroom.booking.controller;

import com.studyroom.booking.dto.BookingRequestDto;
import com.studyroom.booking.dto.BookingResponse;
import com.studyroom.booking.dto.BookingSummaryResponse;
import com.studyroom.booking.model.Booking;
import com.studyroom.booking.model.BookingStatus;
import com.studyroom.booking.model.TimeSlot;
import com.studyroom.booking.service.BookingPdfService;
import com.studyroom.booking.service.BookingService;
import com.studyroom.booking.service.TimeSlotService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDate;
import java.time.OffsetDateTime;
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
    private final TimeSlotService timeSlotService;

    public BookingController(
            BookingService bookingService,
            BookingPdfService bookingPdfService,
            TimeSlotService timeSlotService
    ) {
        this.bookingService = bookingService;
        this.bookingPdfService = bookingPdfService;
        this.timeSlotService = timeSlotService;
    }

    @GetMapping
    public ResponseEntity<List<BookingResponse>> getAllBookings() {
        List<BookingResponse> responses = bookingService.getAllBookings()
                .stream()
                .map(this::mapToBookingResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingResponse> getBookingById(@PathVariable UUID id) {
        Booking booking = bookingService.getBookingById(id);
        return ResponseEntity.ok(mapToBookingResponse(booking));
    }

    @GetMapping("/room/{roomId}")
    public ResponseEntity<List<BookingResponse>> getBookingsByRoomId(@PathVariable UUID roomId) {
        List<BookingResponse> responses = bookingService.getBookingsByRoomId(roomId)
                .stream()
                .map(this::mapToBookingResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<BookingResponse>> getBookingsByUserId(@PathVariable UUID userId) {
        List<BookingResponse> responses = bookingService.getBookingsByUserId(userId)
                .stream()
                .map(this::mapToBookingResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/user/{userId}/current")
    public ResponseEntity<List<BookingResponse>> getCurrentBookings(@PathVariable UUID userId) {
        List<BookingResponse> responses = bookingService.getCurrentBookings(userId)
                .stream()
                .map(this::mapToBookingResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/user/{userId}/past")
    public ResponseEntity<List<BookingResponse>> getPastBookings(@PathVariable UUID userId) {
        List<BookingResponse> responses = bookingService.getPastBookings(userId)
                .stream()
                .map(this::mapToBookingResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/user/{userId}/active")
    public ResponseEntity<List<BookingResponse>> getActiveCurrentBookings(@PathVariable UUID userId) {
        List<BookingResponse> responses = bookingService.getActiveCurrentBookings(userId)
                .stream()
                .map(this::mapToBookingResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/date/{date}")
    public ResponseEntity<List<BookingResponse>> getBookingsByDate(
            @PathVariable
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date
    ) {
        List<BookingResponse> responses = bookingService.getBookingsByDate(date)
                .stream()
                .map(this::mapToBookingResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<BookingResponse>> getBookingsByStatus(@PathVariable BookingStatus status) {
        List<BookingResponse> responses = bookingService.getBookingsByStatus(status)
                .stream()
                .map(this::mapToBookingResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/room/{roomId}/date/{date}")
    public ResponseEntity<List<BookingResponse>> getBookingsByRoomIdAndDate(
            @PathVariable UUID roomId,
            @PathVariable
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date
    ) {
        List<BookingResponse> responses = bookingService.getBookingsByRoomIdAndDate(roomId, date)
                .stream()
                .map(this::mapToBookingResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/user/{userId}/status/{status}")
    public ResponseEntity<List<BookingResponse>> getBookingsByUserIdAndStatus(
            @PathVariable UUID userId,
            @PathVariable BookingStatus status
    ) {
        List<BookingResponse> responses = bookingService.getBookingsByUserIdAndStatus(userId, status)
                .stream()
                .map(this::mapToBookingResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/room/{roomId}/status/{status}")
    public ResponseEntity<List<BookingResponse>> getBookingsByRoomIdAndStatus(
            @PathVariable UUID roomId,
            @PathVariable BookingStatus status
    ) {
        List<BookingResponse> responses = bookingService.getBookingsByRoomIdAndStatus(roomId, status)
                .stream()
                .map(this::mapToBookingResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/date/{date}/status/{status}")
    public ResponseEntity<List<BookingResponse>> getBookingsByDateAndStatus(
            @PathVariable
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date,
            @PathVariable BookingStatus status
    ) {
        List<BookingResponse> responses = bookingService.getBookingsByDateAndStatus(date, status)
                .stream()
                .map(this::mapToBookingResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/status/pending")
    public ResponseEntity<List<BookingResponse>> getPendingBookings() {
        List<BookingResponse> responses = bookingService.getPendingBookings()
                .stream()
                .map(this::mapToBookingResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/status/approved")
    public ResponseEntity<List<BookingResponse>> getApprovedBookings() {
        List<BookingResponse> responses = bookingService.getApprovedBookings()
                .stream()
                .map(this::mapToBookingResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/status/rejected")
    public ResponseEntity<List<BookingResponse>> getRejectedBookings() {
        List<BookingResponse> responses = bookingService.getRejectedBookings()
                .stream()
                .map(this::mapToBookingResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/status/cancelled")
    public ResponseEntity<List<BookingResponse>> getCancelledBookings() {
        List<BookingResponse> responses = bookingService.getCancelledBookings()
                .stream()
                .map(this::mapToBookingResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/status/checked-in")
    public ResponseEntity<List<BookingResponse>> getCheckedInBookings() {
        List<BookingResponse> responses = bookingService.getCheckedInBookings()
                .stream()
                .map(this::mapToBookingResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/status/completed")
    public ResponseEntity<List<BookingResponse>> getCompletedBookings() {
        List<BookingResponse> responses = bookingService.getCompletedBookings()
                .stream()
                .map(this::mapToBookingResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/status/no-show")
    public ResponseEntity<List<BookingResponse>> getNoShowBookings() {
        List<BookingResponse> responses = bookingService.getNoShowBookings()
                .stream()
                .map(this::mapToBookingResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/status/auto-cancelled")
    public ResponseEntity<List<BookingResponse>> getAutoCancelledBookings() {
        List<BookingResponse> responses = bookingService.getAutoCancelledBookings()
                .stream()
                .map(this::mapToBookingResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/user/{userId}/history")
    public ResponseEntity<List<BookingResponse>> getBookingHistory(@PathVariable UUID userId) {
        List<BookingResponse> responses = bookingService.getBookingHistory(userId)
                .stream()
                .map(this::mapToBookingResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/user/{userId}/history/completed")
    public ResponseEntity<List<BookingResponse>> getCompletedBookingsByUserId(@PathVariable UUID userId) {
        List<BookingResponse> responses = bookingService.getCompletedBookingsByUserId(userId)
                .stream()
                .map(this::mapToBookingResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/user/{userId}/history/cancelled")
    public ResponseEntity<List<BookingResponse>> getCancelledBookingsByUserId(@PathVariable UUID userId) {
        List<BookingResponse> responses = bookingService.getCancelledBookingsByUserId(userId)
                .stream()
                .map(this::mapToBookingResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/user/{userId}/history/rejected")
    public ResponseEntity<List<BookingResponse>> getRejectedBookingsByUserId(@PathVariable UUID userId) {
        List<BookingResponse> responses = bookingService.getRejectedBookingsByUserId(userId)
                .stream()
                .map(this::mapToBookingResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/user/{userId}/history/no-show")
    public ResponseEntity<List<BookingResponse>> getNoShowBookingsByUserId(@PathVariable UUID userId) {
        List<BookingResponse> responses = bookingService.getNoShowBookingsByUserId(userId)
                .stream()
                .map(this::mapToBookingResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/user/{userId}/history/auto-cancelled")
    public ResponseEntity<List<BookingResponse>> getAutoCancelledBookingsByUserId(@PathVariable UUID userId) {
        List<BookingResponse> responses = bookingService.getAutoCancelledBookingsByUserId(userId)
                .stream()
                .map(this::mapToBookingResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/user/{userId}/history/date/{date}")
    public ResponseEntity<List<BookingResponse>> getBookingHistoryByDate(
            @PathVariable UUID userId,
            @PathVariable
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date
    ) {
        List<BookingResponse> responses = bookingService.getBookingHistoryByDate(userId, date)
                .stream()
                .map(this::mapToBookingResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
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
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must be before or equal to end date");
        }

        List<BookingResponse> responses = bookingService.getBookingHistoryByDateRange(userId, startDate, endDate)
                .stream()
                .map(this::mapToBookingResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
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
        List<BookingResponse> responses = bookingService.getAllBookingHistoryForAdmin()
                .stream()
                .map(this::mapToBookingResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
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
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must be before or equal to end date");
        }

        List<BookingResponse> responses = bookingService.getAllBookingHistoryForAdminByDateRange(startDate, endDate)
                .stream()
                .map(this::mapToBookingResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @PostMapping("/summary")
    public ResponseEntity<BookingSummaryResponse> getBookingSummary(@RequestBody BookingRequestDto request) {
        validateBookingRequest(request);

        BookingSummaryResponse response = new BookingSummaryResponse();
        response.setRoomId(request.getRoomId());
        response.setRoomName(bookingService.getRoomNameById(request.getRoomId()));
        response.setUserId(request.getUserId());
        response.setPurpose(request.getPurpose());
        response.setAttendeeCount(request.getAttendeeCount());
        response.setStatus(BookingStatus.PENDING);

        if (request.getStartAt() != null && request.getEndAt() != null) {
            long durationMinutes = Duration.between(
                    request.getStartAt(),
                    request.getEndAt()
            ).toMinutes();

            response.setStartAt(request.getStartAt());
            response.setEndAt(request.getEndAt());
            response.setDurationMinutes(durationMinutes);
            response.setMessage("Booking summary generated successfully");
            return ResponseEntity.ok(response);
        }

        TimeSlot slot = timeSlotService.getSlotById(request.getTimeSlotId());

        long durationMinutes = Duration.between(
                slot.getStartAt(),
                slot.getEndAt()
        ).toMinutes();

        response.setStartAt(slot.getStartAt());
        response.setEndAt(slot.getEndAt());
        response.setDurationMinutes(durationMinutes);
        response.setMessage("Booking summary generated successfully");

        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(@RequestBody BookingRequestDto request) {
        validateBookingRequest(request);

        Booking booking;

        if (request.getStartAt() != null && request.getEndAt() != null) {
            booking = bookingService.createBooking(
                    request.getRoomId(),
                    request.getUserId(),
                    request.getStartAt(),
                    request.getEndAt(),
                    request.getPurpose(),
                    request.getAttendeeCount()
            );
        } else {
            booking = bookingService.createBooking(
                    request.getRoomId(),
                    request.getUserId(),
                    request.getTimeSlotId(),
                    request.getAttendeeCount(),
                    request.getPurpose()
            );
        }

        return ResponseEntity.ok(mapToBookingResponse(booking));
    }

    @PutMapping("/{id}/confirm")
    public ResponseEntity<BookingResponse> confirmBooking(
            @PathVariable UUID id,
            @RequestParam UUID approvedBy
    ) {
        Booking booking = bookingService.approveBooking(id, approvedBy);
        return ResponseEntity.ok(mapToBookingResponse(booking));
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<BookingResponse> approveBooking(
            @PathVariable UUID id,
            @RequestParam UUID approvedBy
    ) {
        Booking booking = bookingService.approveBooking(id, approvedBy);
        return ResponseEntity.ok(mapToBookingResponse(booking));
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<BookingResponse> rejectBooking(@PathVariable UUID id) {
        Booking booking = bookingService.rejectBooking(id);
        return ResponseEntity.ok(mapToBookingResponse(booking));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<BookingResponse> cancelBooking(@PathVariable UUID id) {
        Booking booking = bookingService.cancelBooking(id);
        return ResponseEntity.ok(mapToBookingResponse(booking));
    }

    @PutMapping("/{id}/complete")
    public ResponseEntity<BookingResponse> completeBooking(@PathVariable UUID id) {
        Booking booking = bookingService.completeBooking(id);
        return ResponseEntity.ok(mapToBookingResponse(booking));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<BookingResponse> updateBookingStatus(
            @PathVariable UUID id,
            @RequestParam BookingStatus status
    ) {
        Booking booking = bookingService.updateBookingStatus(id, status);
        return ResponseEntity.ok(mapToBookingResponse(booking));
    }

    @PutMapping("/{id}/reschedule")
    public ResponseEntity<BookingResponse> rescheduleBooking(
            @PathVariable UUID id,
            @RequestBody BookingRequestDto request
    ) {
        validateRescheduleRequest(request);

        OffsetDateTimeRange range = resolveBookingRange(request);

        Booking booking = bookingService.rescheduleBooking(
                id,
                range.startAt(),
                range.endAt()
        );

        return ResponseEntity.ok(mapToBookingResponse(booking));
    }

    @PostMapping("/{id}/check-in")
    public ResponseEntity<BookingResponse> checkInBooking(
            @PathVariable UUID id,
            @RequestParam UUID userId
    ) {
        Booking booking = bookingService.checkInBooking(id, userId);
        return ResponseEntity.ok(mapToBookingResponse(booking));
    }

    @GetMapping("/{id}/can-check-in")
    public ResponseEntity<Boolean> canCheckInBooking(
            @PathVariable UUID id,
            @RequestParam UUID userId
    ) {
        boolean canCheckIn = bookingService.canCheckIn(id, userId);
        return ResponseEntity.ok(canCheckIn);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteBooking(@PathVariable UUID id) {
        bookingService.deleteBooking(id);
        return ResponseEntity.ok("Booking deleted successfully");
    }

    private void validateBookingRequest(BookingRequestDto request) {
        if (request == null) {
            throw new IllegalArgumentException("Booking request is required");
        }
        if (request.getRoomId() == null) {
            throw new IllegalArgumentException("Room ID is required");
        }
        if (request.getUserId() == null) {
            throw new IllegalArgumentException("User ID is required");
        }
        if (request.getAttendeeCount() == null || request.getAttendeeCount() <= 0) {
            throw new IllegalArgumentException("Attendee count must be greater than 0");
        }

        boolean hasCustomRange = request.getStartAt() != null || request.getEndAt() != null;
        boolean hasSlotId = request.getTimeSlotId() != null;

        if (hasCustomRange) {
            if (request.getStartAt() == null || request.getEndAt() == null) {
                throw new IllegalArgumentException("Both startAt and endAt are required");
            }
            if (!request.getEndAt().isAfter(request.getStartAt())) {
                throw new IllegalArgumentException("endAt must be after startAt");
            }
            return;
        }

        if (!hasSlotId) {
            throw new IllegalArgumentException("Either timeSlotId or startAt/endAt is required");
        }
    }

    private void validateRescheduleRequest(BookingRequestDto request) {
        if (request == null) {
            throw new IllegalArgumentException("Reschedule request is required");
        }

        boolean hasCustomRange = request.getStartAt() != null || request.getEndAt() != null;
        boolean hasSlotId = request.getTimeSlotId() != null;

        if (hasCustomRange) {
            if (request.getStartAt() == null || request.getEndAt() == null) {
                throw new IllegalArgumentException("Both startAt and endAt are required");
            }
            if (!request.getEndAt().isAfter(request.getStartAt())) {
                throw new IllegalArgumentException("endAt must be after startAt");
            }
            return;
        }

        if (!hasSlotId) {
            throw new IllegalArgumentException("Either timeSlotId or startAt/endAt is required");
        }
    }

    private OffsetDateTimeRange resolveBookingRange(BookingRequestDto request) {
        if (request.getStartAt() != null && request.getEndAt() != null) {
            return new OffsetDateTimeRange(request.getStartAt(), request.getEndAt());
        }

        TimeSlot slot = timeSlotService.getSlotById(request.getTimeSlotId());
        return new OffsetDateTimeRange(slot.getStartAt(), slot.getEndAt());
    }

    private BookingResponse mapToBookingResponse(Booking booking) {
        BookingResponse response = new BookingResponse();

        response.setBookingId(booking.getId());

        if (booking.getRoom() != null) {
            response.setRoomId(booking.getRoom().getId());

            String roomName = booking.getRoom().getDisplayName();
            if (roomName == null || roomName.isBlank()) {
                String blockName = booking.getRoom().getBlockName() != null ? booking.getRoom().getBlockName() : "";
                String roomNumber = booking.getRoom().getRoomNumber() != null ? booking.getRoom().getRoomNumber() : "Room";
                roomName = blockName.isBlank() ? roomNumber : blockName + " - " + roomNumber;
            }
            response.setRoomName(roomName);
        }

        if (booking.getUser() != null) {
            response.setUserId(booking.getUser().getId());
            response.setUserName(booking.getUser().getName());
        }

        response.setStartAt(booking.getStartAt());
        response.setEndAt(booking.getEndAt());
        response.setPurpose(booking.getPurpose());
        response.setAttendeeCount(booking.getAttendeeCount());
        response.setStatus(booking.getStatus() != null ? booking.getStatus().getValue() : null);
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
        if (booking.getStartAt() != null
                && booking.getEndAt() != null
                && booking.getEndAt().isAfter(booking.getStartAt())) {
            durationMinutes = Duration.between(booking.getStartAt(), booking.getEndAt()).toMinutes();
        }
        response.setDurationMinutes(durationMinutes);

        response.setCheckedInAt(booking.getCheckedInAt());
        response.setIsPresent(booking.getIsPresent());
        response.setAttendanceMarkedAt(booking.getAttendanceMarkedAt());
        response.setFeedbackSubmitted(booking.getFeedbackSubmitted());

        response.setCheckInDeadline(booking.getCheckInDeadline());
        response.setAutoCancelledAt(booking.getAutoCancelledAt());

        return response;
    }

    private record OffsetDateTimeRange(OffsetDateTime startAt, OffsetDateTime endAt) {
    }
}
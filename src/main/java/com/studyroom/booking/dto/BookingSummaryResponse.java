package com.studyroom.booking.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.studyroom.booking.model.BookingStatus;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;

public class BookingSummaryResponse {

    private UUID roomId;
    private String roomName;
    private UUID userId;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    private OffsetDateTime startAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    private OffsetDateTime endAt;

    private String purpose;
    private Integer attendeeCount;

    private long durationMinutes;   // 🔥 renamed to match controller
    private BookingStatus status;
    private String message;

    public BookingSummaryResponse() {
    }

    public BookingSummaryResponse(UUID roomId,
                                  String roomName,
                                  UUID userId,
                                  OffsetDateTime startAt,
                                  OffsetDateTime endAt,
                                  String purpose,
                                  Integer attendeeCount,
                                  BookingStatus status,
                                  String message) {
        this.roomId = roomId;
        this.roomName = roomName;
        this.userId = userId;
        this.startAt = startAt;
        this.endAt = endAt;
        this.purpose = purpose;
        this.attendeeCount = attendeeCount;
        this.status = status;
        this.message = message;
        this.durationMinutes = calculateDuration(startAt, endAt);
    }

    private long calculateDuration(OffsetDateTime startAt, OffsetDateTime endAt) {
        if (startAt != null && endAt != null && endAt.isAfter(startAt)) {
            return Duration.between(startAt, endAt).toMinutes();
        }
        return 0;
    }

    private void recalculateDuration() {
        this.durationMinutes = calculateDuration(this.startAt, this.endAt);
    }

    // -------- GETTERS & SETTERS --------

    public UUID getRoomId() {
        return roomId;
    }

    public void setRoomId(UUID roomId) {
        this.roomId = roomId;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public OffsetDateTime getStartAt() {
        return startAt;
    }

    public void setStartAt(OffsetDateTime startAt) {
        this.startAt = startAt;
        recalculateDuration();
    }

    public OffsetDateTime getEndAt() {
        return endAt;
    }

    public void setEndAt(OffsetDateTime endAt) {
        this.endAt = endAt;
        recalculateDuration();
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public Integer getAttendeeCount() {
        return attendeeCount;
    }

    public void setAttendeeCount(Integer attendeeCount) {
        this.attendeeCount = attendeeCount;
    }

    public long getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(long durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
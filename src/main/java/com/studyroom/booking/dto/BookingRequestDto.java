package com.studyroom.booking.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.OffsetDateTime;
import java.util.UUID;

public class BookingRequestDto {

    @NotNull(message = "Room ID is required")
    private UUID roomId;

    @NotNull(message = "User ID is required")
    private UUID userId;

    @NotNull(message = "Start date and time is required")
    @Future(message = "Start date and time must be in the future")
    private OffsetDateTime startAt;

    @NotNull(message = "End date and time is required")
    private OffsetDateTime endAt;

    private String purpose;

    @Positive(message = "Attendee count must be greater than 0")
    private Integer attendeeCount;

    public BookingRequestDto() {
    }

    public BookingRequestDto(UUID roomId,
                             UUID userId,
                             OffsetDateTime startAt,
                             OffsetDateTime endAt,
                             String purpose,
                             Integer attendeeCount) {
        this.roomId = roomId;
        this.userId = userId;
        this.startAt = startAt;
        this.endAt = endAt;
        this.purpose = purpose;
        this.attendeeCount = attendeeCount;
    }

    // ----------- VALIDATION METHOD (IMPORTANT) -----------

    public boolean isValidTimeRange() {
        return startAt != null && endAt != null && endAt.isAfter(startAt);
    }

    // ----------- GETTERS & SETTERS -----------

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

    public OffsetDateTime getStartAt() {
        return startAt;
    }

    public void setStartAt(OffsetDateTime startAt) {
        this.startAt = startAt;
    }

    public OffsetDateTime getEndAt() {
        return endAt;
    }

    public void setEndAt(OffsetDateTime endAt) {
        this.endAt = endAt;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose != null ? purpose.trim() : null;
    }

    public Integer getAttendeeCount() {
        return attendeeCount;
    }

    public void setAttendeeCount(Integer attendeeCount) {
        if (attendeeCount != null && attendeeCount <= 0) {
            throw new IllegalArgumentException("Attendee count must be greater than 0");
        }
        this.attendeeCount = attendeeCount;
    }
}
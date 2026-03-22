package com.studyroom.booking.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.OffsetDateTime;
import java.util.UUID;

public class BookingRequestDto {

    @NotNull(message = "Room ID is required")
    private UUID roomId;

    @NotNull(message = "User ID is required")
    private UUID userId;

    // Optional for flexible booking flow
    private UUID timeSlotId;

    // New: custom booking start and end
    private OffsetDateTime startAt;
    private OffsetDateTime endAt;

    private String purpose;

    @NotNull(message = "Attendee count is required")
    @Positive(message = "Attendee count must be greater than 0")
    private Integer attendeeCount;

    public BookingRequestDto() {
    }

    public BookingRequestDto(
            UUID roomId,
            UUID userId,
            UUID timeSlotId,
            OffsetDateTime startAt,
            OffsetDateTime endAt,
            String purpose,
            Integer attendeeCount
    ) {
        this.roomId = roomId;
        this.userId = userId;
        this.timeSlotId = timeSlotId;
        this.startAt = startAt;
        this.endAt = endAt;
        this.purpose = purpose;
        this.attendeeCount = attendeeCount;
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

    public UUID getTimeSlotId() {
        return timeSlotId;
    }

    public void setTimeSlotId(UUID timeSlotId) {
        this.timeSlotId = timeSlotId;
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
        this.attendeeCount = attendeeCount;
    }
}
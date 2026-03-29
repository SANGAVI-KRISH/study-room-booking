package com.studyroom.booking.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;
import java.util.UUID;

public class WaitlistRequestDto {

    @NotNull(message = "User ID is required")
    private UUID userId;

    @NotNull(message = "Room ID is required")
    private UUID roomId;

    @NotNull(message = "Start time is required")
    @Future(message = "Start time must be in the future")
    private OffsetDateTime startAt;

    @NotNull(message = "End time is required")
    private OffsetDateTime endAt;

    private Boolean autoAssign = false;

    public WaitlistRequestDto() {
    }

    public WaitlistRequestDto(UUID userId,
                              UUID roomId,
                              OffsetDateTime startAt,
                              OffsetDateTime endAt,
                              Boolean autoAssign) {
        this.userId = userId;
        this.roomId = roomId;
        this.startAt = startAt;
        this.endAt = endAt;
        this.autoAssign = autoAssign;
    }

    public boolean isValidTimeRange() {
        return startAt != null && endAt != null && endAt.isAfter(startAt);
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public UUID getRoomId() {
        return roomId;
    }

    public void setRoomId(UUID roomId) {
        this.roomId = roomId;
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

    public Boolean getAutoAssign() {
        return autoAssign;
    }

    public void setAutoAssign(Boolean autoAssign) {
        this.autoAssign = autoAssign;
    }
}
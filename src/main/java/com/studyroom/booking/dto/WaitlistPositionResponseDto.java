package com.studyroom.booking.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public class WaitlistPositionResponseDto {

    private UUID waitlistId;
    private UUID userId;
    private UUID roomId;
    private OffsetDateTime startAt;
    private OffsetDateTime endAt;
    private Integer position;
    private String status;
    private Boolean autoAssign;
    private OffsetDateTime notifiedAt;
    private OffsetDateTime expiresAt;
    private String message;

    public WaitlistPositionResponseDto() {
    }

    public WaitlistPositionResponseDto(UUID waitlistId,
                                       UUID userId,
                                       UUID roomId,
                                       OffsetDateTime startAt,
                                       OffsetDateTime endAt,
                                       Integer position,
                                       String status,
                                       Boolean autoAssign,
                                       OffsetDateTime notifiedAt,
                                       OffsetDateTime expiresAt,
                                       String message) {
        this.waitlistId = waitlistId;
        this.userId = userId;
        this.roomId = roomId;
        this.startAt = startAt;
        this.endAt = endAt;
        this.position = position;
        this.status = status;
        this.autoAssign = autoAssign;
        this.notifiedAt = notifiedAt;
        this.expiresAt = expiresAt;
        this.message = message;
    }

    public UUID getWaitlistId() {
        return waitlistId;
    }

    public void setWaitlistId(UUID waitlistId) {
        this.waitlistId = waitlistId;
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

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Boolean getAutoAssign() {
        return autoAssign;
    }

    public void setAutoAssign(Boolean autoAssign) {
        this.autoAssign = autoAssign;
    }

    public OffsetDateTime getNotifiedAt() {
        return notifiedAt;
    }

    public void setNotifiedAt(OffsetDateTime notifiedAt) {
        this.notifiedAt = notifiedAt;
    }

    public OffsetDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(OffsetDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
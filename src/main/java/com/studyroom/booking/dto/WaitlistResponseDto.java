package com.studyroom.booking.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public class WaitlistResponseDto {

    private UUID id;
    private UUID userId;
    private String userName;

    private UUID roomId;
    private String roomName;

    private OffsetDateTime startAt;
    private OffsetDateTime endAt;

    private Integer position;
    private String status;

    private Boolean autoAssign;

    private OffsetDateTime notifiedAt;
    private OffsetDateTime expiresAt;
    private OffsetDateTime assignedAt;
    private OffsetDateTime cancelledAt;

    private String message;

    public WaitlistResponseDto() {
    }

    public WaitlistResponseDto(UUID id,
                               UUID userId,
                               String userName,
                               UUID roomId,
                               String roomName,
                               OffsetDateTime startAt,
                               OffsetDateTime endAt,
                               Integer position,
                               String status,
                               Boolean autoAssign,
                               OffsetDateTime notifiedAt,
                               OffsetDateTime expiresAt,
                               OffsetDateTime assignedAt,
                               OffsetDateTime cancelledAt,
                               String message) {
        this.id = id;
        this.userId = userId;
        this.userName = userName;
        this.roomId = roomId;
        this.roomName = roomName;
        this.startAt = startAt;
        this.endAt = endAt;
        this.position = position;
        this.status = status;
        this.autoAssign = autoAssign;
        this.notifiedAt = notifiedAt;
        this.expiresAt = expiresAt;
        this.assignedAt = assignedAt;
        this.cancelledAt = cancelledAt;
        this.message = message;
    }

    // ================= GETTERS & SETTERS =================

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

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

    public OffsetDateTime getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(OffsetDateTime assignedAt) {
        this.assignedAt = assignedAt;
    }

    public OffsetDateTime getCancelledAt() {
        return cancelledAt;
    }

    public void setCancelledAt(OffsetDateTime cancelledAt) {
        this.cancelledAt = cancelledAt;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
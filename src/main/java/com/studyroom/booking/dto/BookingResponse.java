package com.studyroom.booking.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.OffsetDateTime;
import java.util.UUID;

public class BookingResponse {

    private UUID bookingId;
    private UUID roomId;
    private String roomName;
    private UUID userId;
    private String userName;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    private OffsetDateTime startAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    private OffsetDateTime endAt;

    private String purpose;
    private Integer attendeeCount;
    private Double totalPrice;
    private long durationMinutes;
    private String status;
    private String checkinStatus;
    private String cancellationReason;
    private String qrToken;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    private OffsetDateTime approvalTime;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    private OffsetDateTime bookedAt;

    private UUID approvedById;
    private String approvedByName;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    private OffsetDateTime checkedInAt;

    private Boolean isPresent;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    private OffsetDateTime attendanceMarkedAt;

    private Boolean feedbackSubmitted;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    private OffsetDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    private OffsetDateTime updatedAt;

    public BookingResponse() {
    }

    public BookingResponse(
            UUID bookingId,
            UUID roomId,
            String roomName,
            UUID userId,
            String userName,
            OffsetDateTime startAt,
            OffsetDateTime endAt,
            String purpose,
            Integer attendeeCount,
            Double totalPrice,
            long durationMinutes,
            String status,
            String checkinStatus,
            String cancellationReason,
            String qrToken,
            OffsetDateTime approvalTime,
            OffsetDateTime bookedAt,
            UUID approvedById,
            String approvedByName,
            OffsetDateTime checkedInAt,
            Boolean isPresent,
            OffsetDateTime attendanceMarkedAt,
            Boolean feedbackSubmitted,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt
    ) {
        this.bookingId = bookingId;
        this.roomId = roomId;
        this.roomName = roomName;
        this.userId = userId;
        this.userName = userName;
        this.startAt = startAt;
        this.endAt = endAt;
        this.purpose = purpose;
        this.attendeeCount = attendeeCount;
        this.totalPrice = totalPrice;
        this.durationMinutes = durationMinutes;
        this.status = status;
        this.checkinStatus = checkinStatus;
        this.cancellationReason = cancellationReason;
        this.qrToken = qrToken;
        this.approvalTime = approvalTime;
        this.bookedAt = bookedAt;
        this.approvedById = approvedById;
        this.approvedByName = approvedByName;
        this.checkedInAt = checkedInAt;
        this.isPresent = isPresent;
        this.attendanceMarkedAt = attendanceMarkedAt;
        this.feedbackSubmitted = feedbackSubmitted;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getBookingId() {
        return bookingId;
    }

    public void setBookingId(UUID bookingId) {
        this.bookingId = bookingId;
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
        this.purpose = purpose;
    }

    public Integer getAttendeeCount() {
        return attendeeCount;
    }

    public void setAttendeeCount(Integer attendeeCount) {
        this.attendeeCount = attendeeCount;
    }

    public Double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(Double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public long getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(long durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCheckinStatus() {
        return checkinStatus;
    }

    public void setCheckinStatus(String checkinStatus) {
        this.checkinStatus = checkinStatus;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }

    public String getQrToken() {
        return qrToken;
    }

    public void setQrToken(String qrToken) {
        this.qrToken = qrToken;
    }

    public OffsetDateTime getApprovalTime() {
        return approvalTime;
    }

    public void setApprovalTime(OffsetDateTime approvalTime) {
        this.approvalTime = approvalTime;
    }

    public OffsetDateTime getBookedAt() {
        return bookedAt;
    }

    public void setBookedAt(OffsetDateTime bookedAt) {
        this.bookedAt = bookedAt;
    }

    public UUID getApprovedById() {
        return approvedById;
    }

    public void setApprovedById(UUID approvedById) {
        this.approvedById = approvedById;
    }

    public String getApprovedByName() {
        return approvedByName;
    }

    public void setApprovedByName(String approvedByName) {
        this.approvedByName = approvedByName;
    }

    public OffsetDateTime getCheckedInAt() {
        return checkedInAt;
    }

    public void setCheckedInAt(OffsetDateTime checkedInAt) {
        this.checkedInAt = checkedInAt;
    }

    public Boolean getIsPresent() {
        return isPresent;
    }

    public void setIsPresent(Boolean isPresent) {
        this.isPresent = isPresent;
    }

    public OffsetDateTime getAttendanceMarkedAt() {
        return attendanceMarkedAt;
    }

    public void setAttendanceMarkedAt(OffsetDateTime attendanceMarkedAt) {
        this.attendanceMarkedAt = attendanceMarkedAt;
    }

    public Boolean getFeedbackSubmitted() {
        return feedbackSubmitted;
    }

    public void setFeedbackSubmitted(Boolean feedbackSubmitted) {
        this.feedbackSubmitted = feedbackSubmitted;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
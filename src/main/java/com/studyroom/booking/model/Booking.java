package com.studyroom.booking.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "room_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private StudyRoom room;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password"})
    private User user;

    @Column(name = "start_at", nullable = false)
    private OffsetDateTime startAt;

    @Column(name = "end_at", nullable = false)
    private OffsetDateTime endAt;

    @Column(name = "purpose")
    private String purpose;

    @Column(name = "attendee_count")
    private Integer attendeeCount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private BookingStatus status = BookingStatus.PENDING;

    @Column(name = "checkin_status", nullable = false, length = 20)
    private String checkinStatus = "not_checked_in";

    @Column(name = "cancellation_reason")
    private String cancellationReason;

    @Column(name = "approval_time")
    private OffsetDateTime approvalTime;

    @Column(name = "booked_at", insertable = false, updatable = false)
    private OffsetDateTime bookedAt;

    @Column(name = "qr_token")
    private String qrToken;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "approved_by")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password"})
    private User approvedBy;

    @Column(name = "reminder_sent", nullable = false)
    private Boolean reminderSent = false;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime updatedAt;

    public Booking() {
    }

    public Booking(UUID id,
                   StudyRoom room,
                   User user,
                   OffsetDateTime startAt,
                   OffsetDateTime endAt,
                   String purpose,
                   Integer attendeeCount,
                   BookingStatus status,
                   String checkinStatus,
                   String cancellationReason,
                   OffsetDateTime approvalTime,
                   OffsetDateTime bookedAt,
                   String qrToken,
                   User approvedBy,
                   Boolean reminderSent,
                   OffsetDateTime createdAt,
                   OffsetDateTime updatedAt) {
        this.id = id;
        this.room = room;
        this.user = user;
        this.startAt = startAt;
        this.endAt = endAt;
        this.purpose = purpose;
        this.attendeeCount = attendeeCount;
        this.status = status;
        this.checkinStatus = checkinStatus;
        this.cancellationReason = cancellationReason;
        this.approvalTime = approvalTime;
        this.bookedAt = bookedAt;
        this.qrToken = qrToken;
        this.approvedBy = approvedBy;
        this.reminderSent = reminderSent;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    @PrePersist
    public void prePersist() {
        applyDefaults();
    }

    @PreUpdate
    public void preUpdate() {
        applyDefaults();
    }

    private void applyDefaults() {
        if (this.status == null) {
            this.status = BookingStatus.PENDING;
        }
        if (this.checkinStatus == null || this.checkinStatus.isBlank()) {
            this.checkinStatus = "not_checked_in";
        }
        if (this.reminderSent == null) {
            this.reminderSent = false;
        }
        if (this.purpose != null) {
            this.purpose = this.purpose.trim();
        }
    }

    public boolean isValidTimeRange() {
        return startAt != null && endAt != null && endAt.isAfter(startAt);
    }

    public boolean isInPast() {
        return startAt != null && startAt.isBefore(OffsetDateTime.now());
    }

    public boolean isUpcoming() {
        return startAt != null && startAt.isAfter(OffsetDateTime.now());
    }

    public long getDurationMinutes() {
        if (!isValidTimeRange()) {
            return 0;
        }
        return Duration.between(startAt, endAt).toMinutes();
    }

    public boolean overlapsWith(OffsetDateTime otherStartAt, OffsetDateTime otherEndAt) {
        if (startAt == null || endAt == null || otherStartAt == null || otherEndAt == null) {
            return false;
        }
        return startAt.isBefore(otherEndAt) && endAt.isAfter(otherStartAt);
    }

    public boolean overlapsWith(Booking other) {
        if (other == null) {
            return false;
        }
        return overlapsWith(other.getStartAt(), other.getEndAt());
    }

    public boolean isPending() {
        return BookingStatus.PENDING.equals(this.status);
    }

    public boolean isApproved() {
        return BookingStatus.APPROVED.equals(this.status);
    }

    public boolean isRejected() {
        return BookingStatus.REJECTED.equals(this.status);
    }

    public boolean isCancelled() {
        return BookingStatus.CANCELLED.equals(this.status);
    }

    public boolean isCompleted() {
        return BookingStatus.COMPLETED.equals(this.status);
    }

    public boolean isAutoCancelled() {
        return BookingStatus.AUTO_CANCELLED.equals(this.status);
    }

    public boolean isHistoryBooking() {
        return BookingStatus.COMPLETED.equals(this.status)
                || BookingStatus.CANCELLED.equals(this.status)
                || BookingStatus.REJECTED.equals(this.status)
                || BookingStatus.AUTO_CANCELLED.equals(this.status);
    }

    public boolean isActiveBooking() {
        return BookingStatus.PENDING.equals(this.status)
                || BookingStatus.APPROVED.equals(this.status);
    }

    public boolean canBeCancelled() {
        return isActiveBooking() && endAt != null && endAt.isAfter(OffsetDateTime.now());
    }

    public boolean canBeRescheduled() {
        return isActiveBooking() && startAt != null && startAt.isAfter(OffsetDateTime.now());
    }

    public boolean canBeConfirmed() {
        return BookingStatus.PENDING.equals(this.status);
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public StudyRoom getRoom() {
        return room;
    }

    public void setRoom(StudyRoom room) {
        this.room = room;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
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

    public String getQrToken() {
        return qrToken;
    }

    public void setQrToken(String qrToken) {
        this.qrToken = qrToken;
    }

    public User getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(User approvedBy) {
        this.approvedBy = approvedBy;
    }

    public Boolean getReminderSent() {
        return reminderSent;
    }

    public void setReminderSent(Boolean reminderSent) {
        this.reminderSent = reminderSent;
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
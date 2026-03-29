package com.studyroom.booking.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "bookings")
public class Booking {

    private static final long DEFAULT_CHECKIN_GRACE_MINUTES = 15;

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

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "approved_by")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password"})
    private User approvedBy;

    @Column(name = "start_at", nullable = false)
    private OffsetDateTime startAt;

    @Column(name = "end_at", nullable = false)
    private OffsetDateTime endAt;

    @Column(name = "purpose")
    private String purpose;

    @Column(name = "attendee_count", nullable = false)
    private Integer attendeeCount;

    @Convert(converter = BookingStatusConverter.class)
    @Column(name = "status", nullable = false, length = 20)
    private BookingStatus status = BookingStatus.PENDING;

    @Column(name = "checkin_status", nullable = false, length = 20)
    private String checkinStatus = "not_checked_in";

    @Column(name = "check_in_deadline")
    private OffsetDateTime checkInDeadline;

    @Column(name = "checked_in_at")
    private OffsetDateTime checkedInAt;

    @Column(name = "auto_cancelled_at")
    private OffsetDateTime autoCancelledAt;

    @Column(name = "is_present", nullable = false)
    private Boolean isPresent = false;

    @Column(name = "attendance_marked_at")
    private OffsetDateTime attendanceMarkedAt;

    @Column(name = "feedback_submitted", nullable = false)
    private Boolean feedbackSubmitted = false;

    @Column(name = "cancellation_reason")
    private String cancellationReason;

    @Column(name = "approval_time")
    private OffsetDateTime approvalTime;

    @Column(name = "qr_token")
    private String qrToken;

    @Column(name = "reminder_sent", nullable = false)
    private Boolean reminderSent = false;

    @Column(name = "booked_at", insertable = false, updatable = false)
    private OffsetDateTime bookedAt;

    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private OffsetDateTime updatedAt;

    public Booking() {}

    public Booking(StudyRoom room,
                   User user,
                   OffsetDateTime startAt,
                   OffsetDateTime endAt,
                   String purpose,
                   Integer attendeeCount) {
        this.room = room;
        this.user = user;
        this.startAt = startAt;
        this.endAt = endAt;
        this.purpose = purpose;
        this.attendeeCount = attendeeCount;
        this.status = BookingStatus.PENDING;
        this.checkinStatus = "not_checked_in";
        this.isPresent = false;
        this.feedbackSubmitted = false;
        this.reminderSent = false;
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

        if (this.isPresent == null) {
            this.isPresent = false;
        }

        if (this.feedbackSubmitted == null) {
            this.feedbackSubmitted = false;
        }

        if (this.attendeeCount == null || this.attendeeCount <= 0) {
            this.attendeeCount = 1;
        }

        if (this.purpose != null) {
            this.purpose = this.purpose.trim();
            if (this.purpose.isBlank()) {
                this.purpose = null;
            }
        }

        if (this.status == BookingStatus.APPROVED && this.approvalTime == null) {
            this.approvalTime = OffsetDateTime.now();
        }

        if (this.status == BookingStatus.APPROVED && this.checkInDeadline == null && this.startAt != null) {
            this.checkInDeadline = this.startAt.plusMinutes(DEFAULT_CHECKIN_GRACE_MINUTES);
        }
    }

    // ================= BUSINESS METHODS =================

    public boolean isValidTimeRange() {
        return startAt != null && endAt != null && endAt.isAfter(startAt);
    }

    public boolean isInPast() {
        return startAt != null && startAt.isBefore(OffsetDateTime.now());
    }

    public boolean isUpcoming() {
        return startAt != null && startAt.isAfter(OffsetDateTime.now());
    }

    public boolean isOngoing() {
        OffsetDateTime now = OffsetDateTime.now();
        return startAt != null
                && endAt != null
                && (startAt.isBefore(now) || startAt.isEqual(now))
                && endAt.isAfter(now);
    }

    public boolean isEnded() {
        return endAt != null && (endAt.isBefore(OffsetDateTime.now()) || endAt.isEqual(OffsetDateTime.now()));
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
        return other != null && overlapsWith(other.getStartAt(), other.getEndAt());
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

    public boolean isCheckedIn() {
        return BookingStatus.CHECKED_IN.equals(this.status);
    }

    public boolean isCompleted() {
        return BookingStatus.COMPLETED.equals(this.status);
    }

    public boolean isNoShow() {
        return BookingStatus.NO_SHOW.equals(this.status);
    }

    public boolean isAutoCancelled() {
        return BookingStatus.AUTO_CANCELLED.equals(this.status);
    }

    public boolean isActiveBooking() {
        return status != null && status.isActive();
    }

    public boolean canBeCancelled() {
        return status != null && status.canBeCancelled();
    }

    public boolean canBeCheckedIn() {
        return status != null && status.canBeCheckedIn();
    }

    public boolean canBeCompleted() {
        return status != null && status.canBeCompleted();
    }

    public boolean hasCheckInDeadlinePassed() {
        return checkInDeadline != null && OffsetDateTime.now().isAfter(checkInDeadline);
    }

    public boolean isEligibleForAutoCancellation() {
        return isApproved()
                && !"checked_in".equalsIgnoreCase(checkinStatus)
                && hasCheckInDeadlinePassed();
    }

    public boolean freesSlot() {
        return status != null && status.freesSlot();
    }

    public void approve(User approvedByUser) {
        this.status = BookingStatus.APPROVED;
        this.approvedBy = approvedByUser;
        this.approvalTime = OffsetDateTime.now();
        this.checkinStatus = "not_checked_in";

        if (this.startAt != null) {
            this.checkInDeadline = this.startAt.plusMinutes(DEFAULT_CHECKIN_GRACE_MINUTES);
        }
    }

    public void reject(String reason) {
        this.status = BookingStatus.REJECTED;
        this.cancellationReason = reason;
    }

    public void cancel(String reason) {
        this.status = BookingStatus.CANCELLED;
        this.cancellationReason = reason;
    }

    public void markCheckedIn() {
        this.status = BookingStatus.CHECKED_IN;
        this.checkinStatus = "checked_in";
        this.checkedInAt = OffsetDateTime.now();
        this.isPresent = true;
        this.attendanceMarkedAt = this.checkedInAt;
    }

    public void markNoShow() {
        this.status = BookingStatus.NO_SHOW;
        this.checkinStatus = "late";
        this.isPresent = false;
        this.attendanceMarkedAt = OffsetDateTime.now();
    }

    public void autoCancel(String reason) {
        this.status = BookingStatus.AUTO_CANCELLED;
        this.cancellationReason = reason;
        this.autoCancelledAt = OffsetDateTime.now();
        this.isPresent = false;

        if (this.checkinStatus == null || this.checkinStatus.isBlank() || "not_checked_in".equalsIgnoreCase(this.checkinStatus)) {
            this.checkinStatus = "not_checked_in";
        }
    }

    public void complete() {
        this.status = BookingStatus.COMPLETED;
    }

    // ================= GETTERS & SETTERS =================

    public UUID getId() {
        return id;
    }

    public StudyRoom getRoom() {
        return room;
    }

    public User getUser() {
        return user;
    }

    public User getApprovedBy() {
        return approvedBy;
    }

    public OffsetDateTime getStartAt() {
        return startAt;
    }

    public OffsetDateTime getEndAt() {
        return endAt;
    }

    public String getPurpose() {
        return purpose;
    }

    public Integer getAttendeeCount() {
        return attendeeCount;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public String getCheckinStatus() {
        return checkinStatus;
    }

    public OffsetDateTime getCheckInDeadline() {
        return checkInDeadline;
    }

    public OffsetDateTime getCheckedInAt() {
        return checkedInAt;
    }

    public OffsetDateTime getAutoCancelledAt() {
        return autoCancelledAt;
    }

    public Boolean getIsPresent() {
        return isPresent;
    }

    public OffsetDateTime getAttendanceMarkedAt() {
        return attendanceMarkedAt;
    }

    public Boolean getFeedbackSubmitted() {
        return feedbackSubmitted;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public OffsetDateTime getApprovalTime() {
        return approvalTime;
    }

    public String getQrToken() {
        return qrToken;
    }

    public Boolean getReminderSent() {
        return reminderSent;
    }

    public OffsetDateTime getBookedAt() {
        return bookedAt;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setRoom(StudyRoom room) {
        this.room = room;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setApprovedBy(User approvedBy) {
        this.approvedBy = approvedBy;
    }

    public void setStartAt(OffsetDateTime startAt) {
        this.startAt = startAt;
    }

    public void setEndAt(OffsetDateTime endAt) {
        this.endAt = endAt;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public void setAttendeeCount(Integer attendeeCount) {
        this.attendeeCount = attendeeCount;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    public void setCheckinStatus(String checkinStatus) {
        this.checkinStatus = checkinStatus;
    }

    public void setCheckInDeadline(OffsetDateTime checkInDeadline) {
        this.checkInDeadline = checkInDeadline;
    }

    public void setCheckedInAt(OffsetDateTime checkedInAt) {
        this.checkedInAt = checkedInAt;
    }

    public void setAutoCancelledAt(OffsetDateTime autoCancelledAt) {
        this.autoCancelledAt = autoCancelledAt;
    }

    public void setIsPresent(Boolean isPresent) {
        this.isPresent = isPresent;
    }

    public void setAttendanceMarkedAt(OffsetDateTime attendanceMarkedAt) {
        this.attendanceMarkedAt = attendanceMarkedAt;
    }

    public void setFeedbackSubmitted(Boolean feedbackSubmitted) {
        this.feedbackSubmitted = feedbackSubmitted;
    }

    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }

    public void setApprovalTime(OffsetDateTime approvalTime) {
        this.approvalTime = approvalTime;
    }

    public void setQrToken(String qrToken) {
        this.qrToken = qrToken;
    }

    public void setReminderSent(Boolean reminderSent) {
        this.reminderSent = reminderSent;
    }

    public void setBookedAt(OffsetDateTime bookedAt) {
        this.bookedAt = bookedAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
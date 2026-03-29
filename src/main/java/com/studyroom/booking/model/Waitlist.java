package com.studyroom.booking.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "waitlist",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_waitlist_user_room_slot",
                        columnNames = {"user_id", "room_id", "start_at", "end_at"}
                )
        }
)
public class Waitlist {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password"})
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "room_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private StudyRoom room;

    @Column(name = "start_at", nullable = false)
    private OffsetDateTime startAt;

    @Column(name = "end_at", nullable = false)
    private OffsetDateTime endAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private WaitlistStatus status = WaitlistStatus.WAITING;

    @Column(name = "position_number")
    private Integer positionNumber;

    @Column(name = "auto_assign", nullable = false)
    private Boolean autoAssign = false;

    @Column(name = "notified_at")
    private OffsetDateTime notifiedAt;

    @Column(name = "expires_at")
    private OffsetDateTime expiresAt;

    @Column(name = "assigned_at")
    private OffsetDateTime assignedAt;

    @Column(name = "cancelled_at")
    private OffsetDateTime cancelledAt;

    @Column(name = "notes")
    private String notes;

    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private OffsetDateTime updatedAt;

    public Waitlist() {
    }

    public Waitlist(User user, StudyRoom room, OffsetDateTime startAt, OffsetDateTime endAt, Boolean autoAssign) {
        this.user = user;
        this.room = room;
        this.startAt = startAt;
        this.endAt = endAt;
        this.autoAssign = autoAssign != null && autoAssign;
        this.status = WaitlistStatus.WAITING;
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
            this.status = WaitlistStatus.WAITING;
        }

        if (this.autoAssign == null) {
            this.autoAssign = false;
        }

        if (this.notes != null) {
            this.notes = this.notes.trim();
            if (this.notes.isBlank()) {
                this.notes = null;
            }
        }
    }

    // ================= BUSINESS METHODS =================

    public boolean isValidTimeRange() {
        return startAt != null && endAt != null && endAt.isAfter(startAt);
    }

    public boolean isWaiting() {
        return WaitlistStatus.WAITING.equals(this.status);
    }

    public boolean isNotified() {
        return WaitlistStatus.NOTIFIED.equals(this.status);
    }

    public boolean isAssigned() {
        return WaitlistStatus.ASSIGNED.equals(this.status);
    }

    public boolean isExpired() {
        return WaitlistStatus.EXPIRED.equals(this.status);
    }

    public boolean isCancelled() {
        return WaitlistStatus.CANCELLED.equals(this.status);
    }

    public boolean isActive() {
        return isWaiting() || isNotified();
    }

    public boolean hasExpired() {
        return expiresAt != null && OffsetDateTime.now().isAfter(expiresAt);
    }

    public void markNotified(long responseWindowMinutes) {
        this.status = WaitlistStatus.NOTIFIED;
        this.notifiedAt = OffsetDateTime.now();
        this.expiresAt = this.notifiedAt.plusMinutes(responseWindowMinutes);
    }

    public void markAssigned() {
        this.status = WaitlistStatus.ASSIGNED;
        this.assignedAt = OffsetDateTime.now();
        this.expiresAt = null;
    }

    public void markExpired() {
        this.status = WaitlistStatus.EXPIRED;
        this.expiresAt = OffsetDateTime.now();
    }

    public void cancel(String reason) {
        this.status = WaitlistStatus.CANCELLED;
        this.cancelledAt = OffsetDateTime.now();
        this.notes = reason;
    }

    public void resetToWaiting() {
        this.status = WaitlistStatus.WAITING;
        this.notifiedAt = null;
        this.expiresAt = null;
    }

    // ================= GETTERS & SETTERS =================

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public StudyRoom getRoom() {
        return room;
    }

    public void setRoom(StudyRoom room) {
        this.room = room;
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

    public WaitlistStatus getStatus() {
        return status;
    }

    public void setStatus(WaitlistStatus status) {
        this.status = status;
    }

    public Integer getPositionNumber() {
        return positionNumber;
    }

    public void setPositionNumber(Integer positionNumber) {
        this.positionNumber = positionNumber;
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

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
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
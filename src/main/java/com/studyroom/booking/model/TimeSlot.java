package com.studyroom.booking.model;

import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "time_slots")
public class TimeSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    // 🔗 ROOM RELATION
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "room_id", nullable = false)
    private StudyRoom room;

    // ⏱ SLOT START
    @Column(name = "start_at", nullable = false)
    private OffsetDateTime startAt;

    // ⏱ SLOT END
    @Column(name = "end_at", nullable = false)
    private OffsetDateTime endAt;

    // 🟢 ACTIVE / INACTIVE
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    // 📌 OPTIONAL: capacity override (optional feature)
    @Column(name = "capacity")
    private Integer capacity;

    // 📌 OPTIONAL: price override (optional feature)
    @Column(name = "price")
    private Double price;

    // 🧾 AUDIT
    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private OffsetDateTime updatedAt;

    public TimeSlot() {}

    public TimeSlot(StudyRoom room, OffsetDateTime startAt, OffsetDateTime endAt) {
        this.room = room;
        this.startAt = startAt;
        this.endAt = endAt;
        this.isActive = true;
    }

    // ================== VALIDATION ==================

    @PrePersist
    @PreUpdate
    public void validate() {
        if (startAt == null || endAt == null) {
            throw new RuntimeException("Start and end time are required");
        }

        if (!endAt.isAfter(startAt)) {
            throw new RuntimeException("End time must be after start time");
        }

        if (isActive == null) {
            isActive = true;
        }
    }

    // ================== HELPERS ==================

    public boolean overlaps(OffsetDateTime otherStart, OffsetDateTime otherEnd) {
        if (otherStart == null || otherEnd == null) return false;
        return startAt.isBefore(otherEnd) && endAt.isAfter(otherStart);
    }

    // ================== GETTERS / SETTERS ==================

    public UUID getId() {
        return id;
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

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean active) {
        isActive = active;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
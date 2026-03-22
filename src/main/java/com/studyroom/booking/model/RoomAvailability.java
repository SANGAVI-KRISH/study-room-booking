package com.studyroom.booking.model;

import jakarta.persistence.*;

import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "room_availability")
public class RoomAvailability {

    @Id
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private StudyRoom room;

    @Column(name = "day_of_week", nullable = false)
    private Integer dayOfWeek; // 0 = Sunday, 1 = Monday ... 6 = Saturday

    @Column(name = "open_time", nullable = false)
    private LocalTime openTime;

    @Column(name = "close_time", nullable = false)
    private LocalTime closeTime;

    @Column(name = "slot_duration_mins", nullable = false)
    private Integer slotDurationMins = 60;

    @Column(name = "is_available", nullable = false)
    private Boolean isAvailable = true;

    public RoomAvailability() {
    }

    @PrePersist
    public void prePersist() {
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
        if (this.slotDurationMins == null || this.slotDurationMins <= 0) {
            this.slotDurationMins = 60;
        }
        if (this.isAvailable == null) {
            this.isAvailable = true;
        }
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

    public Integer getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(Integer dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public LocalTime getOpenTime() {
        return openTime;
    }

    public void setOpenTime(LocalTime openTime) {
        this.openTime = openTime;
    }

    public LocalTime getCloseTime() {
        return closeTime;
    }

    public void setCloseTime(LocalTime closeTime) {
        this.closeTime = closeTime;
    }

    public Integer getSlotDurationMins() {
        return slotDurationMins;
    }

    public void setSlotDurationMins(Integer slotDurationMins) {
        this.slotDurationMins = slotDurationMins;
    }

    public Boolean getIsAvailable() {
        return isAvailable;
    }

    public void setIsAvailable(Boolean available) {
        isAvailable = available;
    }
}
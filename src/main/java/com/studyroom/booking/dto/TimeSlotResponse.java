package com.studyroom.booking.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public class TimeSlotResponse {

    private UUID id;
    private UUID roomId;
    private String roomName;

    // 🔥 NEW FIELDS (Location details)
    private String block;
    private String district;
    private String location;

    private OffsetDateTime startAt;
    private OffsetDateTime endAt;
    private Boolean isActive;
    private Integer capacity;
    private Integer bookedCount;
    private Integer remainingSeats;
    private String availabilityStatus;
    private Double price;

    public TimeSlotResponse() {
    }

    public TimeSlotResponse(UUID id, UUID roomId, String roomName,
                            String block, String district, String location,
                            OffsetDateTime startAt, OffsetDateTime endAt,
                            Boolean isActive, Integer capacity,
                            Integer bookedCount, Integer remainingSeats,
                            String availabilityStatus, Double price) {
        this.id = id;
        this.roomId = roomId;
        this.roomName = roomName;
        this.block = block;
        this.district = district;
        this.location = location;
        this.startAt = startAt;
        this.endAt = endAt;
        this.isActive = isActive;
        this.capacity = capacity;
        this.bookedCount = bookedCount;
        this.remainingSeats = remainingSeats;
        this.availabilityStatus = availabilityStatus;
        this.price = price;
    }

    // ================= GETTERS =================

    public UUID getId() {
        return id;
    }

    public UUID getRoomId() {
        return roomId;
    }

    public String getRoomName() {
        return roomName;
    }

    public String getBlock() {
        return block;
    }

    public String getDistrict() {
        return district;
    }

    public String getLocation() {
        return location;
    }

    public OffsetDateTime getStartAt() {
        return startAt;
    }

    public OffsetDateTime getEndAt() {
        return endAt;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public Integer getBookedCount() {
        return bookedCount;
    }

    public Integer getRemainingSeats() {
        return remainingSeats;
    }

    public String getAvailabilityStatus() {
        return availabilityStatus;
    }

    public Double getPrice() {
        return price;
    }

    // ================= SETTERS =================

    public void setId(UUID id) {
        this.id = id;
    }

    public void setRoomId(UUID roomId) {
        this.roomId = roomId;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public void setBlock(String block) {
        this.block = block;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setStartAt(OffsetDateTime startAt) {
        this.startAt = startAt;
    }

    public void setEndAt(OffsetDateTime endAt) {
        this.endAt = endAt;
    }

    public void setIsActive(Boolean active) {
        isActive = active;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public void setBookedCount(Integer bookedCount) {
        this.bookedCount = bookedCount;
    }

    public void setRemainingSeats(Integer remainingSeats) {
        this.remainingSeats = remainingSeats;
    }

    public void setAvailabilityStatus(String availabilityStatus) {
        this.availabilityStatus = availabilityStatus;
    }

    public void setPrice(Double price) {
        this.price = price;
    }
}
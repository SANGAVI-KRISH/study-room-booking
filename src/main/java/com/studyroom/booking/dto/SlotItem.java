package com.studyroom.booking.dto;

import java.time.LocalTime;

public class SlotItem {

    private LocalTime startTime;
    private LocalTime endTime;
    private boolean available;

    // ✅ New: remaining capacity for this slot
    private Integer remainingSeats;

    // ✅ Optional: helps frontend show why unavailable
    private String status;
    private String message;

    public SlotItem() {
    }

    // ✅ Existing constructor kept for backward compatibility
    public SlotItem(LocalTime startTime, LocalTime endTime, boolean available) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.available = available;
        this.remainingSeats = 0;
        this.status = available ? "available" : "unavailable";
        this.message = null;
    }

    // ✅ New constructor
    public SlotItem(
            LocalTime startTime,
            LocalTime endTime,
            boolean available,
            Integer remainingSeats,
            String status,
            String message
    ) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.available = available;
        this.remainingSeats = remainingSeats;
        this.status = status;
        this.message = message;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public Integer getRemainingSeats() {
        return remainingSeats;
    }

    public void setRemainingSeats(Integer remainingSeats) {
        this.remainingSeats = remainingSeats;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
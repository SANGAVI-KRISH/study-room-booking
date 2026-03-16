package com.studyroom.booking.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public class BookingRequest {

    private Long roomId;
    private Long userId;
    private LocalDate bookingDate;
    private LocalTime startTime;
    private LocalTime endTime;

    public BookingRequest() {
    }

    public BookingRequest(Long roomId, Long userId, LocalDate bookingDate, LocalTime startTime, LocalTime endTime) {
        this.roomId = roomId;
        this.userId = userId;
        this.bookingDate = bookingDate;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public LocalDate getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(LocalDate bookingDate) {
        this.bookingDate = bookingDate;
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
}
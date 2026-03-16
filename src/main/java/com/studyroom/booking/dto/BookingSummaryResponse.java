package com.studyroom.booking.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public class BookingSummaryResponse {

    private Long roomId;
    private String roomName;
    private Long userId;
    private LocalDate bookingDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private long durationInMinutes;
    private String status;
    private String message;

    public BookingSummaryResponse() {
    }

    public BookingSummaryResponse(Long roomId, String roomName, Long userId, LocalDate bookingDate,
                                  LocalTime startTime, LocalTime endTime, long durationInMinutes,
                                  String status, String message) {
        this.roomId = roomId;
        this.roomName = roomName;
        this.userId = userId;
        this.bookingDate = bookingDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.durationInMinutes = durationInMinutes;
        this.status = status;
        this.message = message;
    }

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
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

    public long getDurationInMinutes() {
        return durationInMinutes;
    }

    public void setDurationInMinutes(long durationInMinutes) {
        this.durationInMinutes = durationInMinutes;
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
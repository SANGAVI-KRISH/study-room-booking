package com.studyroom.booking.dto;

public class RoomUsageTrendDto {

    private String roomName;
    private long bookingCount;

    public RoomUsageTrendDto() {
    }

    public RoomUsageTrendDto(String roomName, long bookingCount) {
        this.roomName = roomName;
        this.bookingCount = bookingCount;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public long getBookingCount() {
        return bookingCount;
    }

    public void setBookingCount(long bookingCount) {
        this.bookingCount = bookingCount;
    }
}
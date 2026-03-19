package com.studyroom.booking.dto;

public class RoomUtilizationDto {
    private String roomName;
    private Long totalBookings;
    private Long totalHoursBooked;

    public RoomUtilizationDto() {}

    public RoomUtilizationDto(String roomName, Long totalBookings, Long totalHoursBooked) {
        this.roomName = roomName;
        this.totalBookings = totalBookings;
        this.totalHoursBooked = totalHoursBooked;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public Long getTotalBookings() {
        return totalBookings;
    }

    public void setTotalBookings(Long totalBookings) {
        this.totalBookings = totalBookings;
    }

    public Long getTotalHoursBooked() {
        return totalHoursBooked;
    }

    public void setTotalHoursBooked(Long totalHoursBooked) {
        this.totalHoursBooked = totalHoursBooked;
    }
}
package com.studyroom.booking.dto;

import java.util.List;

public class AdminDashboardDto {

    private long totalRooms;
    private long totalUsers;
    private long totalBookings;
    private long activeBookings;
    private long cancelledBookings;
    private String mostBookedRoom;
    private String peakBookingHour;
    private List<RoomUsageTrendDto> roomUsageTrends;

    public AdminDashboardDto() {
    }

    public AdminDashboardDto(long totalRooms, long totalUsers, long totalBookings,
                             long activeBookings, long cancelledBookings,
                             String mostBookedRoom, String peakBookingHour,
                             List<RoomUsageTrendDto> roomUsageTrends) {
        this.totalRooms = totalRooms;
        this.totalUsers = totalUsers;
        this.totalBookings = totalBookings;
        this.activeBookings = activeBookings;
        this.cancelledBookings = cancelledBookings;
        this.mostBookedRoom = mostBookedRoom;
        this.peakBookingHour = peakBookingHour;
        this.roomUsageTrends = roomUsageTrends;
    }

    public long getTotalRooms() {
        return totalRooms;
    }

    public void setTotalRooms(long totalRooms) {
        this.totalRooms = totalRooms;
    }

    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public long getTotalBookings() {
        return totalBookings;
    }

    public void setTotalBookings(long totalBookings) {
        this.totalBookings = totalBookings;
    }

    public long getActiveBookings() {
        return activeBookings;
    }

    public void setActiveBookings(long activeBookings) {
        this.activeBookings = activeBookings;
    }

    public long getCancelledBookings() {
        return cancelledBookings;
    }

    public void setCancelledBookings(long cancelledBookings) {
        this.cancelledBookings = cancelledBookings;
    }

    public String getMostBookedRoom() {
        return mostBookedRoom;
    }

    public void setMostBookedRoom(String mostBookedRoom) {
        this.mostBookedRoom = mostBookedRoom;
    }

    public String getPeakBookingHour() {
        return peakBookingHour;
    }

    public void setPeakBookingHour(String peakBookingHour) {
        this.peakBookingHour = peakBookingHour;
    }

    public List<RoomUsageTrendDto> getRoomUsageTrends() {
        return roomUsageTrends;
    }

    public void setRoomUsageTrends(List<RoomUsageTrendDto> roomUsageTrends) {
        this.roomUsageTrends = roomUsageTrends;
    }
}
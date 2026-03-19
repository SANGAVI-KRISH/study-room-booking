package com.studyroom.booking.dto;

public class UserActivityDto {
    private String userName;
    private String email;
    private Long totalBookings;

    public UserActivityDto() {}

    public UserActivityDto(String userName, String email, Long totalBookings) {
        this.userName = userName;
        this.email = email;
        this.totalBookings = totalBookings;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Long getTotalBookings() {
        return totalBookings;
    }

    public void setTotalBookings(Long totalBookings) {
        this.totalBookings = totalBookings;
    }
}
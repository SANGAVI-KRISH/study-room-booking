package com.studyroom.booking.dto;

public class BookingCountReportDto {
    private String label;
    private Long totalBookings;

    public BookingCountReportDto() {}

    public BookingCountReportDto(String label, Long totalBookings) {
        this.label = label;
        this.totalBookings = totalBookings;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Long getTotalBookings() {
        return totalBookings;
    }

    public void setTotalBookings(Long totalBookings) {
        this.totalBookings = totalBookings;
    }
}
package com.studyroom.booking.dto;

import java.time.LocalTime;

public class RoomAvailabilityRequest {

    private Integer dayOfWeek;
    private LocalTime openTime;
    private LocalTime closeTime;
    private Integer slotDurationMins;
    private Boolean isAvailable;

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
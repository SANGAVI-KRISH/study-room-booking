package com.studyroom.booking.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class AvailableSlotsResponse {

    private UUID roomId;
    private LocalDate date;
    private Integer dayOfWeek;
    private Integer slotDurationMins;
    private List<SlotItem> slots;

    public AvailableSlotsResponse() {
    }

    public AvailableSlotsResponse(UUID roomId, LocalDate date, Integer dayOfWeek,
                                  Integer slotDurationMins, List<SlotItem> slots) {
        this.roomId = roomId;
        this.date = date;
        this.dayOfWeek = dayOfWeek;
        this.slotDurationMins = slotDurationMins;
        this.slots = slots;
    }

    public UUID getRoomId() {
        return roomId;
    }

    public void setRoomId(UUID roomId) {
        this.roomId = roomId;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Integer getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(Integer dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public Integer getSlotDurationMins() {
        return slotDurationMins;
    }

    public void setSlotDurationMins(Integer slotDurationMins) {
        this.slotDurationMins = slotDurationMins;
    }

    public List<SlotItem> getSlots() {
        return slots;
    }

    public void setSlots(List<SlotItem> slots) {
        this.slots = slots;
    }
}
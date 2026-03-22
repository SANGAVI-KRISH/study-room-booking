package com.studyroom.booking.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class MaintenanceResponse {

    private UUID maintenanceId;
    private UUID roomId;

    private String roomName;
    private String roomNumber;
    private String blockName;
    private String district;
    private String exactLocation;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fromDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate toDate;

    private String reason;
    private Boolean isActive;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    public MaintenanceResponse() {
    }

    public MaintenanceResponse(
            UUID maintenanceId,
            UUID roomId,
            String roomName,
            String roomNumber,
            String blockName,
            String district,
            String exactLocation,
            LocalDate fromDate,
            LocalDate toDate,
            String reason,
            Boolean isActive,
            LocalDateTime createdAt
    ) {
        this.maintenanceId = maintenanceId;
        this.roomId = roomId;
        this.roomName = roomName;
        this.roomNumber = roomNumber;
        this.blockName = blockName;
        this.district = district;
        this.exactLocation = exactLocation;
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.reason = reason;
        this.isActive = isActive;
        this.createdAt = createdAt;
    }

    public UUID getMaintenanceId() {
        return maintenanceId;
    }

    public void setMaintenanceId(UUID maintenanceId) {
        this.maintenanceId = maintenanceId;
    }

    public UUID getRoomId() {
        return roomId;
    }

    public void setRoomId(UUID roomId) {
        this.roomId = roomId;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public String getBlockName() {
        return blockName;
    }

    public void setBlockName(String blockName) {
        this.blockName = blockName;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getExactLocation() {
        return exactLocation;
    }

    public void setExactLocation(String exactLocation) {
        this.exactLocation = exactLocation;
    }

    public LocalDate getFromDate() {
        return fromDate;
    }

    public void setFromDate(LocalDate fromDate) {
        this.fromDate = fromDate;
    }

    public LocalDate getToDate() {
        return toDate;
    }

    public void setToDate(LocalDate toDate) {
        this.toDate = toDate;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean active) {
        isActive = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
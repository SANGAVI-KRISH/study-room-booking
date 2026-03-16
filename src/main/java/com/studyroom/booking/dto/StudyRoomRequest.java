package com.studyroom.booking.dto;

import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

public class StudyRoomRequest {

    private String blockName;
    private String roomNumber;
    private String floorNumber;
    private Integer seatingCapacity;
    private String availabilityTimings;
    private String facilities;
    private String district;
    private String location;
    private BigDecimal feePerHour;
    private Boolean approvalRequired;
    private MultipartFile[] images;

    public StudyRoomRequest() {
    }

    public String getBlockName() {
        return blockName;
    }

    public void setBlockName(String blockName) {
        this.blockName = blockName;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public String getFloorNumber() {
        return floorNumber;
    }

    public void setFloorNumber(String floorNumber) {
        this.floorNumber = floorNumber;
    }

    public Integer getSeatingCapacity() {
        return seatingCapacity;
    }

    public void setSeatingCapacity(Integer seatingCapacity) {
        this.seatingCapacity = seatingCapacity;
    }

    public String getAvailabilityTimings() {
        return availabilityTimings;
    }

    public void setAvailabilityTimings(String availabilityTimings) {
        this.availabilityTimings = availabilityTimings;
    }

    public String getFacilities() {
        return facilities;
    }

    public void setFacilities(String facilities) {
        this.facilities = facilities;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public BigDecimal getFeePerHour() {
        return feePerHour;
    }

    public void setFeePerHour(BigDecimal feePerHour) {
        this.feePerHour = feePerHour;
    }

    public Boolean getApprovalRequired() {
        return approvalRequired;
    }

    public void setApprovalRequired(Boolean approvalRequired) {
        this.approvalRequired = approvalRequired;
    }

    public MultipartFile[] getImages() {
        return images;
    }

    public void setImages(MultipartFile[] images) {
        this.images = images;
    }
}
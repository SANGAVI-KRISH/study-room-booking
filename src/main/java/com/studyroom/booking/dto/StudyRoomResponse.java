package com.studyroom.booking.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class StudyRoomResponse {

    private UUID id;
    private String displayName;
    private String blockName;

    // 🔥 NEW (for consistency with TimeSlotResponse)
    private String block;

    private String roomNumber;
    private String floorNumber;
    private Integer seatingCapacity;
    private String facilities;
    private String district;
    private String location;
    private BigDecimal feePerHour;
    private Boolean approvalRequired = false;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private List<ImageResponse> images = new ArrayList<>();

    public StudyRoomResponse() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getBlockName() {
        return blockName;
    }

    public void setBlockName(String blockName) {
        this.blockName = blockName;

        // 🔥 auto sync
        this.block = blockName;
    }

    public String getBlock() {
        return block;
    }

    public void setBlock(String block) {
        this.block = block;
        this.blockName = block; // keep both synced
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

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<ImageResponse> getImages() {
        return images;
    }

    public void setImages(List<ImageResponse> images) {
        this.images = (images != null) ? images : new ArrayList<>();
    }

    public static class ImageResponse {

        private UUID id;
        private String imageUrl;
        private Integer displayOrder;

        public ImageResponse() {
        }

        public ImageResponse(UUID id, String imageUrl, Integer displayOrder) {
            this.id = id;
            this.imageUrl = imageUrl;
            this.displayOrder = displayOrder;
        }

        public UUID getId() {
            return id;
        }

        public void setId(UUID id) {
            this.id = id;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }

        public Integer getDisplayOrder() {
            return displayOrder;
        }

        public void setDisplayOrder(Integer displayOrder) {
            this.displayOrder = displayOrder;
        }
    }
}
package com.studyroom.booking.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "rooms")
public class StudyRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "display_name", nullable = false)
    private String displayName;

    @Column(name = "block_name", nullable = false)
    private String blockName;

    @Column(name = "room_number", nullable = false)
    private String roomNumber;

    @Column(name = "floor_number")
    private String floorNumber;

    @Column(name = "seating_capacity")
    private Integer seatingCapacity;

    @Column(name = "facilities", columnDefinition = "TEXT")
    private String facilities;

    @Column(name = "district")
    private String district;

    @Column(name = "exact_location")
    private String location;

    @Column(name = "fee_per_hour", precision = 10, scale = 2)
    private BigDecimal feePerHour;

    @Column(name = "approval_required", nullable = false)
    private Boolean approvalRequired = false;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<StudyRoomImage> images = new ArrayList<>();

    public StudyRoom() {
    }

    public StudyRoom(
            String blockName,
            String roomNumber,
            String floorNumber,
            Integer seatingCapacity,
            String facilities,
            String district,
            String location,
            BigDecimal feePerHour,
            Boolean approvalRequired
    ) {
        this.blockName = normalize(blockName);
        this.roomNumber = normalize(roomNumber);
        this.floorNumber = normalize(floorNumber);
        this.seatingCapacity = seatingCapacity;
        this.facilities = normalize(facilities);
        this.district = normalize(district);
        this.location = normalize(location);
        this.feePerHour = feePerHour;
        this.approvalRequired = approvalRequired != null ? approvalRequired : false;
        this.isActive = true;
        this.isDeleted = false;
        this.displayName = buildDisplayName();
    }

    @PrePersist
    protected void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;

        if (this.approvalRequired == null) {
            this.approvalRequired = false;
        }

        if (this.isActive == null) {
            this.isActive = true;
        }

        if (this.isDeleted == null) {
            this.isDeleted = false;
        }

        trimFields();
        this.displayName = buildDisplayName();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = OffsetDateTime.now();

        trimFields();

        if (this.approvalRequired == null) {
            this.approvalRequired = false;
        }

        if (this.isActive == null) {
            this.isActive = true;
        }

        if (this.isDeleted == null) {
            this.isDeleted = false;
        }

        this.displayName = buildDisplayName();
    }

    private void trimFields() {
        this.displayName = normalize(this.displayName);
        this.blockName = normalize(this.blockName);
        this.roomNumber = normalize(this.roomNumber);
        this.floorNumber = normalize(this.floorNumber);
        this.facilities = normalize(this.facilities);
        this.district = normalize(this.district);
        this.location = normalize(this.location);
    }

    private String normalize(String value) {
        return value == null ? null : value.trim();
    }

    private String buildDisplayName() {
        StringBuilder sb = new StringBuilder();

        if (district != null && !district.isBlank()) {
            sb.append(district);
        }

        if (location != null && !location.isBlank()) {
            if (sb.length() > 0) sb.append(" - ");
            sb.append(location);
        }

        if (blockName != null && !blockName.isBlank()) {
            if (sb.length() > 0) sb.append(" - ");
            sb.append(blockName);
        }

        if (roomNumber != null && !roomNumber.isBlank()) {
            if (sb.length() > 0) sb.append(" - ");
            sb.append("Room ").append(roomNumber);
        }

        return sb.length() > 0 ? sb.toString() : "Unnamed Room";
    }

    public void addImage(StudyRoomImage image) {
        if (image != null) {
            images.add(image);
            image.setRoom(this);
        }
    }

    public void removeImage(StudyRoomImage image) {
        if (image != null) {
            images.remove(image);
            image.setRoom(null);
        }
    }

    public void clearImages() {
        if (images != null) {
            for (StudyRoomImage image : images) {
                image.setRoom(null);
            }
            images.clear();
        }
    }

    public void activateRoom() {
        this.isActive = true;
        this.isDeleted = false;
    }

    public void deactivateRoom() {
        this.isActive = false;
    }

    public void softDelete() {
        this.isActive = false;
        this.isDeleted = true;
    }

    public boolean isAvailableForBooking() {
        return Boolean.TRUE.equals(this.isActive) && !Boolean.TRUE.equals(this.isDeleted);
    }

    public Integer getCapacity() {
        return seatingCapacity != null ? seatingCapacity : 0;
    }

    public BigDecimal getPricePerPerson() {
        return feePerHour != null ? feePerHour : BigDecimal.ZERO;
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
        this.displayName = normalize(displayName);
    }

    public String getBlockName() {
        return blockName;
    }

    public void setBlockName(String blockName) {
        this.blockName = normalize(blockName);
    }

    public String getBlock() {
        return blockName;
    }

    public void setBlock(String block) {
        this.blockName = normalize(block);
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = normalize(roomNumber);
    }

    public String getFloorNumber() {
        return floorNumber;
    }

    public void setFloorNumber(String floorNumber) {
        this.floorNumber = normalize(floorNumber);
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
        this.facilities = normalize(facilities);
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = normalize(district);
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = normalize(location);
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

    public Boolean getIsActive() {
        return isActive;
    }

    public Boolean isActive() {
        return Boolean.TRUE.equals(isActive);
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Boolean getIsDeleted() {
        return isDeleted;
    }

    public Boolean isDeleted() {
        return Boolean.TRUE.equals(isDeleted);
    }

    public void setIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
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

    public List<StudyRoomImage> getImages() {
        return images;
    }

    public void setImages(List<StudyRoomImage> images) {
        this.clearImages();
        if (images != null) {
            for (StudyRoomImage image : images) {
                this.addImage(image);
            }
        }
    }
}
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
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "block_name", nullable = false)
    private String blockName;

    @Column(name = "room_number", nullable = false)
    private String roomNumber;

    @Column(name = "floor_number")
    private String floorNumber;

    @Column(name = "seating_capacity")
    private Integer seatingCapacity;

    @Column(name = "availability_timings")
    private String availabilityTimings;

    @Column(name = "facilities")
    private String facilities;

    @Column(name = "district")
    private String district;

    @Column(name = "location")
    private String location;

    @Column(name = "fee_per_hour", precision = 10, scale = 2)
    private BigDecimal feePerHour;

    @Column(name = "approval_required", nullable = false)
    private boolean approvalRequired = false;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("displayOrder ASC, createdAt ASC")
    @JsonManagedReference
    private List<StudyRoomImage> images = new ArrayList<>();

    public StudyRoom() {
    }

    public StudyRoom(String blockName,
                     String roomNumber,
                     String floorNumber,
                     Integer seatingCapacity,
                     String availabilityTimings,
                     String facilities,
                     String district,
                     String location,
                     BigDecimal feePerHour,
                     boolean approvalRequired) {
        this.blockName = blockName;
        this.roomNumber = roomNumber;
        this.floorNumber = floorNumber;
        this.seatingCapacity = seatingCapacity;
        this.availabilityTimings = availabilityTimings;
        this.facilities = facilities;
        this.district = district;
        this.location = location;
        this.feePerHour = feePerHour;
        this.approvalRequired = approvalRequired;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = OffsetDateTime.now();
        this.updatedAt = OffsetDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = OffsetDateTime.now();
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

    public UUID getId() {
        return id;
    }

    public String getBlockName() {
        return blockName;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public String getFloorNumber() {
        return floorNumber;
    }

    public Integer getSeatingCapacity() {
        return seatingCapacity;
    }

    public String getAvailabilityTimings() {
        return availabilityTimings;
    }

    public String getFacilities() {
        return facilities;
    }

    public String getDistrict() {
        return district;
    }

    public String getLocation() {
        return location;
    }

    public BigDecimal getFeePerHour() {
        return feePerHour;
    }

    public boolean isApprovalRequired() {
        return approvalRequired;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public List<StudyRoomImage> getImages() {
        return images;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setBlockName(String blockName) {
        this.blockName = blockName;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public void setFloorNumber(String floorNumber) {
        this.floorNumber = floorNumber;
    }

    public void setSeatingCapacity(Integer seatingCapacity) {
        this.seatingCapacity = seatingCapacity;
    }

    public void setAvailabilityTimings(String availabilityTimings) {
        this.availabilityTimings = availabilityTimings;
    }

    public void setFacilities(String facilities) {
        this.facilities = facilities;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setFeePerHour(BigDecimal feePerHour) {
        this.feePerHour = feePerHour;
    }

    public void setApprovalRequired(boolean approvalRequired) {
        this.approvalRequired = approvalRequired;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setImages(List<StudyRoomImage> images) {
        this.images.clear();
        if (images != null) {
            for (StudyRoomImage image : images) {
                addImage(image);
            }
        }
    }
}
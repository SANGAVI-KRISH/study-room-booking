package com.studyroom.booking.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public class FeedbackRequest {

    @NotNull(message = "Booking ID is required")
    private UUID bookingId;

    @NotNull(message = "Overall rating is required")
    @Min(value = 1, message = "Overall rating must be at least 1")
    @Max(value = 5, message = "Overall rating must be at most 5")
    private Integer rating;

    // Optional but if present must be valid
    @Min(value = 1, message = "Cleanliness rating must be at least 1")
    @Max(value = 5, message = "Cleanliness rating must be at most 5")
    private Integer cleanlinessRating;

    @Min(value = 1, message = "Usefulness rating must be at least 1")
    @Max(value = 5, message = "Usefulness rating must be at most 5")
    private Integer usefulnessRating;

    @Size(max = 1000, message = "Comments must not exceed 1000 characters")
    private String comments;

    @Size(max = 1000, message = "Maintenance issue must not exceed 1000 characters")
    private String maintenanceIssue;

    // 🔥 NEW: Optional but useful for backend validation/logging
    private UUID userId;

    // 🔥 NEW: Auto-calculated average (optional from frontend)
    private Double averageRating;

    /* ---------------- GETTERS & SETTERS ---------------- */

    public UUID getBookingId() {
        return bookingId;
    }

    public void setBookingId(UUID bookingId) {
        this.bookingId = bookingId;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public Integer getCleanlinessRating() {
        return cleanlinessRating;
    }

    public void setCleanlinessRating(Integer cleanlinessRating) {
        this.cleanlinessRating = cleanlinessRating;
    }

    public Integer getUsefulnessRating() {
        return usefulnessRating;
    }

    public void setUsefulnessRating(Integer usefulnessRating) {
        this.usefulnessRating = usefulnessRating;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getMaintenanceIssue() {
        return maintenanceIssue;
    }

    public void setMaintenanceIssue(String maintenanceIssue) {
        this.maintenanceIssue = maintenanceIssue;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public Double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }
}
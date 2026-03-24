package com.studyroom.booking.dto;

import java.util.List;

public class FeedbackSummaryResponse {

    private Double averageRating;
    private Double averageCleanlinessRating;
    private Double averageUsefulnessRating;
    private Integer totalReviews;
    private List<FeedbackResponse> reviews;

    public Double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }

    public Double getAverageCleanlinessRating() {
        return averageCleanlinessRating;
    }

    public void setAverageCleanlinessRating(Double averageCleanlinessRating) {
        this.averageCleanlinessRating = averageCleanlinessRating;
    }

    public Double getAverageUsefulnessRating() {
        return averageUsefulnessRating;
    }

    public void setAverageUsefulnessRating(Double averageUsefulnessRating) {
        this.averageUsefulnessRating = averageUsefulnessRating;
    }

    public Integer getTotalReviews() {
        return totalReviews;
    }

    public void setTotalReviews(Integer totalReviews) {
        this.totalReviews = totalReviews;
    }

    public List<FeedbackResponse> getReviews() {
        return reviews;
    }

    public void setReviews(List<FeedbackResponse> reviews) {
        this.reviews = reviews;
    }
}
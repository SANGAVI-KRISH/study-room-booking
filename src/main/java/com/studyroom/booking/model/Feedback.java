package com.studyroom.booking.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "feedback",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_feedback_booking", columnNames = {"booking_id"})
        }
)
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "booking_id", nullable = false, unique = true)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Booking booking;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "room_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private StudyRoom room;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password"})
    private User user;

    @Column(name = "rating", nullable = false)
    private Integer rating;

    @Column(name = "cleanliness_rating")
    private Integer cleanlinessRating;

    @Column(name = "usefulness_rating")
    private Integer usefulnessRating;

    @Column(name = "comments", columnDefinition = "TEXT")
    private String comments;

    @Column(name = "maintenance_issue", columnDefinition = "TEXT")
    private String maintenanceIssue;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    public Feedback() {
    }

    public Feedback(Booking booking,
                    StudyRoom room,
                    User user,
                    Integer rating,
                    Integer cleanlinessRating,
                    Integer usefulnessRating,
                    String comments,
                    String maintenanceIssue) {
        this.booking = booking;
        this.room = room;
        this.user = user;
        this.rating = rating;
        this.cleanlinessRating = cleanlinessRating;
        this.usefulnessRating = usefulnessRating;
        this.comments = comments;
        this.maintenanceIssue = maintenanceIssue;
    }

    @PrePersist
    public void prePersist() {
        applyDefaults();
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
    }

    @PreUpdate
    public void preUpdate() {
        applyDefaults();
    }

    private void applyDefaults() {
        if (comments != null) {
            comments = comments.trim();
            if (comments.isBlank()) {
                comments = null;
            }
        }

        if (maintenanceIssue != null) {
            maintenanceIssue = maintenanceIssue.trim();
            if (maintenanceIssue.isBlank()) {
                maintenanceIssue = null;
            }
        }
    }

    // ================= BUSINESS METHODS =================

    public boolean hasMaintenanceIssue() {
        return maintenanceIssue != null && !maintenanceIssue.isBlank();
    }

    public boolean hasComments() {
        return comments != null && !comments.isBlank();
    }

    public boolean hasDetailedRatings() {
        return cleanlinessRating != null || usefulnessRating != null;
    }

    // ================= GETTERS & SETTERS =================

    public UUID getId() {
        return id;
    }

    public Booking getBooking() {
        return booking;
    }

    public StudyRoom getRoom() {
        return room;
    }

    public User getUser() {
        return user;
    }

    public Integer getRating() {
        return rating;
    }

    public Integer getCleanlinessRating() {
        return cleanlinessRating;
    }

    public Integer getUsefulnessRating() {
        return usefulnessRating;
    }

    public String getComments() {
        return comments;
    }

    public String getMaintenanceIssue() {
        return maintenanceIssue;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setBooking(Booking booking) {
        this.booking = booking;
    }

    public void setRoom(StudyRoom room) {
        this.room = room;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public void setCleanlinessRating(Integer cleanlinessRating) {
        this.cleanlinessRating = cleanlinessRating;
    }

    public void setUsefulnessRating(Integer usefulnessRating) {
        this.usefulnessRating = usefulnessRating;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public void setMaintenanceIssue(String maintenanceIssue) {
        this.maintenanceIssue = maintenanceIssue;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
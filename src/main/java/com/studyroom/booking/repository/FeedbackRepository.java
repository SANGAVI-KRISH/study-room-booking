package com.studyroom.booking.repository;

import com.studyroom.booking.model.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, UUID> {

    // Get feedback by booking
    Optional<Feedback> findByBooking_Id(UUID bookingId);

    // Get feedback by booking + user (extra safety)
    Optional<Feedback> findByBooking_IdAndUser_Id(UUID bookingId, UUID userId);

    // Get all feedback for a room (latest first)
    List<Feedback> findByRoom_IdOrderByCreatedAtDesc(UUID roomId);

    // Get all feedback by a user
    List<Feedback> findByUser_IdOrderByCreatedAtDesc(UUID userId);

    // Admin: get all feedback
    List<Feedback> findAllByOrderByCreatedAtDesc();

    // Check if feedback exists for booking
    boolean existsByBooking_Id(UUID bookingId);

    // Check if feedback exists for booking + user
    boolean existsByBooking_IdAndUser_Id(UUID bookingId, UUID userId);
}
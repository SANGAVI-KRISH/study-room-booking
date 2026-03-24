package com.studyroom.booking.service;

import com.studyroom.booking.dto.FeedbackRequest;
import com.studyroom.booking.dto.FeedbackResponse;
import com.studyroom.booking.dto.FeedbackSummaryResponse;
import com.studyroom.booking.model.Booking;
import com.studyroom.booking.model.BookingStatus;
import com.studyroom.booking.model.Feedback;
import com.studyroom.booking.model.StudyRoom;
import com.studyroom.booking.model.User;
import com.studyroom.booking.repository.BookingRepository;
import com.studyroom.booking.repository.FeedbackRepository;
import com.studyroom.booking.repository.StudyRoomRepository;
import com.studyroom.booking.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final BookingRepository bookingRepository;
    private final StudyRoomRepository studyRoomRepository;
    private final UserRepository userRepository;

    private static final ZoneId APP_ZONE = ZoneId.of("Asia/Kolkata");

    public FeedbackService(
            FeedbackRepository feedbackRepository,
            BookingRepository bookingRepository,
            StudyRoomRepository studyRoomRepository,
            UserRepository userRepository
    ) {
        this.feedbackRepository = feedbackRepository;
        this.bookingRepository = bookingRepository;
        this.studyRoomRepository = studyRoomRepository;
        this.userRepository = userRepository;
    }

    public FeedbackResponse submitFeedback(FeedbackRequest request, UUID userId) {
        if (request == null) {
            throw new RuntimeException("Feedback request is required");
        }

        if (request.getBookingId() == null) {
            throw new RuntimeException("Booking ID is required");
        }

        if (userId == null) {
            throw new RuntimeException("User ID is required");
        }

        validateRating(request.getRating(), "Overall rating");
        validateOptionalRating(request.getCleanlinessRating(), "Cleanliness rating");
        validateOptionalRating(request.getUsefulnessRating(), "Usefulness rating");

        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (booking.getUser() == null || booking.getUser().getId() == null) {
            throw new RuntimeException("Booking user information is missing");
        }

        if (!booking.getUser().getId().equals(userId)) {
            throw new RuntimeException("You can submit feedback only for your own booking");
        }

        if (!BookingStatus.COMPLETED.equals(booking.getStatus())) {
            throw new RuntimeException("Feedback can be submitted only for completed bookings");
        }

        if (!Boolean.TRUE.equals(booking.getIsPresent())) {
            throw new RuntimeException("Feedback can be submitted only for attended bookings");
        }

        if (feedbackRepository.existsByBooking_Id(booking.getId())
                || Boolean.TRUE.equals(booking.getFeedbackSubmitted())) {
            throw new RuntimeException("Feedback already submitted for this booking");
        }

        OffsetDateTime now = OffsetDateTime.now(APP_ZONE);
        if (booking.getEndAt() != null && booking.getEndAt().isAfter(now)) {
            throw new RuntimeException("Feedback can be submitted only after booking is completed");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        StudyRoom room = booking.getRoom();
        if (room == null || room.getId() == null) {
            throw new RuntimeException("Room not found for booking");
        }

        Feedback feedback = new Feedback();
        feedback.setBooking(booking);
        feedback.setRoom(room);
        feedback.setUser(user);
        feedback.setRating(request.getRating());
        feedback.setCleanlinessRating(request.getCleanlinessRating());
        feedback.setUsefulnessRating(request.getUsefulnessRating());
        feedback.setComments(normalizeText(request.getComments()));
        feedback.setMaintenanceIssue(normalizeText(request.getMaintenanceIssue()));

        Feedback saved = feedbackRepository.save(feedback);

        booking.setFeedbackSubmitted(true);
        bookingRepository.save(booking);

        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public FeedbackSummaryResponse getRoomFeedback(UUID roomId) {
        if (roomId == null) {
            throw new RuntimeException("Room ID is required");
        }

        studyRoomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        List<Feedback> feedbackList = feedbackRepository.findByRoom_IdOrderByCreatedAtDesc(roomId);

        double averageRating = feedbackList.stream()
                .map(Feedback::getRating)
                .filter(rating -> rating != null)
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0.0);

        double averageCleanlinessRating = feedbackList.stream()
                .map(Feedback::getCleanlinessRating)
                .filter(rating -> rating != null)
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0.0);

        double averageUsefulnessRating = feedbackList.stream()
                .map(Feedback::getUsefulnessRating)
                .filter(rating -> rating != null)
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0.0);

        FeedbackSummaryResponse response = new FeedbackSummaryResponse();
        response.setAverageRating(roundToOneDecimal(averageRating));
        response.setAverageCleanlinessRating(roundToOneDecimal(averageCleanlinessRating));
        response.setAverageUsefulnessRating(roundToOneDecimal(averageUsefulnessRating));
        response.setTotalReviews(feedbackList.size());
        response.setReviews(feedbackList.stream().map(this::mapToResponse).toList());

        return response;
    }

    @Transactional(readOnly = true)
    public List<FeedbackResponse> getUserFeedback(UUID userId) {
        if (userId == null) {
            throw new RuntimeException("User ID is required");
        }

        userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return feedbackRepository.findByUser_IdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<FeedbackResponse> getAllFeedback() {
        return feedbackRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public FeedbackResponse getFeedbackByBooking(UUID bookingId) {
        if (bookingId == null) {
            throw new RuntimeException("Booking ID is required");
        }

        bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        Feedback feedback = feedbackRepository.findByBooking_Id(bookingId)
                .orElseThrow(() -> new RuntimeException("Feedback not found for this booking"));

        return mapToResponse(feedback);
    }

    @Transactional(readOnly = true)
    public boolean hasBookingFeedback(UUID bookingId) {
        if (bookingId == null) {
            return false;
        }

        return feedbackRepository.existsByBooking_Id(bookingId);
    }

    public void deleteFeedback(UUID feedbackId) {
        if (feedbackId == null) {
            throw new RuntimeException("Feedback ID is required");
        }

        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new RuntimeException("Feedback not found"));

        Booking booking = feedback.getBooking();

        feedbackRepository.delete(feedback);

        if (booking != null) {
            booking.setFeedbackSubmitted(false);
            bookingRepository.save(booking);
        }
    }

    private void validateRating(Integer rating, String fieldName) {
        if (rating == null) {
            throw new RuntimeException(fieldName + " is required");
        }

        if (rating < 1 || rating > 5) {
            throw new RuntimeException(fieldName + " must be between 1 and 5");
        }
    }

    private void validateOptionalRating(Integer rating, String fieldName) {
        if (rating != null && (rating < 1 || rating > 5)) {
            throw new RuntimeException(fieldName + " must be between 1 and 5");
        }
    }

    private String normalizeText(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isBlank() ? null : trimmed;
    }

    private double roundToOneDecimal(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    private double calculateAverageRating(Feedback feedback) {
        int count = 0;
        int total = 0;

        if (feedback.getRating() != null) {
            total += feedback.getRating();
            count++;
        }

        if (feedback.getCleanlinessRating() != null) {
            total += feedback.getCleanlinessRating();
            count++;
        }

        if (feedback.getUsefulnessRating() != null) {
            total += feedback.getUsefulnessRating();
            count++;
        }

        if (count == 0) {
            return 0.0;
        }

        return roundToOneDecimal((double) total / count);
    }

    private String buildRoomName(StudyRoom room) {
        if (room == null) {
            return null;
        }

        String displayName = room.getDisplayName() != null
                ? room.getDisplayName().trim()
                : "";

        String roomNumber = room.getRoomNumber() != null
                ? room.getRoomNumber().trim()
                : "";

        if (!displayName.isBlank() && !roomNumber.isBlank()) {
            return displayName + " - " + roomNumber;
        }

        if (!displayName.isBlank()) {
            return displayName;
        }

        if (!roomNumber.isBlank()) {
            return roomNumber;
        }

        return "Room";
    }

    private String buildUserName(User user) {
        if (user == null) {
            return "User";
        }

        if (user.getName() != null && !user.getName().isBlank()) {
            return user.getName().trim();
        }

        return "User";
    }

    private FeedbackResponse mapToResponse(Feedback feedback) {
        FeedbackResponse response = new FeedbackResponse();

        response.setId(feedback.getId());

        if (feedback.getBooking() != null) {
            response.setBookingId(feedback.getBooking().getId());
            response.setBookingDate(feedback.getBooking().getStartAt());
        }

        if (feedback.getRoom() != null) {
            response.setRoomId(feedback.getRoom().getId());
            response.setRoomName(buildRoomName(feedback.getRoom()));
        }

        if (feedback.getUser() != null) {
            response.setUserId(feedback.getUser().getId());
            response.setUserName(buildUserName(feedback.getUser()));
            response.setStudentName(buildUserName(feedback.getUser()));
        }

        response.setRating(feedback.getRating());
        response.setCleanlinessRating(feedback.getCleanlinessRating());
        response.setUsefulnessRating(feedback.getUsefulnessRating());
        response.setAverageRating(calculateAverageRating(feedback));
        response.setComments(feedback.getComments());
        response.setComment(feedback.getComments());
        response.setMaintenanceIssue(feedback.getMaintenanceIssue());
        response.setCreatedAt(feedback.getCreatedAt());

        return response;
    }
}
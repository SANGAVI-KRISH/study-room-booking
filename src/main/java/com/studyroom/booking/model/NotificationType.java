package com.studyroom.booking.model;

public enum NotificationType {

    BOOKING_CONFIRMED,
    BOOKING_CANCELLED,
    BOOKING_REMINDER,
    BOOKING_APPROVED,
    BOOKING_REJECTED,
    WAITLIST_AVAILABLE;

    // Optional: readable label for UI
    public String getDisplayName() {
        return switch (this) {
            case BOOKING_CONFIRMED -> "Booking Confirmed";
            case BOOKING_CANCELLED -> "Booking Cancelled";
            case BOOKING_REMINDER -> "Booking Reminder";
            case BOOKING_APPROVED -> "Booking Approved";
            case BOOKING_REJECTED -> "Booking Rejected";
            case WAITLIST_AVAILABLE -> "Waitlist Available";
        };
    }
}
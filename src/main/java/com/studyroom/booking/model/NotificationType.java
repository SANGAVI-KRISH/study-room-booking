package com.studyroom.booking.model;

public enum NotificationType {

    // 🔹 Admin related
    APPROVAL_REQUEST,

    // 🔹 Booking lifecycle
    BOOKING_CREATED,
    BOOKING_CONFIRMED,
    BOOKING_APPROVED,
    BOOKING_REJECTED,
    BOOKING_CANCELLED,

    // 🔹 Reminder
    BOOKING_REMINDER,

    // 🔹 Other
    WAITLIST_AVAILABLE;

    // Optional: readable label for UI
    public String getDisplayName() {
        return switch (this) {
            case APPROVAL_REQUEST -> "Approval Request";
            case BOOKING_CREATED -> "Booking Created";
            case BOOKING_CONFIRMED -> "Booking Confirmed";
            case BOOKING_APPROVED -> "Booking Approved";
            case BOOKING_REJECTED -> "Booking Rejected";
            case BOOKING_CANCELLED -> "Booking Cancelled";
            case BOOKING_REMINDER -> "Booking Reminder";
            case WAITLIST_AVAILABLE -> "Waitlist Available";
        };
    }
}
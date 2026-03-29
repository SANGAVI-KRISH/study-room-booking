package com.studyroom.booking.model;

public enum NotificationType {

    // ================= ADMIN RELATED =================
    APPROVAL_REQUEST,

    // ================= BOOKING LIFECYCLE =================
    BOOKING_CREATED,
    BOOKING_CONFIRMED,
    BOOKING_APPROVED,
    BOOKING_REJECTED,
    BOOKING_CANCELLED,
    BOOKING_AUTO_CANCELLED,

    // ================= CHECK-IN / ATTENDANCE =================
    CHECK_IN_SUCCESS,

    // ================= REMINDERS =================
    BOOKING_REMINDER,

    // ================= WAITLIST =================
    WAITLIST_JOINED,
    WAITLIST_AVAILABLE,
    WAITLIST_ASSIGNED,
    WAITLIST_EXPIRED,
    WAITLIST_CANCELLED;

    public String getDisplayName() {
        return switch (this) {
            case APPROVAL_REQUEST -> "Approval Request";

            case BOOKING_CREATED -> "Booking Created";
            case BOOKING_CONFIRMED -> "Booking Confirmed";
            case BOOKING_APPROVED -> "Booking Approved";
            case BOOKING_REJECTED -> "Booking Rejected";
            case BOOKING_CANCELLED -> "Booking Cancelled";
            case BOOKING_AUTO_CANCELLED -> "Booking Auto-Cancelled";

            case CHECK_IN_SUCCESS -> "Check-In Successful";

            case BOOKING_REMINDER -> "Booking Reminder";

            case WAITLIST_JOINED -> "Waitlist Joined";
            case WAITLIST_AVAILABLE -> "Waitlist Available";
            case WAITLIST_ASSIGNED -> "Waitlist Assigned";
            case WAITLIST_EXPIRED -> "Waitlist Expired";
            case WAITLIST_CANCELLED -> "Waitlist Cancelled";
        };
    }
}
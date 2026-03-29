package com.studyroom.booking.model;

public enum BookingStatus {
    PENDING("pending"),
    APPROVED("approved"),
    REJECTED("rejected"),
    CANCELLED("cancelled"),
    CHECKED_IN("checked_in"),
    COMPLETED("completed"),
    NO_SHOW("no_show"),
    AUTO_CANCELLED("auto_cancelled");

    private final String value;

    BookingStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public boolean isActive() {
        return this == PENDING || this == APPROVED || this == CHECKED_IN;
    }

    public boolean isFinalStatus() {
        return this == REJECTED
                || this == CANCELLED
                || this == COMPLETED
                || this == NO_SHOW
                || this == AUTO_CANCELLED;
    }

    public boolean canBeCancelled() {
        return this == PENDING || this == APPROVED;
    }

    public boolean canBeCheckedIn() {
        return this == APPROVED;
    }

    public boolean canBeCompleted() {
        return this == CHECKED_IN;
    }

    public boolean freesSlot() {
        return this == CANCELLED
                || this == REJECTED
                || this == NO_SHOW
                || this == AUTO_CANCELLED
                || this == COMPLETED;
    }

    public static BookingStatus fromValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Booking status cannot be null or empty");
        }

        String normalized = value.trim();

        for (BookingStatus status : values()) {
            if (status.value.equalsIgnoreCase(normalized)
                    || status.name().equalsIgnoreCase(normalized)) {
                return status;
            }
        }

        throw new IllegalArgumentException("Unknown booking status: " + value);
    }

    @Override
    public String toString() {
        return value;
    }
}
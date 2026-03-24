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

    public static BookingStatus fromValue(String value) {
        for (BookingStatus status : values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown booking status: " + value);
    }
}
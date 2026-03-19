package com.studyroom.booking.dto;

public class CancellationAnalysisDto {
    private Long cancelledBookings;
    private Long autoCancelledBookings;
    private Long totalCancellations;

    public CancellationAnalysisDto() {}

    public CancellationAnalysisDto(Long cancelledBookings, Long autoCancelledBookings, Long totalCancellations) {
        this.cancelledBookings = cancelledBookings;
        this.autoCancelledBookings = autoCancelledBookings;
        this.totalCancellations = totalCancellations;
    }

    public Long getCancelledBookings() {
        return cancelledBookings;
    }

    public void setCancelledBookings(Long cancelledBookings) {
        this.cancelledBookings = cancelledBookings;
    }

    public Long getAutoCancelledBookings() {
        return autoCancelledBookings;
    }

    public void setAutoCancelledBookings(Long autoCancelledBookings) {
        this.autoCancelledBookings = autoCancelledBookings;
    }

    public Long getTotalCancellations() {
        return totalCancellations;
    }

    public void setTotalCancellations(Long totalCancellations) {
        this.totalCancellations = totalCancellations;
    }
}
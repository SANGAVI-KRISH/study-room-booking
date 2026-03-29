package com.studyroom.booking.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Converter(autoApply = false)
public class BookingStatusConverter implements AttributeConverter<BookingStatus, String> {

    private static final Logger logger = LoggerFactory.getLogger(BookingStatusConverter.class);

    @Override
    public String convertToDatabaseColumn(BookingStatus status) {
        if (status == null) {
            return null;
        }
        return status.getValue(); // always store lowercase DB value
    }

    @Override
    public BookingStatus convertToEntityAttribute(String dbValue) {
        if (dbValue == null || dbValue.trim().isEmpty()) {
            return null;
        }

        try {
            return BookingStatus.fromValue(dbValue);
        } catch (IllegalArgumentException ex) {
            // 🔥 Safety fallback to avoid app crash if DB has invalid value
            logger.error("Invalid booking status in DB: {}", dbValue, ex);

            // You can choose fallback behavior:
            return BookingStatus.PENDING; // safe default
        }
    }
}
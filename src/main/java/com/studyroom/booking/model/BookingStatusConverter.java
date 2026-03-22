package com.studyroom.booking.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class BookingStatusConverter implements AttributeConverter<BookingStatus, String> {

    @Override
    public String convertToDatabaseColumn(BookingStatus status) {
        return status == null ? null : status.getValue();
    }

    @Override
    public BookingStatus convertToEntityAttribute(String dbValue) {
        return dbValue == null ? null : BookingStatus.fromValue(dbValue);
    }
}
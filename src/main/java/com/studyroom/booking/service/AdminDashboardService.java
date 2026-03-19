package com.studyroom.booking.service;

import com.studyroom.booking.dto.AdminDashboardDto;
import com.studyroom.booking.dto.RoomUsageTrendDto;
import com.studyroom.booking.model.BookingStatus;
import com.studyroom.booking.repository.BookingRepository;
import com.studyroom.booking.repository.StudyRoomRepository;
import com.studyroom.booking.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AdminDashboardService {

    private final StudyRoomRepository studyRoomRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;

    public AdminDashboardService(StudyRoomRepository studyRoomRepository,
                                 UserRepository userRepository,
                                 BookingRepository bookingRepository) {
        this.studyRoomRepository = studyRoomRepository;
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
    }

    public AdminDashboardDto getDashboardStats() {
        long totalRooms = studyRoomRepository.count();
        long totalUsers = userRepository.count();
        long totalBookings = bookingRepository.count();

        long activeBookings = bookingRepository.countByStatusIn(
                List.of(BookingStatus.PENDING, BookingStatus.APPROVED)
        );

        long cancelledBookings = bookingRepository.countByStatus(BookingStatus.CANCELLED);

        String mostBookedRoom = getMostBookedRoom();
        String peakBookingHour = getPeakBookingHour();
        List<RoomUsageTrendDto> roomUsageTrends = getRoomUsageTrends();

        return new AdminDashboardDto(
                totalRooms,
                totalUsers,
                totalBookings,
                activeBookings,
                cancelledBookings,
                mostBookedRoom,
                peakBookingHour,
                roomUsageTrends
        );
    }

    private String getMostBookedRoom() {
        List<Object[]> results = bookingRepository.findMostBookedRoom();
        if (results == null || results.isEmpty()) {
            return "No bookings yet";
        }

        Object[] row = results.get(0);
        String roomName = row[0] != null ? row[0].toString() : "Unknown Room";
        long count = ((Number) row[1]).longValue();

        return roomName + " (" + count + " bookings)";
    }

    private String getPeakBookingHour() {
        List<Object[]> results = bookingRepository.findPeakBookingHours();
        if (results == null || results.isEmpty()) {
            return "No bookings yet";
        }

        Object[] row = results.get(0);
        int hour = ((Number) row[0]).intValue();
        long count = ((Number) row[1]).longValue();

        String formattedHour = String.format("%02d:00 - %02d:00", hour, (hour + 1) % 24);
        return formattedHour + " (" + count + " bookings)";
    }

    private List<RoomUsageTrendDto> getRoomUsageTrends() {
        List<Object[]> results = bookingRepository.findRoomUsageTrends();
        List<RoomUsageTrendDto> trends = new ArrayList<>();

        for (Object[] row : results) {
            String roomName = row[0] != null ? row[0].toString() : "Unknown Room";
            long bookingCount = ((Number) row[1]).longValue();
            trends.add(new RoomUsageTrendDto(roomName, bookingCount));
        }

        return trends;
    }
}
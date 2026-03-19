package com.studyroom.booking.service;

import com.studyroom.booking.dto.BookingCountReportDto;
import com.studyroom.booking.dto.CancellationAnalysisDto;
import com.studyroom.booking.dto.RoomUtilizationDto;
import com.studyroom.booking.dto.UserActivityDto;
import com.studyroom.booking.model.BookingStatus;
import com.studyroom.booking.repository.BookingRepository;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ReportService {

    private final BookingRepository bookingRepository;

    public ReportService(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    public BookingCountReportDto getDailyBookingReport() {
        OffsetDateTime start = OffsetDateTime.now()
                .withHour(0)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);
        OffsetDateTime end = start.plusDays(1);

        long total = bookingRepository.countByStartAtBetween(start, end);
        return new BookingCountReportDto("Today", total);
    }

    public BookingCountReportDto getWeeklyBookingReport() {
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime start = now.with(DayOfWeek.MONDAY)
                .withHour(0)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);
        OffsetDateTime end = start.plusDays(7);

        long total = bookingRepository.countByStartAtBetween(start, end);
        return new BookingCountReportDto("This Week", total);
    }

    public BookingCountReportDto getMonthlyBookingReport() {
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime start = now.withDayOfMonth(1)
                .withHour(0)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);
        OffsetDateTime end = start.plusMonths(1);

        long total = bookingRepository.countByStartAtBetween(start, end);
        return new BookingCountReportDto("This Month", total);
    }

    public List<RoomUtilizationDto> getRoomUtilizationReport() {
        List<Object[]> rows = bookingRepository.findRoomUtilizationReport();
        List<RoomUtilizationDto> result = new ArrayList<>();

        for (Object[] row : rows) {
            String roomName = row[0] != null ? row[0].toString() : "Unknown Room";
            long totalBookings = row[1] != null ? ((Number) row[1]).longValue() : 0L;
            long totalHoursBooked = row[2] != null ? ((Number) row[2]).longValue() : 0L;

            result.add(new RoomUtilizationDto(roomName, totalBookings, totalHoursBooked));
        }

        return result;
    }

    public List<Map<String, Object>> getFrequentlyUsedRooms() {
        List<Object[]> rows = bookingRepository.findFrequentlyUsedRooms();
        List<Map<String, Object>> result = new ArrayList<>();

        for (Object[] row : rows) {
            String roomName = row[0] != null ? row[0].toString() : "Unknown Room";
            long bookingCount = row[1] != null ? ((Number) row[1]).longValue() : 0L;

            result.add(Map.of(
                    "roomName", roomName,
                    "bookingCount", bookingCount
            ));
        }

        return result;
    }

    public CancellationAnalysisDto getCancellationAnalysis() {
        OffsetDateTime start = OffsetDateTime.now().minusMonths(1);
        OffsetDateTime end = OffsetDateTime.now();

        long cancelled = bookingRepository.countByStatusAndStartAtBetween(
                BookingStatus.CANCELLED, start, end
        );

        long autoCancelled = bookingRepository.countByStatusAndStartAtBetween(
                BookingStatus.AUTO_CANCELLED, start, end
        );

        return new CancellationAnalysisDto(
                cancelled,
                autoCancelled,
                cancelled + autoCancelled
        );
    }

    public List<UserActivityDto> getUserActivityReport() {
        List<Object[]> rows = bookingRepository.findUserActivityReport();
        List<UserActivityDto> result = new ArrayList<>();

        for (Object[] row : rows) {
            String userName = row[0] != null ? row[0].toString() : "Unknown User";
            String email = row[1] != null ? row[1].toString() : "No Email";
            long totalBookings = row[2] != null ? ((Number) row[2]).longValue() : 0L;

            result.add(new UserActivityDto(userName, email, totalBookings));
        }

        return result;
    }
}
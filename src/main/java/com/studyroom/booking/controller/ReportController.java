package com.studyroom.booking.controller;

import com.studyroom.booking.service.ReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "http://localhost:5173")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/daily")
    public ResponseEntity<?> getDailyReport() {
        return ResponseEntity.ok(reportService.getDailyBookingReport());
    }

    @GetMapping("/weekly")
    public ResponseEntity<?> getWeeklyReport() {
        return ResponseEntity.ok(reportService.getWeeklyBookingReport());
    }

    @GetMapping("/monthly")
    public ResponseEntity<?> getMonthlyReport() {
        return ResponseEntity.ok(reportService.getMonthlyBookingReport());
    }

    @GetMapping("/room-utilization")
    public ResponseEntity<?> getRoomUtilizationReport() {
        return ResponseEntity.ok(reportService.getRoomUtilizationReport());
    }

    @GetMapping("/frequently-used-rooms")
    public ResponseEntity<?> getFrequentlyUsedRooms() {
        return ResponseEntity.ok(reportService.getFrequentlyUsedRooms());
    }

    @GetMapping("/cancellation-analysis")
    public ResponseEntity<?> getCancellationAnalysis() {
        return ResponseEntity.ok(reportService.getCancellationAnalysis());
    }

    @GetMapping("/user-activity")
    public ResponseEntity<?> getUserActivityReport() {
        return ResponseEntity.ok(reportService.getUserActivityReport());
    }
}
package com.studyroom.booking.controller;

import com.studyroom.booking.dto.MaintenanceRequest;
import com.studyroom.booking.dto.MaintenanceResponse;
import com.studyroom.booking.service.MaintenanceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/maintenance")
@CrossOrigin(origins = "*")
public class MaintenanceController {

    private final MaintenanceService maintenanceService;

    public MaintenanceController(MaintenanceService maintenanceService) {
        this.maintenanceService = maintenanceService;
    }

    /* ================= CREATE ================= */
    @PostMapping
    public ResponseEntity<MaintenanceResponse> createMaintenance(@RequestBody MaintenanceRequest request) {
        MaintenanceResponse response = maintenanceService.createMaintenanceBlock(request);
        return ResponseEntity.ok(response);
    }

    /* ================= GET ACTIVE / UPCOMING ================= */
    @GetMapping
    public ResponseEntity<List<MaintenanceResponse>> getActiveMaintenanceBlocks() {
        List<MaintenanceResponse> response = maintenanceService.getActiveMaintenanceBlocks();
        return ResponseEntity.ok(response);
    }

    /* ================= GET BY ID ================= */
    @GetMapping("/{maintenanceId}")
    public ResponseEntity<MaintenanceResponse> getMaintenanceById(@PathVariable UUID maintenanceId) {
        MaintenanceResponse response = maintenanceService.getMaintenanceById(maintenanceId);
        return ResponseEntity.ok(response);
    }

    /* ================= DEACTIVATE ================= */
    @PutMapping("/{maintenanceId}/deactivate")
    public ResponseEntity<MaintenanceResponse> deactivateMaintenance(@PathVariable UUID maintenanceId) {
        MaintenanceResponse response = maintenanceService.deactivateMaintenanceBlock(maintenanceId);
        return ResponseEntity.ok(response);
    }

    /* ================= DELETE ================= */
    @DeleteMapping("/{maintenanceId}")
    public ResponseEntity<String> deleteMaintenance(@PathVariable UUID maintenanceId) {
        maintenanceService.deleteMaintenanceBlock(maintenanceId);
        return ResponseEntity.ok("Maintenance block deleted successfully");
    }
}
package com.studyroom.booking.repository;

import com.studyroom.booking.model.StudyRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StudyRoomRepository extends JpaRepository<StudyRoom, UUID> {

    /* ================= BASIC SEARCH ================= */

    // Find room by ID is already available from JpaRepository

    // Find rooms by exact location
    List<StudyRoom> findByLocationIgnoreCase(String location);

    // Search rooms by location (partial match)
    List<StudyRoom> findByLocationContainingIgnoreCase(String location);

    // Filter rooms by district
    List<StudyRoom> findByDistrictIgnoreCase(String district);

    // Search rooms by facilities
    List<StudyRoom> findByFacilitiesContainingIgnoreCase(String facilities);

    // Filter rooms by minimum seating capacity
    List<StudyRoom> findBySeatingCapacityGreaterThanEqual(Integer seatingCapacity);

    // Find room by block name and room number
    Optional<StudyRoom> findByBlockNameIgnoreCaseAndRoomNumberIgnoreCase(String blockName, String roomNumber);

    // Check if room already exists
    boolean existsByBlockNameIgnoreCaseAndRoomNumberIgnoreCase(String blockName, String roomNumber);

    /* ================= APPROVAL FILTER ================= */

    List<StudyRoom> findByApprovalRequired(boolean approvalRequired);

    /* ================= FEE FILTER ================= */

    List<StudyRoom> findByFeePerHourLessThanEqual(BigDecimal feePerHour);

    List<StudyRoom> findByFeePerHourGreaterThanEqual(BigDecimal feePerHour);

    List<StudyRoom> findByDistrictIgnoreCaseAndFeePerHourLessThanEqual(String district, BigDecimal feePerHour);

    List<StudyRoom> findByLocationContainingIgnoreCaseAndFeePerHourLessThanEqual(String location, BigDecimal feePerHour);

    /* ================= COMBINATION FILTERS ================= */

    // Filter by district + location
    List<StudyRoom> findByDistrictIgnoreCaseAndLocationContainingIgnoreCase(
            String district,
            String location
    );

    // Filter by location + seating capacity
    List<StudyRoom> findByLocationContainingIgnoreCaseAndSeatingCapacityGreaterThanEqual(
            String location,
            Integer seatingCapacity
    );

    // Filter by location + facilities
    List<StudyRoom> findByLocationContainingIgnoreCaseAndFacilitiesContainingIgnoreCase(
            String location,
            String facilities
    );

    // Filter by seating capacity + facilities
    List<StudyRoom> findBySeatingCapacityGreaterThanEqualAndFacilitiesContainingIgnoreCase(
            Integer seatingCapacity,
            String facilities
    );

    // Filter by district + seating capacity
    List<StudyRoom> findByDistrictIgnoreCaseAndSeatingCapacityGreaterThanEqual(
            String district,
            Integer seatingCapacity
    );

    // Filter by district + facilities
    List<StudyRoom> findByDistrictIgnoreCaseAndFacilitiesContainingIgnoreCase(
            String district,
            String facilities
    );

    // Filter by district + location + seating capacity
    List<StudyRoom> findByDistrictIgnoreCaseAndLocationContainingIgnoreCaseAndSeatingCapacityGreaterThanEqual(
            String district,
            String location,
            Integer seatingCapacity
    );

    // Filter by district + location + facilities
    List<StudyRoom> findByDistrictIgnoreCaseAndLocationContainingIgnoreCaseAndFacilitiesContainingIgnoreCase(
            String district,
            String location,
            String facilities
    );

    // Filter by location + seating capacity + facilities
    List<StudyRoom> findByLocationContainingIgnoreCaseAndSeatingCapacityGreaterThanEqualAndFacilitiesContainingIgnoreCase(
            String location,
            Integer seatingCapacity,
            String facilities
    );

    // Filter by district + seating capacity + facilities
    List<StudyRoom> findByDistrictIgnoreCaseAndSeatingCapacityGreaterThanEqualAndFacilitiesContainingIgnoreCase(
            String district,
            Integer seatingCapacity,
            String facilities
    );

    // Filter by district + location + seating capacity + facilities
    List<StudyRoom> findByDistrictIgnoreCaseAndLocationContainingIgnoreCaseAndSeatingCapacityGreaterThanEqualAndFacilitiesContainingIgnoreCase(
            String district,
            String location,
            Integer seatingCapacity,
            String facilities
    );

    /* ================= EXTRA USEFUL FILTERS ================= */

    List<StudyRoom> findByDistrictIgnoreCaseAndApprovalRequired(String district, boolean approvalRequired);

    List<StudyRoom> findByLocationContainingIgnoreCaseAndApprovalRequired(String location, boolean approvalRequired);

    List<StudyRoom> findByDistrictIgnoreCaseAndLocationContainingIgnoreCaseAndApprovalRequired(
            String district,
            String location,
            boolean approvalRequired
    );

    List<StudyRoom> findByDistrictIgnoreCaseAndLocationContainingIgnoreCaseAndSeatingCapacityGreaterThanEqualAndFacilitiesContainingIgnoreCaseAndFeePerHourLessThanEqual(
            String district,
            String location,
            Integer seatingCapacity,
            String facilities,
            BigDecimal feePerHour
    );
}
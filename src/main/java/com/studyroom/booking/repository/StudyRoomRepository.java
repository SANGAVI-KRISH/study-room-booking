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

    /* ================= DUPLICATE CHECK ================= */

    Optional<StudyRoom> findByDistrictIgnoreCaseAndLocationIgnoreCaseAndBlockNameIgnoreCaseAndRoomNumberIgnoreCase(
            String district,
            String location,
            String blockName,
            String roomNumber
    );

    boolean existsByDistrictIgnoreCaseAndLocationIgnoreCaseAndBlockNameIgnoreCaseAndRoomNumberIgnoreCase(
            String district,
            String location,
            String blockName,
            String roomNumber
    );

    /* ================= BASIC SEARCH ================= */

    List<StudyRoom> findByLocationIgnoreCase(String location);

    List<StudyRoom> findByLocationContainingIgnoreCase(String location);

    List<StudyRoom> findByDistrictIgnoreCase(String district);

    List<StudyRoom> findByFacilitiesContainingIgnoreCase(String facilities);

    List<StudyRoom> findBySeatingCapacityGreaterThanEqual(Integer seatingCapacity);

    /* ================= APPROVAL FILTER ================= */

    List<StudyRoom> findByApprovalRequired(Boolean approvalRequired);

    List<StudyRoom> findByDistrictIgnoreCaseAndApprovalRequired(String district, Boolean approvalRequired);

    List<StudyRoom> findByLocationContainingIgnoreCaseAndApprovalRequired(String location, Boolean approvalRequired);

    List<StudyRoom> findByDistrictIgnoreCaseAndLocationContainingIgnoreCaseAndApprovalRequired(
            String district,
            String location,
            Boolean approvalRequired
    );

    /* ================= FEE FILTER ================= */

    List<StudyRoom> findByFeePerHourLessThanEqual(BigDecimal feePerHour);

    List<StudyRoom> findByFeePerHourGreaterThanEqual(BigDecimal feePerHour);

    List<StudyRoom> findByDistrictIgnoreCaseAndFeePerHourLessThanEqual(
            String district,
            BigDecimal feePerHour
    );

    List<StudyRoom> findByLocationContainingIgnoreCaseAndFeePerHourLessThanEqual(
            String location,
            BigDecimal feePerHour
    );

    /* ================= COMBINATION FILTERS ================= */

    List<StudyRoom> findByDistrictIgnoreCaseAndLocationContainingIgnoreCase(
            String district,
            String location
    );

    List<StudyRoom> findByLocationContainingIgnoreCaseAndSeatingCapacityGreaterThanEqual(
            String location,
            Integer seatingCapacity
    );

    List<StudyRoom> findByLocationContainingIgnoreCaseAndFacilitiesContainingIgnoreCase(
            String location,
            String facilities
    );

    List<StudyRoom> findBySeatingCapacityGreaterThanEqualAndFacilitiesContainingIgnoreCase(
            Integer seatingCapacity,
            String facilities
    );

    List<StudyRoom> findByDistrictIgnoreCaseAndSeatingCapacityGreaterThanEqual(
            String district,
            Integer seatingCapacity
    );

    List<StudyRoom> findByDistrictIgnoreCaseAndFacilitiesContainingIgnoreCase(
            String district,
            String facilities
    );

    List<StudyRoom> findByDistrictIgnoreCaseAndLocationContainingIgnoreCaseAndSeatingCapacityGreaterThanEqual(
            String district,
            String location,
            Integer seatingCapacity
    );

    List<StudyRoom> findByDistrictIgnoreCaseAndLocationContainingIgnoreCaseAndFacilitiesContainingIgnoreCase(
            String district,
            String location,
            String facilities
    );

    List<StudyRoom> findByLocationContainingIgnoreCaseAndSeatingCapacityGreaterThanEqualAndFacilitiesContainingIgnoreCase(
            String location,
            Integer seatingCapacity,
            String facilities
    );

    List<StudyRoom> findByDistrictIgnoreCaseAndSeatingCapacityGreaterThanEqualAndFacilitiesContainingIgnoreCase(
            String district,
            Integer seatingCapacity,
            String facilities
    );

    List<StudyRoom> findByDistrictIgnoreCaseAndLocationContainingIgnoreCaseAndSeatingCapacityGreaterThanEqualAndFacilitiesContainingIgnoreCase(
            String district,
            String location,
            Integer seatingCapacity,
            String facilities
    );

    /* ================= FULL FILTER ================= */

    List<StudyRoom> findByDistrictIgnoreCaseAndLocationContainingIgnoreCaseAndSeatingCapacityGreaterThanEqualAndFacilitiesContainingIgnoreCaseAndFeePerHourLessThanEqual(
            String district,
            String location,
            Integer seatingCapacity,
            String facilities,
            BigDecimal feePerHour
    );
}




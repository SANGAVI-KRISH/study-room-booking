package com.studyroom.booking.service;

import com.studyroom.booking.model.StudyRoom;
import com.studyroom.booking.repository.StudyRoomRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class StudyRoomService {

    private final StudyRoomRepository studyRoomRepository;

    public StudyRoomService(StudyRoomRepository studyRoomRepository) {
        this.studyRoomRepository = studyRoomRepository;
    }

    /* ================= CREATE ================= */

    public StudyRoom addRoom(StudyRoom studyRoom) {
        boolean alreadyExists = studyRoomRepository
                .existsByBlockNameIgnoreCaseAndRoomNumberIgnoreCase(
                        studyRoom.getBlockName(),
                        studyRoom.getRoomNumber()
                );

        if (alreadyExists) {
            throw new RuntimeException(
                    "Room already exists with block name '" + studyRoom.getBlockName()
                            + "' and room number '" + studyRoom.getRoomNumber() + "'"
            );
        }

        return studyRoomRepository.save(studyRoom);
    }

    /* ================= READ ================= */

    public List<StudyRoom> getAllRooms() {
        return studyRoomRepository.findAll();
    }

    public Optional<StudyRoom> getRoomById(UUID id) {
        return studyRoomRepository.findById(id);
    }

    public StudyRoom getExistingRoomById(UUID id) {
        return studyRoomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found with id: " + id));
    }

    /* ================= UPDATE ================= */

    public StudyRoom updateRoom(UUID id, StudyRoom updatedRoom) {
        StudyRoom existingRoom = studyRoomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found with id: " + id));

        Optional<StudyRoom> duplicateRoom = studyRoomRepository
                .findByBlockNameIgnoreCaseAndRoomNumberIgnoreCase(
                        updatedRoom.getBlockName(),
                        updatedRoom.getRoomNumber()
                );

        if (duplicateRoom.isPresent() && !duplicateRoom.get().getId().equals(id)) {
            throw new RuntimeException(
                    "Another room already exists with block name '" + updatedRoom.getBlockName()
                            + "' and room number '" + updatedRoom.getRoomNumber() + "'"
            );
        }

        existingRoom.setBlockName(updatedRoom.getBlockName());
        existingRoom.setRoomNumber(updatedRoom.getRoomNumber());
        existingRoom.setFloorNumber(updatedRoom.getFloorNumber());
        existingRoom.setSeatingCapacity(updatedRoom.getSeatingCapacity());
        existingRoom.setAvailabilityTimings(updatedRoom.getAvailabilityTimings());
        existingRoom.setFacilities(updatedRoom.getFacilities());
        existingRoom.setDistrict(updatedRoom.getDistrict());
        existingRoom.setLocation(updatedRoom.getLocation());
        existingRoom.setFeePerHour(updatedRoom.getFeePerHour());
        existingRoom.setApprovalRequired(updatedRoom.isApprovalRequired());

        return studyRoomRepository.save(existingRoom);
    }

    /* ================= DELETE ================= */

    public void deleteRoom(UUID id) {
        StudyRoom existingRoom = studyRoomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found with id: " + id));

        studyRoomRepository.delete(existingRoom);
    }

    /* ================= BASIC SEARCH ================= */

    public List<StudyRoom> getRoomsByExactLocation(String location) {
        return studyRoomRepository.findByLocationIgnoreCase(location);
    }

    public List<StudyRoom> getRoomsByLocation(String location) {
        return studyRoomRepository.findByLocationContainingIgnoreCase(location);
    }

    public List<StudyRoom> getRoomsByDistrict(String district) {
        return studyRoomRepository.findByDistrictIgnoreCase(district);
    }

    public List<StudyRoom> getRoomsByFacilities(String facilities) {
        return studyRoomRepository.findByFacilitiesContainingIgnoreCase(facilities);
    }

    public List<StudyRoom> getRoomsByMinimumCapacity(Integer seatingCapacity) {
        return studyRoomRepository.findBySeatingCapacityGreaterThanEqual(seatingCapacity);
    }

    public List<StudyRoom> getRoomsByApprovalRequired(boolean approvalRequired) {
        return studyRoomRepository.findByApprovalRequired(approvalRequired);
    }

    /* ================= FEE FILTER ================= */

    public List<StudyRoom> getRoomsByMaximumFee(BigDecimal feePerHour) {
        return studyRoomRepository.findByFeePerHourLessThanEqual(feePerHour);
    }

    public List<StudyRoom> getRoomsByMinimumFee(BigDecimal feePerHour) {
        return studyRoomRepository.findByFeePerHourGreaterThanEqual(feePerHour);
    }

    public List<StudyRoom> getRoomsByDistrictAndMaximumFee(String district, BigDecimal feePerHour) {
        return studyRoomRepository.findByDistrictIgnoreCaseAndFeePerHourLessThanEqual(district, feePerHour);
    }

    public List<StudyRoom> getRoomsByLocationAndMaximumFee(String location, BigDecimal feePerHour) {
        return studyRoomRepository.findByLocationContainingIgnoreCaseAndFeePerHourLessThanEqual(location, feePerHour);
    }

    /* ================= COMBINATION FILTERS ================= */

    public List<StudyRoom> getRoomsByDistrictAndLocation(String district, String location) {
        return studyRoomRepository.findByDistrictIgnoreCaseAndLocationContainingIgnoreCase(district, location);
    }

    public List<StudyRoom> getRoomsByLocationAndCapacity(String location, Integer seatingCapacity) {
        return studyRoomRepository
                .findByLocationContainingIgnoreCaseAndSeatingCapacityGreaterThanEqual(location, seatingCapacity);
    }

    public List<StudyRoom> getRoomsByLocationAndFacilities(String location, String facilities) {
        return studyRoomRepository
                .findByLocationContainingIgnoreCaseAndFacilitiesContainingIgnoreCase(location, facilities);
    }

    public List<StudyRoom> getRoomsByCapacityAndFacilities(Integer seatingCapacity, String facilities) {
        return studyRoomRepository
                .findBySeatingCapacityGreaterThanEqualAndFacilitiesContainingIgnoreCase(seatingCapacity, facilities);
    }

    public List<StudyRoom> getRoomsByDistrictAndCapacity(String district, Integer seatingCapacity) {
        return studyRoomRepository
                .findByDistrictIgnoreCaseAndSeatingCapacityGreaterThanEqual(district, seatingCapacity);
    }

    public List<StudyRoom> getRoomsByDistrictAndFacilities(String district, String facilities) {
        return studyRoomRepository
                .findByDistrictIgnoreCaseAndFacilitiesContainingIgnoreCase(district, facilities);
    }

    public List<StudyRoom> getRoomsByDistrictLocationAndCapacity(
            String district,
            String location,
            Integer seatingCapacity
    ) {
        return studyRoomRepository
                .findByDistrictIgnoreCaseAndLocationContainingIgnoreCaseAndSeatingCapacityGreaterThanEqual(
                        district, location, seatingCapacity
                );
    }

    public List<StudyRoom> getRoomsByDistrictLocationAndFacilities(
            String district,
            String location,
            String facilities
    ) {
        return studyRoomRepository
                .findByDistrictIgnoreCaseAndLocationContainingIgnoreCaseAndFacilitiesContainingIgnoreCase(
                        district, location, facilities
                );
    }

    public List<StudyRoom> getRoomsByLocationCapacityAndFacilities(
            String location,
            Integer seatingCapacity,
            String facilities
    ) {
        return studyRoomRepository
                .findByLocationContainingIgnoreCaseAndSeatingCapacityGreaterThanEqualAndFacilitiesContainingIgnoreCase(
                        location, seatingCapacity, facilities
                );
    }

    public List<StudyRoom> getRoomsByDistrictCapacityAndFacilities(
            String district,
            Integer seatingCapacity,
            String facilities
    ) {
        return studyRoomRepository
                .findByDistrictIgnoreCaseAndSeatingCapacityGreaterThanEqualAndFacilitiesContainingIgnoreCase(
                        district, seatingCapacity, facilities
                );
    }

    public List<StudyRoom> getRoomsByDistrictLocationCapacityAndFacilities(
            String district,
            String location,
            Integer seatingCapacity,
            String facilities
    ) {
        return studyRoomRepository
                .findByDistrictIgnoreCaseAndLocationContainingIgnoreCaseAndSeatingCapacityGreaterThanEqualAndFacilitiesContainingIgnoreCase(
                        district, location, seatingCapacity, facilities
                );
    }

    /* ================= EXTRA FILTERS ================= */

    public List<StudyRoom> getRoomsByDistrictAndApprovalRequired(String district, boolean approvalRequired) {
        return studyRoomRepository.findByDistrictIgnoreCaseAndApprovalRequired(district, approvalRequired);
    }

    public List<StudyRoom> getRoomsByLocationAndApprovalRequired(String location, boolean approvalRequired) {
        return studyRoomRepository.findByLocationContainingIgnoreCaseAndApprovalRequired(location, approvalRequired);
    }

    public List<StudyRoom> getRoomsByDistrictLocationAndApprovalRequired(
            String district,
            String location,
            boolean approvalRequired
    ) {
        return studyRoomRepository.findByDistrictIgnoreCaseAndLocationContainingIgnoreCaseAndApprovalRequired(
                district, location, approvalRequired
        );
    }

    public List<StudyRoom> getRoomsByAllFilters(
            String district,
            String location,
            Integer seatingCapacity,
            String facilities,
            BigDecimal feePerHour
    ) {
        return studyRoomRepository
                .findByDistrictIgnoreCaseAndLocationContainingIgnoreCaseAndSeatingCapacityGreaterThanEqualAndFacilitiesContainingIgnoreCaseAndFeePerHourLessThanEqual(
                        district, location, seatingCapacity, facilities, feePerHour
                );
    }
}
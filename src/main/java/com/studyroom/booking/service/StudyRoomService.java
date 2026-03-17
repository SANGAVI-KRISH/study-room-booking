package com.studyroom.booking.service;

import com.studyroom.booking.dto.StudyRoomRequest;
import com.studyroom.booking.dto.StudyRoomResponse;
import com.studyroom.booking.model.StudyRoom;
import com.studyroom.booking.model.StudyRoomImage;
import com.studyroom.booking.repository.StudyRoomImageRepository;
import com.studyroom.booking.repository.StudyRoomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class StudyRoomService {

    private final StudyRoomRepository studyRoomRepository;
    private final StudyRoomImageRepository studyRoomImageRepository;
    private final FileStorageService fileStorageService;

    public StudyRoomService(StudyRoomRepository studyRoomRepository,
                            StudyRoomImageRepository studyRoomImageRepository,
                            FileStorageService fileStorageService) {
        this.studyRoomRepository = studyRoomRepository;
        this.studyRoomImageRepository = studyRoomImageRepository;
        this.fileStorageService = fileStorageService;
    }

    /* ================= CREATE ================= */

    public StudyRoomResponse addRoom(StudyRoomRequest request) {
        validateDuplicateRoom(request.getBlockName(), request.getRoomNumber(), null);

        StudyRoom studyRoom = new StudyRoom();
        mapRequestToEntity(request, studyRoom);

        StudyRoom savedRoom = studyRoomRepository.save(studyRoom);

        saveRoomImages(savedRoom, request.getImages());

        StudyRoom finalRoom = studyRoomRepository.findById(savedRoom.getId())
                .orElseThrow(() -> new RuntimeException("Room saved but could not be fetched"));

        return mapToResponse(finalRoom);
    }

    /* ================= READ ================= */

    @Transactional(readOnly = true)
    public List<StudyRoomResponse> getAllRooms() {
        List<StudyRoom> rooms = studyRoomRepository.findAll();
        List<StudyRoomResponse> responseList = new ArrayList<>();

        for (StudyRoom room : rooms) {
            responseList.add(mapToResponse(room));
        }

        return responseList;
    }

    @Transactional(readOnly = true)
    public Optional<StudyRoomResponse> getRoomById(UUID id) {
        return studyRoomRepository.findById(id).map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public StudyRoom getExistingRoomById(UUID id) {
        return studyRoomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found with id: " + id));
    }

    /* ================= UPDATE ================= */

    public StudyRoomResponse updateRoom(UUID id, StudyRoomRequest request) {
        StudyRoom existingRoom = studyRoomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found with id: " + id));

        validateDuplicateRoom(request.getBlockName(), request.getRoomNumber(), id);

        mapRequestToEntity(request, existingRoom);

        MultipartFile[] newImages = request.getImages();
        if (newImages != null && newImages.length > 0) {
            List<StudyRoomImage> oldImages = studyRoomImageRepository.findByRoom_IdOrderByDisplayOrderAscCreatedAtAsc(id);

            List<String> oldImageUrls = new ArrayList<>();
            for (StudyRoomImage image : oldImages) {
                oldImageUrls.add(image.getImageUrl());
            }

            studyRoomImageRepository.deleteByRoom_Id(id);
            fileStorageService.deleteFiles(oldImageUrls);

            existingRoom.clearImages();
            saveRoomImages(existingRoom, newImages);
        }

        StudyRoom updatedRoom = studyRoomRepository.save(existingRoom);
        return mapToResponse(updatedRoom);
    }

    /* ================= DELETE ================= */

    public void deleteRoom(UUID id) {
        StudyRoom existingRoom = studyRoomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found with id: " + id));

        List<StudyRoomImage> images = studyRoomImageRepository.findByRoom_IdOrderByDisplayOrderAscCreatedAtAsc(id);
        List<String> imageUrls = new ArrayList<>();

        for (StudyRoomImage image : images) {
            imageUrls.add(image.getImageUrl());
        }

        studyRoomRepository.delete(existingRoom);
        fileStorageService.deleteFiles(imageUrls);
    }

    /* ================= BASIC SEARCH ================= */

    @Transactional(readOnly = true)
    public List<StudyRoom> getRoomsByExactLocation(String location) {
        return studyRoomRepository.findByLocationIgnoreCase(location);
    }

    @Transactional(readOnly = true)
    public List<StudyRoom> getRoomsByLocation(String location) {
        return studyRoomRepository.findByLocationContainingIgnoreCase(location);
    }

    @Transactional(readOnly = true)
    public List<StudyRoom> getRoomsByDistrict(String district) {
        return studyRoomRepository.findByDistrictIgnoreCase(district);
    }

    @Transactional(readOnly = true)
    public List<StudyRoom> getRoomsByFacilities(String facilities) {
        return studyRoomRepository.findByFacilitiesContainingIgnoreCase(facilities);
    }

    @Transactional(readOnly = true)
    public List<StudyRoom> getRoomsByMinimumCapacity(Integer seatingCapacity) {
        return studyRoomRepository.findBySeatingCapacityGreaterThanEqual(seatingCapacity);
    }

    @Transactional(readOnly = true)
    public List<StudyRoom> getRoomsByApprovalRequired(boolean approvalRequired) {
        return studyRoomRepository.findByApprovalRequired(approvalRequired);
    }

    /* ================= FEE FILTER ================= */

    @Transactional(readOnly = true)
    public List<StudyRoom> getRoomsByMaximumFee(BigDecimal feePerHour) {
        return studyRoomRepository.findByFeePerHourLessThanEqual(feePerHour);
    }

    @Transactional(readOnly = true)
    public List<StudyRoom> getRoomsByMinimumFee(BigDecimal feePerHour) {
        return studyRoomRepository.findByFeePerHourGreaterThanEqual(feePerHour);
    }

    @Transactional(readOnly = true)
    public List<StudyRoom> getRoomsByDistrictAndMaximumFee(String district, BigDecimal feePerHour) {
        return studyRoomRepository.findByDistrictIgnoreCaseAndFeePerHourLessThanEqual(district, feePerHour);
    }

    @Transactional(readOnly = true)
    public List<StudyRoom> getRoomsByLocationAndMaximumFee(String location, BigDecimal feePerHour) {
        return studyRoomRepository.findByLocationContainingIgnoreCaseAndFeePerHourLessThanEqual(location, feePerHour);
    }

    /* ================= COMBINATION FILTERS ================= */

    @Transactional(readOnly = true)
    public List<StudyRoom> getRoomsByDistrictAndLocation(String district, String location) {
        return studyRoomRepository.findByDistrictIgnoreCaseAndLocationContainingIgnoreCase(district, location);
    }

    @Transactional(readOnly = true)
    public List<StudyRoom> getRoomsByLocationAndCapacity(String location, Integer seatingCapacity) {
        return studyRoomRepository
                .findByLocationContainingIgnoreCaseAndSeatingCapacityGreaterThanEqual(location, seatingCapacity);
    }

    @Transactional(readOnly = true)
    public List<StudyRoom> getRoomsByLocationAndFacilities(String location, String facilities) {
        return studyRoomRepository
                .findByLocationContainingIgnoreCaseAndFacilitiesContainingIgnoreCase(location, facilities);
    }

    @Transactional(readOnly = true)
    public List<StudyRoom> getRoomsByCapacityAndFacilities(Integer seatingCapacity, String facilities) {
        return studyRoomRepository
                .findBySeatingCapacityGreaterThanEqualAndFacilitiesContainingIgnoreCase(seatingCapacity, facilities);
    }

    @Transactional(readOnly = true)
    public List<StudyRoom> getRoomsByDistrictAndCapacity(String district, Integer seatingCapacity) {
        return studyRoomRepository
                .findByDistrictIgnoreCaseAndSeatingCapacityGreaterThanEqual(district, seatingCapacity);
    }

    @Transactional(readOnly = true)
    public List<StudyRoom> getRoomsByDistrictAndFacilities(String district, String facilities) {
        return studyRoomRepository
                .findByDistrictIgnoreCaseAndFacilitiesContainingIgnoreCase(district, facilities);
    }

    @Transactional(readOnly = true)
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

    @Transactional(readOnly = true)
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

    @Transactional(readOnly = true)
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

    @Transactional(readOnly = true)
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

    @Transactional(readOnly = true)
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

    @Transactional(readOnly = true)
    public List<StudyRoom> getRoomsByDistrictAndApprovalRequired(String district, boolean approvalRequired) {
        return studyRoomRepository.findByDistrictIgnoreCaseAndApprovalRequired(district, approvalRequired);
    }

    @Transactional(readOnly = true)
    public List<StudyRoom> getRoomsByLocationAndApprovalRequired(String location, boolean approvalRequired) {
        return studyRoomRepository.findByLocationContainingIgnoreCaseAndApprovalRequired(location, approvalRequired);
    }

    @Transactional(readOnly = true)
    public List<StudyRoom> getRoomsByDistrictLocationAndApprovalRequired(
            String district,
            String location,
            boolean approvalRequired
    ) {
        return studyRoomRepository.findByDistrictIgnoreCaseAndLocationContainingIgnoreCaseAndApprovalRequired(
                district, location, approvalRequired
        );
    }

    @Transactional(readOnly = true)
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

    /* ================= HELPER METHODS ================= */

    private void validateDuplicateRoom(String blockName, String roomNumber, UUID currentRoomId) {
        if (blockName == null || roomNumber == null) {
            return;
        }

        Optional<StudyRoom> duplicateRoom = studyRoomRepository
                .findByBlockNameIgnoreCaseAndRoomNumberIgnoreCase(blockName, roomNumber);

        if (duplicateRoom.isPresent()) {
            if (currentRoomId == null || !duplicateRoom.get().getId().equals(currentRoomId)) {
                throw new RuntimeException(
                        "Another room already exists with block name '" + blockName
                                + "' and room number '" + roomNumber + "'"
                );
            }
        }
    }

    private void mapRequestToEntity(StudyRoomRequest request, StudyRoom studyRoom) {
        studyRoom.setBlockName(request.getBlockName());
        studyRoom.setRoomNumber(request.getRoomNumber());
        studyRoom.setFloorNumber(request.getFloorNumber());
        studyRoom.setSeatingCapacity(request.getSeatingCapacity());
        studyRoom.setAvailabilityTimings(request.getAvailabilityTimings());
        studyRoom.setFacilities(request.getFacilities());
        studyRoom.setDistrict(request.getDistrict());
        studyRoom.setLocation(request.getLocation());
        studyRoom.setFeePerHour(request.getFeePerHour());
        studyRoom.setApprovalRequired(Boolean.TRUE.equals(request.getApprovalRequired()));
    }

    private void saveRoomImages(StudyRoom room, MultipartFile[] files) {
        if (files == null || files.length == 0) {
            return;
        }

        List<String> imageUrls = fileStorageService.saveFiles(files);

        int displayOrder = 0;
        for (String imageUrl : imageUrls) {
            StudyRoomImage image = new StudyRoomImage();
            image.setRoom(room);
            image.setImageUrl(imageUrl);
            image.setDisplayOrder(displayOrder++);
            room.addImage(image);
        }

        studyRoomRepository.save(room);
    }

    private StudyRoomResponse mapToResponse(StudyRoom room) {
        StudyRoomResponse response = new StudyRoomResponse();
        response.setId(room.getId());
        response.setBlockName(room.getBlockName());
        response.setRoomNumber(room.getRoomNumber());
        response.setFloorNumber(room.getFloorNumber());
        response.setSeatingCapacity(room.getSeatingCapacity());
        response.setAvailabilityTimings(room.getAvailabilityTimings());
        response.setFacilities(room.getFacilities());
        response.setDistrict(room.getDistrict());
        response.setLocation(room.getLocation());
        response.setFeePerHour(room.getFeePerHour());
        response.setApprovalRequired(room.isApprovalRequired());
        response.setCreatedAt(room.getCreatedAt());
        response.setUpdatedAt(room.getUpdatedAt());

        List<StudyRoomImage> images = studyRoomImageRepository
                .findByRoom_IdOrderByDisplayOrderAscCreatedAtAsc(room.getId());

        List<StudyRoomResponse.ImageResponse> imageResponses = new ArrayList<>();
        for (StudyRoomImage image : images) {
            imageResponses.add(
                    new StudyRoomResponse.ImageResponse(
                            image.getId(),
                            image.getImageUrl(),
                            image.getDisplayOrder()
                    )
            );
        }

        response.setImages(imageResponses);
        return response;
    }
}
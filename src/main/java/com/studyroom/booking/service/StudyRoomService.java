package com.studyroom.booking.service;

import com.studyroom.booking.dto.StudyRoomRequest;
import com.studyroom.booking.dto.StudyRoomResponse;
import com.studyroom.booking.model.Booking;
import com.studyroom.booking.model.BookingStatus;
import com.studyroom.booking.model.StudyRoom;
import com.studyroom.booking.model.StudyRoomImage;
import com.studyroom.booking.repository.BookingRepository;
import com.studyroom.booking.repository.StudyRoomImageRepository;
import com.studyroom.booking.repository.StudyRoomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class StudyRoomService {

    private final StudyRoomRepository studyRoomRepository;
    private final StudyRoomImageRepository studyRoomImageRepository;
    private final BookingRepository bookingRepository;
    private final FileStorageService fileStorageService;
    private final NotificationService notificationService;

    public StudyRoomService(
            StudyRoomRepository studyRoomRepository,
            StudyRoomImageRepository studyRoomImageRepository,
            BookingRepository bookingRepository,
            FileStorageService fileStorageService,
            NotificationService notificationService
    ) {
        this.studyRoomRepository = studyRoomRepository;
        this.studyRoomImageRepository = studyRoomImageRepository;
        this.bookingRepository = bookingRepository;
        this.fileStorageService = fileStorageService;
        this.notificationService = notificationService;
    }

    /* ================= CREATE ================= */

    public StudyRoomResponse addRoom(StudyRoomRequest request) {
        validateDuplicateRoom(
                request.getDistrict(),
                request.getLocation(),
                request.getBlockName(),
                request.getRoomNumber(),
                null
        );

        StudyRoom studyRoom = new StudyRoom();
        mapRequestToEntity(request, studyRoom);

        StudyRoom savedRoom = studyRoomRepository.saveAndFlush(studyRoom);

        MultipartFile[] images = request.getImages();
        if (hasValidFiles(images)) {
            replaceRoomImages(savedRoom, images);
        }

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
    public List<StudyRoomResponse> getAllActiveRooms() {
        List<StudyRoom> rooms = studyRoomRepository.findAll()
                .stream()
                .filter(room -> room != null && room.isAvailableForBooking())
                .collect(Collectors.toList());

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

        validateDuplicateRoom(
                request.getDistrict(),
                request.getLocation(),
                request.getBlockName(),
                request.getRoomNumber(),
                id
        );

        mapRequestToEntity(request, existingRoom);

        StudyRoom savedRoom = studyRoomRepository.saveAndFlush(existingRoom);

        MultipartFile[] newImages = request.getImages();
        if (hasValidFiles(newImages)) {
            replaceRoomImages(savedRoom, newImages);
        }

        StudyRoom finalRoom = studyRoomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Updated room could not be fetched with id: " + id));

        return mapToResponse(finalRoom);
    }

    /* ================= ACTIVATE / DEACTIVATE ================= */

    public String deactivateRoom(UUID id) {
        StudyRoom existingRoom = studyRoomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found with id: " + id));

        existingRoom.deactivateRoom();
        studyRoomRepository.saveAndFlush(existingRoom);

        return "Room deactivated successfully";
    }

    public String activateRoom(UUID id) {
        StudyRoom existingRoom = studyRoomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found with id: " + id));

        existingRoom.activateRoom();
        studyRoomRepository.saveAndFlush(existingRoom);

        return "Room activated successfully";
    }

    /* ================= DELETE / SOFT DELETE ================= */

    public String deleteRoom(UUID id) {
        StudyRoom existingRoom = studyRoomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found with id: " + id));

        List<Booking> roomBookings = bookingRepository.findByRoom_Id(id);
        List<Booking> activeBookings = bookingRepository.findByRoom_IdAndStatusInOrderByStartAtAsc(
                id,
                getAutoCancellableStatuses()
        );

        if (!activeBookings.isEmpty()) {
            autoCancelBookingsAndNotify(activeBookings);
        }

        /*
         * If any booking history exists, keep history safe and do soft delete only.
         * This matches your requirement:
         * - active bookings get cancelled automatically
         * - students get notification
         * - room disappears from future booking
         * - booking history remains intact
         */
        if (!roomBookings.isEmpty()) {
            existingRoom.softDelete();
            studyRoomRepository.saveAndFlush(existingRoom);

            return "Room deleted successfully. Active bookings were auto-cancelled, affected students were notified, and the room was soft-deleted to preserve booking history.";
        }

        /*
         * No booking history -> physical delete is safe.
         */
        List<StudyRoomImage> existingImages =
                studyRoomImageRepository.findByRoom_IdOrderByDisplayOrderAscCreatedAtAsc(id);

        List<String> imageUrls = new ArrayList<>();
        for (StudyRoomImage image : existingImages) {
            if (image.getImageUrl() != null && !image.getImageUrl().isBlank()) {
                imageUrls.add(image.getImageUrl());
            }
        }

        try {
            existingRoom.clearImages();
            studyRoomRepository.saveAndFlush(existingRoom);

            studyRoomRepository.delete(existingRoom);
            studyRoomRepository.flush();
        } catch (Exception e) {
            throw new RuntimeException(
                    "Room could not be deleted. It may still be linked to another table such as time slots, maintenance, or availability. Root error: "
                            + e.getMessage(),
                    e
            );
        }

        if (!imageUrls.isEmpty()) {
            try {
                fileStorageService.deleteFiles(imageUrls);
            } catch (Exception e) {
                System.err.println("Room deleted, but image files could not be removed: " + e.getMessage());
            }
        }

        return "Room deleted successfully";
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
    public List<StudyRoom> getFilteredRooms(String district, String location) {
        return studyRoomRepository.findByDistrictIgnoreCaseAndLocationContainingIgnoreCase(district, location);
    }

    @Transactional(readOnly = true)
    public List<StudyRoomResponse> getFilteredRoomResponses(String district, String location) {
        List<StudyRoom> rooms = studyRoomRepository.findByDistrictIgnoreCaseAndLocationContainingIgnoreCase(
                district,
                location
        );

        List<StudyRoomResponse> responseList = new ArrayList<>();
        for (StudyRoom room : rooms) {
            responseList.add(mapToResponse(room));
        }

        return responseList;
    }

    @Transactional(readOnly = true)
    public List<StudyRoom> getRoomsByLocationAndCapacity(String location, Integer seatingCapacity) {
        return studyRoomRepository.findByLocationContainingIgnoreCaseAndSeatingCapacityGreaterThanEqual(
                location, seatingCapacity
        );
    }

    @Transactional(readOnly = true)
    public List<StudyRoom> getRoomsByLocationAndFacilities(String location, String facilities) {
        return studyRoomRepository.findByLocationContainingIgnoreCaseAndFacilitiesContainingIgnoreCase(
                location, facilities
        );
    }

    @Transactional(readOnly = true)
    public List<StudyRoom> getRoomsByCapacityAndFacilities(Integer seatingCapacity, String facilities) {
        return studyRoomRepository.findBySeatingCapacityGreaterThanEqualAndFacilitiesContainingIgnoreCase(
                seatingCapacity, facilities
        );
    }

    @Transactional(readOnly = true)
    public List<StudyRoom> getRoomsByDistrictAndCapacity(String district, Integer seatingCapacity) {
        return studyRoomRepository.findByDistrictIgnoreCaseAndSeatingCapacityGreaterThanEqual(
                district, seatingCapacity
        );
    }

    @Transactional(readOnly = true)
    public List<StudyRoom> getRoomsByDistrictAndFacilities(String district, String facilities) {
        return studyRoomRepository.findByDistrictIgnoreCaseAndFacilitiesContainingIgnoreCase(
                district, facilities
        );
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

    /* ================= ROOM DELETE / BOOKING CANCEL HELPERS ================= */

    private List<BookingStatus> getAutoCancellableStatuses() {
        return List.of(
                BookingStatus.PENDING,
                BookingStatus.APPROVED
        );
    }

    private boolean isAutoCancellableBooking(Booking booking) {
        if (booking == null || booking.getStatus() == null) {
            return false;
        }

        return booking.getStatus() == BookingStatus.PENDING
                || booking.getStatus() == BookingStatus.APPROVED;
    }

    private void autoCancelBookingsAndNotify(List<Booking> activeBookings) {
        for (Booking booking : activeBookings) {
            booking.cancel("Cancelled automatically because the room was removed by admin.");
            bookingRepository.save(booking);
            notificationService.sendRoomDeletedCancellationNotification(booking);
        }
    }

    /* ================= HELPER METHODS ================= */

    private void validateDuplicateRoom(
            String district,
            String location,
            String blockName,
            String roomNumber,
            UUID currentRoomId
    ) {
        if (district == null || district.isBlank() ||
                location == null || location.isBlank() ||
                blockName == null || blockName.isBlank() ||
                roomNumber == null || roomNumber.isBlank()) {
            return;
        }

        Optional<StudyRoom> duplicateRoom =
                studyRoomRepository.findByDistrictIgnoreCaseAndLocationIgnoreCaseAndBlockNameIgnoreCaseAndRoomNumberIgnoreCase(
                        district, location, blockName, roomNumber
                );

        if (duplicateRoom.isPresent()) {
            if (currentRoomId == null || !duplicateRoom.get().getId().equals(currentRoomId)) {
                throw new RuntimeException(
                        "Another room already exists with district '" + district +
                                "', location '" + location +
                                "', block name '" + blockName +
                                "' and room number '" + roomNumber + "'"
                );
            }
        }
    }

    private void mapRequestToEntity(StudyRoomRequest request, StudyRoom studyRoom) {
        studyRoom.setBlockName(request.getBlockName());
        studyRoom.setRoomNumber(request.getRoomNumber());
        studyRoom.setFloorNumber(request.getFloorNumber());
        studyRoom.setSeatingCapacity(request.getSeatingCapacity());
        studyRoom.setFacilities(request.getFacilities());
        studyRoom.setDistrict(request.getDistrict());
        studyRoom.setLocation(request.getLocation());
        studyRoom.setFeePerHour(request.getFeePerHour());
        studyRoom.setApprovalRequired(Boolean.TRUE.equals(request.getApprovalRequired()));

        if (studyRoom.getIsActive() == null) {
            studyRoom.setIsActive(true);
        }

        if (studyRoom.getIsDeleted() == null) {
            studyRoom.setIsDeleted(false);
        }
    }

    private boolean hasValidFiles(MultipartFile[] files) {
        if (files == null || files.length == 0) {
            return false;
        }

        for (MultipartFile file : files) {
            if (file != null && !file.isEmpty()) {
                return true;
            }
        }

        return false;
    }

    private void replaceRoomImages(StudyRoom room, MultipartFile[] files) {
        if (!hasValidFiles(files)) {
            return;
        }

        UUID roomId = room.getId();
        if (roomId == null) {
            throw new RuntimeException("Room ID is required before saving images");
        }

        StudyRoom managedRoom = studyRoomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found with id: " + roomId));

        List<StudyRoomImage> oldImages =
                studyRoomImageRepository.findByRoom_IdOrderByDisplayOrderAscCreatedAtAsc(roomId);

        List<String> oldImageUrls = new ArrayList<>();
        for (StudyRoomImage image : oldImages) {
            if (image.getImageUrl() != null && !image.getImageUrl().isBlank()) {
                oldImageUrls.add(image.getImageUrl());
            }
        }

        managedRoom.clearImages();
        studyRoomRepository.saveAndFlush(managedRoom);

        List<String> imageUrls = fileStorageService.saveFiles(files);
        int displayOrder = 0;

        for (String imageUrl : imageUrls) {
            if (imageUrl == null || imageUrl.isBlank()) {
                continue;
            }

            StudyRoomImage image = new StudyRoomImage();
            image.setRoom(managedRoom);
            image.setImageUrl(imageUrl);
            image.setDisplayOrder(displayOrder++);
            image.setCreatedAt(OffsetDateTime.now());

            managedRoom.addImage(image);
        }

        studyRoomRepository.saveAndFlush(managedRoom);

        if (!oldImageUrls.isEmpty()) {
            fileStorageService.deleteFiles(oldImageUrls);
        }
    }

    private StudyRoomResponse mapToResponse(StudyRoom room) {
        StudyRoomResponse response = new StudyRoomResponse();
        response.setId(room.getId());
        response.setDisplayName(room.getDisplayName());
        response.setBlockName(room.getBlockName());
        response.setRoomNumber(room.getRoomNumber());
        response.setFloorNumber(room.getFloorNumber());
        response.setSeatingCapacity(room.getSeatingCapacity());
        response.setFacilities(room.getFacilities());
        response.setDistrict(room.getDistrict());
        response.setLocation(room.getLocation());
        response.setFeePerHour(room.getFeePerHour());
        response.setApprovalRequired(Boolean.TRUE.equals(room.getApprovalRequired()));
        response.setCreatedAt(room.getCreatedAt());
        response.setUpdatedAt(room.getUpdatedAt());

        List<StudyRoomImage> images =
                studyRoomImageRepository.findByRoom_IdOrderByDisplayOrderAscCreatedAtAsc(room.getId());

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
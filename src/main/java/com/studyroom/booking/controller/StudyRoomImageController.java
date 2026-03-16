package com.studyroom.booking.controller;

import com.studyroom.booking.model.StudyRoom;
import com.studyroom.booking.model.StudyRoomImage;
import com.studyroom.booking.repository.StudyRoomImageRepository;
import com.studyroom.booking.repository.StudyRoomRepository;
import com.studyroom.booking.service.FileStorageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/rooms")
@CrossOrigin(origins = "*")
public class StudyRoomImageController {

    private final StudyRoomRepository studyRoomRepository;
    private final StudyRoomImageRepository studyRoomImageRepository;
    private final FileStorageService fileStorageService;

    public StudyRoomImageController(StudyRoomRepository studyRoomRepository,
                                    StudyRoomImageRepository studyRoomImageRepository,
                                    FileStorageService fileStorageService) {
        this.studyRoomRepository = studyRoomRepository;
        this.studyRoomImageRepository = studyRoomImageRepository;
        this.fileStorageService = fileStorageService;
    }

    @PostMapping("/{roomId}/images")
    public ResponseEntity<?> uploadRoomImages(@PathVariable UUID roomId,
                                              @RequestParam("files") MultipartFile[] files) {
        try {
            StudyRoom room = studyRoomRepository.findById(roomId)
                    .orElseThrow(() -> new RuntimeException("Room not found with id: " + roomId));

            if (files == null || files.length == 0) {
                return ResponseEntity.badRequest().body("No files selected");
            }

            List<String> imageUrls = fileStorageService.saveFiles(files);
            List<StudyRoomImage> savedImages = new ArrayList<>();

            int startOrder = studyRoomImageRepository
                    .findByRoom_IdOrderByDisplayOrderAscCreatedAtAsc(roomId)
                    .size();

            for (int i = 0; i < imageUrls.size(); i++) {
                StudyRoomImage image = new StudyRoomImage();
                image.setRoom(room);
                image.setImageUrl(imageUrls.get(i));
                image.setDisplayOrder(startOrder + i);

                savedImages.add(studyRoomImageRepository.save(image));
            }

            return ResponseEntity.ok(savedImages);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to upload images: " + e.getMessage());
        }
    }

    @GetMapping("/{roomId}/images")
    public ResponseEntity<?> getRoomImages(@PathVariable UUID roomId) {
        try {
            if (!studyRoomRepository.existsById(roomId)) {
                return ResponseEntity.badRequest().body("Room not found with id: " + roomId);
            }

            List<StudyRoomImage> images =
                    studyRoomImageRepository.findByRoom_IdOrderByDisplayOrderAscCreatedAtAsc(roomId);

            return ResponseEntity.ok(images);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to fetch images: " + e.getMessage());
        }
    }

    @DeleteMapping("/images/{imageId}")
    public ResponseEntity<?> deleteRoomImage(@PathVariable UUID imageId) {
        try {
            StudyRoomImage image = studyRoomImageRepository.findById(imageId)
                    .orElseThrow(() -> new RuntimeException("Image not found with id: " + imageId));

            fileStorageService.deleteFile(image.getImageUrl());
            studyRoomImageRepository.delete(image);

            return ResponseEntity.ok("Image deleted successfully");

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to delete image: " + e.getMessage());
        }
    }

    @DeleteMapping("/{roomId}/images")
    public ResponseEntity<?> deleteAllRoomImages(@PathVariable UUID roomId) {
        try {
            List<StudyRoomImage> images =
                    studyRoomImageRepository.findByRoom_IdOrderByDisplayOrderAscCreatedAtAsc(roomId);

            for (StudyRoomImage image : images) {
                fileStorageService.deleteFile(image.getImageUrl());
            }

            studyRoomImageRepository.deleteByRoom_Id(roomId);

            return ResponseEntity.ok("All room images deleted successfully");

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to delete room images: " + e.getMessage());
        }
    }
}
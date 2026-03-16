package com.studyroom.booking.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    public String saveFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }

        try {
            Path uploadPath = Paths.get(uploadDir, "rooms").toAbsolutePath().normalize();

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String originalName = file.getOriginalFilename();
            String safeOriginalName = originalName == null ? "image" : Paths.get(originalName).getFileName().toString();

            String extension = "";
            int dotIndex = safeOriginalName.lastIndexOf(".");
            if (dotIndex >= 0) {
                extension = safeOriginalName.substring(dotIndex);
            }

            String fileName = UUID.randomUUID() + extension;
            Path targetPath = uploadPath.resolve(fileName).normalize();

            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            return "/uploads/rooms/" + fileName;

        } catch (IOException e) {
            throw new RuntimeException("Failed to store file: " + e.getMessage(), e);
        }
    }

    public List<String> saveFiles(MultipartFile[] files) {
        List<String> imageUrls = new ArrayList<>();

        if (files == null || files.length == 0) {
            return imageUrls;
        }

        for (MultipartFile file : files) {
            if (file != null && !file.isEmpty()) {
                imageUrls.add(saveFile(file));
            }
        }

        return imageUrls;
    }

    public void deleteFile(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return;
        }

        try {
            String normalizedUrl = imageUrl.trim();
            String fileName = normalizedUrl.substring(normalizedUrl.lastIndexOf("/") + 1);

            Path filePath = Paths.get(uploadDir, "rooms", fileName).toAbsolutePath().normalize();
            Files.deleteIfExists(filePath);

        } catch (IOException e) {
            throw new RuntimeException("Failed to delete file: " + e.getMessage(), e);
        }
    }

    public void deleteFiles(List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            return;
        }

        for (String imageUrl : imageUrls) {
            deleteFile(imageUrl);
        }
    }
}
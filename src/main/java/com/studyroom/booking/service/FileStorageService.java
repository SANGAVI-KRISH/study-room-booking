package com.studyroom.booking.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/jpg",
            "image/png",
            "image/webp"
    );

    public String saveFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }

        validateImage(file);

        try {
            Path uploadPath = getRoomUploadPath();
            Files.createDirectories(uploadPath);

            String originalName = file.getOriginalFilename();
            String safeOriginalName = originalName == null
                    ? "image"
                    : Paths.get(originalName).getFileName().toString();

            String extension = getExtension(safeOriginalName);
            String fileName = UUID.randomUUID() + extension;

            Path targetPath = uploadPath.resolve(fileName).normalize();

            if (!targetPath.startsWith(uploadPath)) {
                throw new RuntimeException("Invalid file path");
            }

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }

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
            String fileName = extractFileName(imageUrl);
            if (fileName == null || fileName.isBlank()) {
                return;
            }

            Path uploadPath = getRoomUploadPath();
            Path filePath = uploadPath.resolve(fileName).normalize();

            if (!filePath.startsWith(uploadPath)) {
                throw new RuntimeException("Invalid file path");
            }

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

    private Path getRoomUploadPath() {
        return Paths.get(uploadDir, "rooms").toAbsolutePath().normalize();
    }

    private void validateImage(MultipartFile file) {
        String contentType = file.getContentType();

        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new RuntimeException("Only JPG, JPEG, PNG, and WEBP images are allowed");
        }
    }

    private String getExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf(".");
        if (dotIndex >= 0 && dotIndex < fileName.length() - 1) {
            return fileName.substring(dotIndex).toLowerCase();
        }
        return "";
    }

    private String extractFileName(String imageUrl) {
        String normalizedUrl = imageUrl.trim();

        int queryIndex = normalizedUrl.indexOf("?");
        if (queryIndex >= 0) {
            normalizedUrl = normalizedUrl.substring(0, queryIndex);
        }

        int lastSlash = normalizedUrl.lastIndexOf("/");
        if (lastSlash >= 0 && lastSlash < normalizedUrl.length() - 1) {
            return normalizedUrl.substring(lastSlash + 1);
        }

        return normalizedUrl;
    }
}
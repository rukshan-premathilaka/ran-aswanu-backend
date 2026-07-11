package com.rukshan.ranaswanu.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${app.upload.dir}")
    private String uploadDir;

    private static final List<String> ALLOWED_TYPES = List.of(
            "image/jpeg", "image/png", "image/webp", "image/jpg"
    );

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    /**
     * Saves a file under a subfolder (e.g. "profile-pics", "product-images")
     * and returns the relative path to store in the database.
     */
    public String storeFile(MultipartFile file, String subFolder) {
        validateFile(file);

        try {
            Path targetDir = Paths.get(uploadDir, subFolder).toAbsolutePath().normalize();
            Files.createDirectories(targetDir);

            String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
            String extension = getExtension(originalFilename);
            String uniqueFilename = UUID.randomUUID() + extension;

            Path targetPath = targetDir.resolve(uniqueFilename);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            // Relative path stored in DB, served later via /files/**
            return subFolder + "/" + uniqueFilename;

        } catch (IOException e) {
            throw new RuntimeException("Failed to store file: " + originalFilenameSafe(file), e);
        }
    }

    public void deleteFile(String relativePath) {
        if (relativePath == null || relativePath.isBlank()) return;

        try {
            Path filePath = Paths.get(uploadDir, relativePath).toAbsolutePath().normalize();
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete file: " + relativePath, e);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File exceeds maximum size of 5MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("Only JPEG, PNG, and WEBP images are allowed");
        }
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        return filename.substring(filename.lastIndexOf("."));
    }

    private String originalFilenameSafe(MultipartFile file) {
        return file != null ? file.getOriginalFilename() : "unknown";
    }
}
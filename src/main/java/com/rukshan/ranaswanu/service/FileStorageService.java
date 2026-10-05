package com.rukshan.ranaswanu.service;

import com.rukshan.ranaswanu.entities.StoredFile;
import com.rukshan.ranaswanu.exception.FileStorageException;
import com.rukshan.ranaswanu.repository.StoredFileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

// Stores uploaded images in the database (table stored_files) instead of the disk, because
// Heroku's disk is erased on every restart/deploy. The returned relative path is still saved in
// the other tables and served at /files/<path> by FileController, so nothing else changes.
@Service
public class FileStorageService {

    @Autowired
    private StoredFileRepository storedFileRepository;

    // allowed content type -> file extension (the extension never comes from the client's file name)
    private static final Map<String, String> ALLOWED_TYPES = Map.of(
            "image/jpeg", ".jpg",
            "image/jpg", ".jpg",
            "image/png", ".png",
            "image/webp", ".webp"
    );

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    /**
     * Saves a file under a subfolder name (e.g. "profile-pics", "product-images")
     * and returns the relative path to store in the database.
     */
    public String storeFile(MultipartFile file, String subFolder) {
        validateFile(file);

        try {
            String contentType = file.getContentType().toLowerCase();
            String relativePath = subFolder + "/" + UUID.randomUUID() + ALLOWED_TYPES.get(contentType);
            storedFileRepository.save(new StoredFile(relativePath, contentType, file.getBytes()));
            return relativePath;
        } catch (IOException e) {
            throw new FileStorageException("We could not save the file. Please try again.", e);
        }
    }

    public void deleteFile(String relativePath) {
        if (relativePath == null || relativePath.isBlank()) return;
        storedFileRepository.deleteById(relativePath);
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File exceeds maximum size of 5MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.containsKey(contentType.toLowerCase())) {
            throw new IllegalArgumentException("Only JPEG, PNG, and WEBP images are allowed");
        }
    }
}

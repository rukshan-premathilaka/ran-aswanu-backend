package com.rukshan.ranaswanu.controller;

import com.rukshan.ranaswanu.repository.StoredFileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

// Public image endpoint: GET /files/profile-pics/<name>.jpg and /files/product-images/<name>.png
@RestController
public class FileController {

    @Autowired
    private StoredFileRepository storedFileRepository;

    @GetMapping("/files/{folder}/{filename:.+}")
    public ResponseEntity<byte[]> getFile(@PathVariable String folder, @PathVariable String filename) {
        return storedFileRepository.findById(folder + "/" + filename)
                .map(f -> ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(f.getContentType()))
                        .cacheControl(CacheControl.maxAge(30, TimeUnit.DAYS).cachePublic())
                        .body(f.getData()))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}

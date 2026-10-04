package com.rukshan.ranaswanu.service;

import com.rukshan.ranaswanu.exception.FileStorageException;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Stores uploaded images.
 *  - CLOUDINARY_URL set   -> images go to Cloudinary (use this on Heroku: its disk is wiped on every restart).
 *  - CLOUDINARY_URL empty -> images go to the local "uploads" folder (fine for your own computer).
 *
 * The value saved in the database is either "profile-pics/abc.jpg" (local) or a full https link (Cloudinary).
 * Always turn it into a public link with {@link #publicUrl(String)}.
 */
@Service
public class FileStorageService {

    private static final Logger log = LoggerFactory.getLogger(FileStorageService.class);

    @Value("${app.upload.dir}")
    private String uploadDir;

    // cloudinary://<api_key>:<api_secret>@<cloud_name>   (copy it from the Cloudinary dashboard)
    @Value("${app.cloudinary.url:}")
    private String cloudinaryUrl;

    private static final List<String> ALLOWED_TYPES = List.of(
            "image/jpeg", "image/png", "image/webp", "image/jpg"
    );

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final Pattern CLOUD_URL = Pattern.compile("^cloudinary://([^:]+):([^@]+)@(.+)$");
    private static final Pattern SECURE_URL = Pattern.compile("\"secure_url\"\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern PUBLIC_ID = Pattern.compile("/upload/(?:v\\d+/)?(.+?)\\.[A-Za-z0-9]+$");

    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

    private boolean cloudEnabled;
    private String apiKey;
    private String apiSecret;
    private String cloudName;

    @PostConstruct
    void init() {
        String value = cloudinaryUrl == null ? "" : cloudinaryUrl.trim();
        if (value.isEmpty()) {
            log.warn("CLOUDINARY_URL is not set -> images are saved on the local disk (they are lost on Heroku restarts).");
            return;
        }
        Matcher m = CLOUD_URL.matcher(value);
        if (!m.matches()) {
            throw new IllegalStateException("CLOUDINARY_URL must look like cloudinary://API_KEY:API_SECRET@CLOUD_NAME");
        }
        apiKey = m.group(1);
        apiSecret = m.group(2);
        cloudName = m.group(3);
        cloudEnabled = true;
        log.info("Image uploads use Cloudinary (cloud: {}).", cloudName);
    }

    /** Stored value -> link the browser can use. Full links stay as they are, local paths get /files/. */
    public static String publicUrl(String stored) {
        if (stored == null || stored.isBlank()) return null;
        if (stored.startsWith("http://") || stored.startsWith("https://")) return stored;
        return "/files/" + stored;
    }

    /**
     * Saves a file under a subfolder (e.g. "profile-pics", "product-images")
     * and returns the value to store in the database.
     */
    public String storeFile(MultipartFile file, String subFolder) {
        validateFile(file);
        return cloudEnabled ? storeInCloudinary(file, subFolder) : storeOnDisk(file, subFolder);
    }

    public void deleteFile(String stored) {
        if (stored == null || stored.isBlank()) return;
        if (stored.startsWith("http")) {
            deleteFromCloudinary(stored);
        } else {
            deleteFromDisk(stored);
        }
    }

    // ---------------- local disk ----------------

    private String storeOnDisk(MultipartFile file, String subFolder) {
        try {
            Path targetDir = Paths.get(uploadDir, subFolder).toAbsolutePath().normalize();
            Files.createDirectories(targetDir);

            String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
            String uniqueFilename = UUID.randomUUID() + getExtension(originalFilename);

            Files.copy(file.getInputStream(), targetDir.resolve(uniqueFilename), StandardCopyOption.REPLACE_EXISTING);

            // Relative path stored in DB, served later via /files/**
            return subFolder + "/" + uniqueFilename;
        } catch (IOException e) {
            throw new FileStorageException("We could not save the file. Please try again.", e);
        }
    }

    private void deleteFromDisk(String relativePath) {
        try {
            Path filePath = Paths.get(uploadDir, relativePath).toAbsolutePath().normalize();
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new FileStorageException("We could not replace the old file. Please try again.", e);
        }
    }

    // ---------------- Cloudinary (plain HTTPS, no extra library) ----------------

    private String storeInCloudinary(MultipartFile file, String subFolder) {
        try {
            String folder = "ranaswanu/" + subFolder;
            String publicId = UUID.randomUUID().toString();
            long timestamp = System.currentTimeMillis() / 1000;

            // Signature: the parameters (without file / api_key) sorted by name + the API secret, SHA-1.
            String signature = sha1("folder=" + folder + "&public_id=" + publicId + "&timestamp=" + timestamp + apiSecret);

            String boundary = "----ranaswanu" + UUID.randomUUID();
            ByteArrayOutputStream body = new ByteArrayOutputStream();
            writeField(body, boundary, "api_key", apiKey);
            writeField(body, boundary, "timestamp", String.valueOf(timestamp));
            writeField(body, boundary, "signature", signature);
            writeField(body, boundary, "folder", folder);
            writeField(body, boundary, "public_id", publicId);
            String contentType = file.getContentType() != null ? file.getContentType() : "application/octet-stream";
            write(body, "--" + boundary + "\r\nContent-Disposition: form-data; name=\"file\"; filename=\"upload\"\r\n"
                    + "Content-Type: " + contentType + "\r\n\r\n");
            body.write(file.getBytes());
            write(body, "\r\n--" + boundary + "--\r\n");

            HttpRequest request = HttpRequest.newBuilder(
                            URI.create("https://api.cloudinary.com/v1_1/" + cloudName + "/image/upload"))
                    .timeout(Duration.ofSeconds(30))
                    .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                    .POST(HttpRequest.BodyPublishers.ofByteArray(body.toByteArray()))
                    .build();

            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                log.error("Cloudinary upload failed: HTTP {} {}", response.statusCode(), response.body());
                throw new FileStorageException("We could not save the file. Please try again.",
                        new IOException("Cloudinary HTTP " + response.statusCode()));
            }
            Matcher m = SECURE_URL.matcher(response.body());
            if (!m.find()) {
                log.error("Cloudinary answer has no secure_url: {}", response.body());
                throw new FileStorageException("We could not save the file. Please try again.",
                        new IOException("No secure_url in Cloudinary answer"));
            }
            return m.group(1).replace("\\/", "/");

        } catch (IOException e) {
            throw new FileStorageException("We could not save the file. Please try again.", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new FileStorageException("We could not save the file. Please try again.", e);
        }
    }

    // A failed delete must never block the user (the old image just stays in Cloudinary), so it is only logged.
    private void deleteFromCloudinary(String url) {
        if (!cloudEnabled) return;
        Matcher m = PUBLIC_ID.matcher(url);
        if (!m.find()) return;
        try {
            String publicId = m.group(1);
            long timestamp = System.currentTimeMillis() / 1000;
            String signature = sha1("public_id=" + publicId + "&timestamp=" + timestamp + apiSecret);
            String form = "public_id=" + enc(publicId) + "&timestamp=" + timestamp
                    + "&api_key=" + enc(apiKey) + "&signature=" + signature;

            HttpRequest request = HttpRequest.newBuilder(
                            URI.create("https://api.cloudinary.com/v1_1/" + cloudName + "/image/destroy"))
                    .timeout(Duration.ofSeconds(15))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(form))
                    .build();
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                log.warn("Cloudinary delete failed: HTTP {} {}", response.statusCode(), response.body());
            }
        } catch (IOException e) {
            log.warn("Cloudinary delete failed", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // ---------------- helpers ----------------

    private static void write(ByteArrayOutputStream out, String text) throws IOException {
        out.write(text.getBytes(StandardCharsets.UTF_8));
    }

    private static void writeField(ByteArrayOutputStream out, String boundary, String name, String value) throws IOException {
        write(out, "--" + boundary + "\r\nContent-Disposition: form-data; name=\"" + name + "\"\r\n\r\n" + value + "\r\n");
    }

    private static String enc(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static String sha1(String text) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-1").digest(text.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : digest) hex.append(String.format("%02x", b));
            return hex.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
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
}

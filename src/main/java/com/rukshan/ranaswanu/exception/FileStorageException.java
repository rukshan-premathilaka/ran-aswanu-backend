package com.rukshan.ranaswanu.exception;

// Thrown when saving or deleting a file on disk fails -> 500 with a readable message
public class FileStorageException extends RuntimeException {
    public FileStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}

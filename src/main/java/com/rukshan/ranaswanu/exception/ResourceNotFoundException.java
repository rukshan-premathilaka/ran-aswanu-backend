package com.rukshan.ranaswanu.exception;

// Thrown when an item does not exist or belongs to someone else -> 404
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}

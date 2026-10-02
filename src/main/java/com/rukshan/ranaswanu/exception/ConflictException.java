package com.rukshan.ranaswanu.exception;

// Thrown for duplicates or "already done" -> 409
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}

package com.rukshan.ranaswanu.security;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.Instant;

// Writes a Shape B error for errors raised before a controller is reached (401 / 403)
public final class ApiErrorWriter {

    private ApiErrorWriter() {
    }

    public static void write(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        String safe = message.replace("\\", "\\\\").replace("\"", "\\\"");
        response.getWriter().write(
                "{\"timestamp\":\"" + Instant.now() + "\",\"status\":" + status + ",\"error\":\"" + safe + "\"}");
    }
}

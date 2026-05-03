package com.ai.knowRAG.dto;

import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    
    private int status;
    private String message;
    private String error;
    private String path;
    private LocalDateTime timestamp;
    private Object details;

    public ErrorResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public ErrorResponse(int status, String message, String error) {
        this();
        this.status = status;
        this.message = message;
        this.error = error;
    }

    public ErrorResponse(int status, String message, String error, String path) {
        this();
        this.status = status;
        this.message = message;
        this.error = error;
        this.path = path;
    }

    public ErrorResponse(int status, String message, String error, String path, Object details) {
        this();
        this.status = status;
        this.message = message;
        this.error = error;
        this.path = path;
        this.details = details;
    }

    // Getters and Setters
    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Object getDetails() {
        return details;
    }

    public void setDetails(Object details) {
        this.details = details;
    }
}

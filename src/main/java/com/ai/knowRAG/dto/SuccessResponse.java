package com.ai.knowRAG.dto;

import java.time.LocalDateTime;

public class SuccessResponse<T> {
    
    private int status;
    private String message;
    private T data;
    private LocalDateTime timestamp;

    public SuccessResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public SuccessResponse(int status, String message, T data) {
        this();
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public SuccessResponse(int status, String message) {
        this();
        this.status = status;
        this.message = message;
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

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}

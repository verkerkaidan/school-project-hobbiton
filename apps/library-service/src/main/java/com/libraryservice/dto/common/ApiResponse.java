package com.libraryservice.dto.common;

import java.time.LocalDateTime;

/**
 * Generic API-response wrapper used across the application.
 * <p>
 * Zorgt voor een consistente response structuur voor alle endpoints:
 * - success: geeft aan of de request geslaagd is
 * - message: extra informatie over de response
 * - totalResults: aantal resultaten (voor lijsten)
 * - data: de daadwerkelijke payload (generic type)
 * - timestamp: moment van response
 * <p>
 * Wordt gebruikt door controllers om uniforme responses terug te geven.
 */
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private int totalResults;
    private T data;
    private LocalDateTime timestamp;

    public ApiResponse() {
    }

    public ApiResponse(boolean success, String message, int totalResults, T data, LocalDateTime timestamp) {
        this.success = success;
        this.message = message;
        this.totalResults = totalResults;
        this.data = data;
        this.timestamp = timestamp;
    }

    /**
     * Factory method voor een succesvolle response.
     *
     * @param data    de payload
     * @param total   aantal resultaten
     * @param message optionele succesboodschap
     * @return ApiResponse met success = true
     */
    public static <T> ApiResponse<T> success(T data, int total, String message) {
        return new ApiResponse<>(true, message, total, data, LocalDateTime.now());
    }

    /**
     * Factory method voor een foutresponse.
     *
     * @param message foutmelding
     * @return ApiResponse met success = false en geen data
     */
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, 0, null, LocalDateTime.now());
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public int getTotalResults() {
        return totalResults;
    }

    public T getData() {
        return data;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setTotalResults(int totalResults) {
        this.totalResults = totalResults;
    }

    public void setData(T data) {
        this.data = data;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}

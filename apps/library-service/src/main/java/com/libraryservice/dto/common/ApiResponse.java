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

    public ApiResponse(boolean b, String message, int total, T data, LocalDateTime now) {
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

}

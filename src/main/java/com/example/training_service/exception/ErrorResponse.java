package com.example.training_service.exception;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Unified error response body")
public record ErrorResponse(
        @Schema(description = "Timestamp of the error") LocalDateTime timestamp,
        @Schema(description = "HTTP status code", example = "404") int status,
        @Schema(description = "Short error description", example = "Not Found") String error,
        @Schema(description = "Detailed error message") String message,
        @Schema(description = "Request path that triggered the error", example = "/trainings/abc") String path
) {
}

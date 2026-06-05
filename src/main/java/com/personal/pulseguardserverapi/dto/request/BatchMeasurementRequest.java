package com.personal.pulseguardserverapi.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class BatchMeasurementRequest {

    @NotNull(message = "deviceId is required")
    private UUID deviceId;

    @NotNull(message = "sessionId is required")
    private UUID sessionId;

    @NotEmpty(message = "measurements list cannot be empty")
    @Valid
    private List<MeasurementItemRequest> measurements;

    @Data
    public static class MeasurementItemRequest {

        @NotNull(message = "heartRate is required")
        @Min(value = 20, message = "heartRate must be at least 20 bpm")
        @Max(value = 250, message = "heartRate must be at most 250 bpm")
        private Integer heartRate;

        @NotNull(message = "spo2 is required")
        @DecimalMin(value = "0.0", message = "spo2 must be at least 0")
        @DecimalMax(value = "100.0", message = "spo2 must be at most 100")
        private Double spo2;

        @NotNull(message = "measuredAt is required")
        private LocalDateTime measuredAt;
    }
}

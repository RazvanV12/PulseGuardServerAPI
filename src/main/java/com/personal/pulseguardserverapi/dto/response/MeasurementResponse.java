package com.personal.pulseguardserverapi.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeasurementResponse {
    private Long measurementId;
    private UUID deviceId;
    private UUID sessionId;
    private Integer heartRate;
    private Double spo2;
    private LocalDateTime measuredAt;
}

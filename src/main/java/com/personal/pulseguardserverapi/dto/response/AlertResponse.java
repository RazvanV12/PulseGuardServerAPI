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
public class AlertResponse {
    private UUID alertId;
    private String type;
    private String severity;
    private boolean acknowledged;
    private LocalDateTime createdAt;
    private Long measurementId;
    private Integer heartRate;
    private Double spo2;
    private LocalDateTime measuredAt;
}

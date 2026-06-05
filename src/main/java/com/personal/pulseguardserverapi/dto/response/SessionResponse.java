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
public class SessionResponse {
    private UUID sessionId;
    private UUID userId;
    private UUID deviceId;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private Double avgHr;
    private Double avgSpo2;
}

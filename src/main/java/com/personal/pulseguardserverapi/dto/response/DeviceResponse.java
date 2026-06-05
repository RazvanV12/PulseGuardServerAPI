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
public class DeviceResponse {
    private UUID deviceId;
    private UUID userId;
    private String ipAddress;
    private String firmwareVersion;
    private String status;
    private LocalDateTime pairedAt;
}

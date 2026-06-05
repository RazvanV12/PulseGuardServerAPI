package com.personal.pulseguardserverapi.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class StartSessionRequest {

    @NotNull(message = "deviceId is required")
    private UUID deviceId;
}

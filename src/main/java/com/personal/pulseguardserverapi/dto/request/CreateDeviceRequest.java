package com.personal.pulseguardserverapi.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateDeviceRequest {

    @NotBlank(message = "IP address is required")
    private String ipAddress;

    private String firmwareVersion;

    // Accepted values: ACTIVE, INACTIVE, PAIRED. Defaults to ACTIVE if blank.
    private String status = "ACTIVE";
}

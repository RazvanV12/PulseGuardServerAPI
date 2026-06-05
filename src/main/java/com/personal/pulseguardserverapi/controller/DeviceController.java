package com.personal.pulseguardserverapi.controller;

import com.personal.pulseguardserverapi.dto.request.CreateDeviceRequest;
import com.personal.pulseguardserverapi.dto.response.DeviceResponse;
import com.personal.pulseguardserverapi.service.DeviceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/devices")
@RequiredArgsConstructor
@Tag(name = "Devices", description = "Device pairing and listing")
@SecurityRequirement(name = "Bearer Authentication")
public class DeviceController {

    private final DeviceService deviceService;

    @PostMapping
    @Operation(summary = "Pair / register a new Arduino device")
    public ResponseEntity<DeviceResponse> registerDevice(
            Authentication authentication,
            @Valid @RequestBody CreateDeviceRequest request) {
        UUID userId = UUID.fromString(authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(deviceService.registerDevice(userId, request));
    }

    @GetMapping
    @Operation(summary = "List all devices for the current user")
    public ResponseEntity<List<DeviceResponse>> getDevices(Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        return ResponseEntity.ok(deviceService.getUserDevices(userId));
    }
}

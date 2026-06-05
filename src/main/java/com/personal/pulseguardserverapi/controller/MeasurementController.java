package com.personal.pulseguardserverapi.controller;

import com.personal.pulseguardserverapi.dto.request.BatchMeasurementRequest;
import com.personal.pulseguardserverapi.dto.response.BatchMeasurementResponse;
import com.personal.pulseguardserverapi.dto.response.MeasurementResponse;
import com.personal.pulseguardserverapi.service.MeasurementService;
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
@RequestMapping("/v1/measurements")
@RequiredArgsConstructor
@Tag(name = "Measurements", description = "Sensor data from Arduino and Flutter history")
@SecurityRequirement(name = "Bearer Authentication")
public class MeasurementController {

    private final MeasurementService measurementService;

    @GetMapping("/latest")
    @Operation(summary = "Get the single most recent measurement (used by Flutter live monitoring)")
    public ResponseEntity<MeasurementResponse> getLatestMeasurement(Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        return measurementService.getLatestMeasurement(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(summary = "Get all measurements for the current user (newest first)")
    public ResponseEntity<List<MeasurementResponse>> getMeasurements(Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        return ResponseEntity.ok(measurementService.getUserMeasurements(userId));
    }

    @PostMapping
    @Operation(summary = "Post a batch of measurements (called by Arduino or Python simulator)")
    public ResponseEntity<BatchMeasurementResponse> saveMeasurements(
            Authentication authentication,
            @Valid @RequestBody BatchMeasurementRequest request) {
        UUID userId = UUID.fromString(authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(measurementService.saveBatch(userId, request));
    }
}

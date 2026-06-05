package com.personal.pulseguardserverapi.controller;

import com.personal.pulseguardserverapi.dto.response.HealthResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/v1/health")
@Tag(name = "Health", description = "Backend health check — no token required")
public class HealthController {

    @GetMapping
    @Operation(summary = "Check if the backend is running")
    public ResponseEntity<HealthResponse> health() {
        return ResponseEntity.ok(HealthResponse.builder()
                .status("UP")
                .timestamp(LocalDateTime.now())
                .version("1.0.0")
                .build());
    }
}

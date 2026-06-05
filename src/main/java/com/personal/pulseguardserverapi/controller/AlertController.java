package com.personal.pulseguardserverapi.controller;

import com.personal.pulseguardserverapi.dto.response.AlertResponse;
import com.personal.pulseguardserverapi.service.AlertService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/alerts")
@RequiredArgsConstructor
@Tag(name = "Alerts", description = "Vital-sign alerts")
@SecurityRequirement(name = "Bearer Authentication")
public class AlertController {

    private final AlertService alertService;

    @GetMapping
    @Operation(summary = "Get alerts for the current user (optional filter: ?acknowledged=true/false)")
    public ResponseEntity<List<AlertResponse>> getAlerts(
            Authentication authentication,
            @RequestParam(required = false) Boolean acknowledged) {
        UUID userId = UUID.fromString(authentication.getName());
        return ResponseEntity.ok(alertService.getAlerts(userId, acknowledged));
    }

    @PatchMapping("/{alertId}/acknowledge")
    @Operation(summary = "Mark an alert as acknowledged")
    public ResponseEntity<AlertResponse> acknowledgeAlert(
            Authentication authentication,
            @PathVariable UUID alertId) {
        UUID userId = UUID.fromString(authentication.getName());
        return ResponseEntity.ok(alertService.acknowledgeAlert(userId, alertId));
    }
}

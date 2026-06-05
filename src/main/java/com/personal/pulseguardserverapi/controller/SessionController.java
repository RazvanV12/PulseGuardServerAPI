package com.personal.pulseguardserverapi.controller;

import com.personal.pulseguardserverapi.dto.request.StartSessionRequest;
import com.personal.pulseguardserverapi.dto.response.SessionResponse;
import com.personal.pulseguardserverapi.service.SessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/sessions")
@RequiredArgsConstructor
@Tag(name = "Sessions", description = "Monitoring sessions")
@SecurityRequirement(name = "Bearer Authentication")
public class SessionController {

    private final SessionService sessionService;

    @PostMapping
    @Operation(summary = "Start a new monitoring session")
    public ResponseEntity<SessionResponse> startSession(
            Authentication authentication,
            @Valid @RequestBody StartSessionRequest request) {
        UUID userId = UUID.fromString(authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(sessionService.startSession(userId, request));
    }

    @PostMapping("/{sessionId}/end")
    @Operation(summary = "End a monitoring session and calculate averages")
    public ResponseEntity<SessionResponse> endSession(
            Authentication authentication,
            @PathVariable UUID sessionId) {
        UUID userId = UUID.fromString(authentication.getName());
        return ResponseEntity.ok(sessionService.endSession(userId, sessionId));
    }
}

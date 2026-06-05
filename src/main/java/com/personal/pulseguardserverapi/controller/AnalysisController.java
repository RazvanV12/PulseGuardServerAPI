package com.personal.pulseguardserverapi.controller;

import com.personal.pulseguardserverapi.dto.response.AnalysisSummaryResponse;
import com.personal.pulseguardserverapi.service.AnalysisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/v1/analysis")
@RequiredArgsConstructor
@Tag(name = "Analysis", description = "Health summary statistics")
@SecurityRequirement(name = "Bearer Authentication")
public class AnalysisController {

    private final AnalysisService analysisService;

    @GetMapping("/summary")
    @Operation(summary = "Get a health summary for the current user")
    public ResponseEntity<AnalysisSummaryResponse> getSummary(Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        return ResponseEntity.ok(analysisService.getSummary(userId));
    }
}

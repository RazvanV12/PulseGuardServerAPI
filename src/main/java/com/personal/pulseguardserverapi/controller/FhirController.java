package com.personal.pulseguardserverapi.controller;

import com.personal.pulseguardserverapi.dto.response.FhirObservationResponse;
import com.personal.pulseguardserverapi.service.FhirService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/fhir")
@RequiredArgsConstructor
@Tag(name = "FHIR", description = "Simplified HL7 FHIR R4 Observation resources")
@SecurityRequirement(name = "Bearer Authentication")
public class FhirController {

    private final FhirService fhirService;

    @GetMapping("/observations")
    @Operation(summary = "Get measurements as FHIR Observations (LOINC coded, UCUM units)")
    public ResponseEntity<List<FhirObservationResponse>> getObservations(
            Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        return ResponseEntity.ok(fhirService.getObservations(userId));
    }
}

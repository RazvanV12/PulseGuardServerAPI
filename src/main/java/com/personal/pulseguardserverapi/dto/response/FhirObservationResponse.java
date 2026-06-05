package com.personal.pulseguardserverapi.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Simplified HL7 FHIR R4 Observation resource.
 * Uses LOINC codes: 8867-4 (Heart Rate), 2708-6 (SpO2).
 * Uses UCUM units: /min (heart rate), % (SpO2).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FhirObservationResponse {

    private String resourceType;
    private String status;
    private FhirCode code;
    private FhirReference subject;
    private FhirReference device;
    private String effectiveDateTime;
    private FhirValueQuantity valueQuantity;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FhirCode {
        private List<FhirCoding> coding;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FhirCoding {
        private String system;
        private String code;
        private String display;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FhirReference {
        private String reference;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FhirValueQuantity {
        private Number value;
        private String unit;
        private String system;
        private String code;
    }
}

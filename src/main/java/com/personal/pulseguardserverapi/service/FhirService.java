package com.personal.pulseguardserverapi.service;

import com.personal.pulseguardserverapi.dto.response.FhirObservationResponse;
import com.personal.pulseguardserverapi.dto.response.FhirObservationResponse.*;
import com.personal.pulseguardserverapi.entity.Measurement;
import com.personal.pulseguardserverapi.entity.User;
import com.personal.pulseguardserverapi.exception.ResourceNotFoundException;
import com.personal.pulseguardserverapi.repository.MeasurementRepository;
import com.personal.pulseguardserverapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FhirService {

    private final MeasurementRepository measurementRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<FhirObservationResponse> getObservations(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<Measurement> measurements = measurementRepository.findByUserOrderByMeasuredAtDesc(user);
        List<FhirObservationResponse> result = new ArrayList<>();

        for (Measurement m : measurements) {
            result.add(buildHeartRateObservation(m));
            result.add(buildSpo2Observation(m));
        }

        return result;
    }

    // LOINC 8867-4 = Heart Rate, UCUM unit /min
    private FhirObservationResponse buildHeartRateObservation(Measurement m) {
        return FhirObservationResponse.builder()
                .resourceType("Observation")
                .status("final")
                .code(FhirCode.builder()
                        .coding(List.of(FhirCoding.builder()
                                .system("http://loinc.org")
                                .code("8867-4")
                                .display("Heart rate")
                                .build()))
                        .build())
                .subject(FhirReference.builder()
                        .reference("Patient/" + m.getUser().getUserId())
                        .build())
                .device(FhirReference.builder()
                        .reference("Device/" + m.getDevice().getDeviceId())
                        .build())
                .effectiveDateTime(m.getMeasuredAt().toString())
                .valueQuantity(FhirValueQuantity.builder()
                        .value(m.getHeartRate())
                        .unit("/min")
                        .system("http://unitsofmeasure.org")
                        .code("/min")
                        .build())
                .build();
    }

    // LOINC 2708-6 = Oxygen saturation, UCUM unit %
    private FhirObservationResponse buildSpo2Observation(Measurement m) {
        return FhirObservationResponse.builder()
                .resourceType("Observation")
                .status("final")
                .code(FhirCode.builder()
                        .coding(List.of(FhirCoding.builder()
                                .system("http://loinc.org")
                                .code("2708-6")
                                .display("Oxygen saturation")
                                .build()))
                        .build())
                .subject(FhirReference.builder()
                        .reference("Patient/" + m.getUser().getUserId())
                        .build())
                .device(FhirReference.builder()
                        .reference("Device/" + m.getDevice().getDeviceId())
                        .build())
                .effectiveDateTime(m.getMeasuredAt().toString())
                .valueQuantity(FhirValueQuantity.builder()
                        .value(m.getSpo2())
                        .unit("%")
                        .system("http://unitsofmeasure.org")
                        .code("%")
                        .build())
                .build();
    }
}

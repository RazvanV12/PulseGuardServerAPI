package com.personal.pulseguardserverapi.service;

import com.personal.pulseguardserverapi.dto.response.AnalysisSummaryResponse;
import com.personal.pulseguardserverapi.entity.User;
import com.personal.pulseguardserverapi.exception.ResourceNotFoundException;
import com.personal.pulseguardserverapi.repository.AlertRepository;
import com.personal.pulseguardserverapi.repository.MeasurementRepository;
import com.personal.pulseguardserverapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AnalysisService {

    private final MeasurementRepository measurementRepository;
    private final AlertRepository alertRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public AnalysisSummaryResponse getSummary(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        long measurementCount = measurementRepository.countByUser(user);
        Double avgHr   = measurementRepository.avgHeartRateByUser(user);
        Double avgSpo2 = measurementRepository.avgSpo2ByUser(user);
        Integer minHr  = measurementRepository.minHeartRateByUser(user);
        Integer maxHr  = measurementRepository.maxHeartRateByUser(user);
        Double minSpo2 = measurementRepository.minSpo2ByUser(user);
        Double maxSpo2 = measurementRepository.maxSpo2ByUser(user);
        long alertCount = alertRepository.countByUser(user);

        LocalDateTime lastMeasuredAt = measurementRepository
                .findFirstByUserOrderByMeasuredAtDesc(user)
                .map(m -> m.getMeasuredAt())
                .orElse(null);

        return AnalysisSummaryResponse.builder()
                .measurementCount(measurementCount)
                .avgHeartRate(round1(avgHr))
                .avgSpo2(round1(avgSpo2))
                .minHeartRate(minHr)
                .maxHeartRate(maxHr)
                .minSpo2(minSpo2)
                .maxSpo2(maxSpo2)
                .alertCount(alertCount)
                .lastMeasuredAt(lastMeasuredAt)
                .build();
    }

    private Double round1(Double value) {
        if (value == null) return null;
        return Math.round(value * 10.0) / 10.0;
    }
}

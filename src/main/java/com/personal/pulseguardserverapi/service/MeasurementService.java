package com.personal.pulseguardserverapi.service;

import com.personal.pulseguardserverapi.dto.request.BatchMeasurementRequest;
import com.personal.pulseguardserverapi.dto.response.BatchMeasurementResponse;
import com.personal.pulseguardserverapi.dto.response.MeasurementResponse;
import com.personal.pulseguardserverapi.entity.*;
import com.personal.pulseguardserverapi.exception.ResourceNotFoundException;
import com.personal.pulseguardserverapi.exception.UnauthorizedException;
import com.personal.pulseguardserverapi.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MeasurementService {

    private final MeasurementRepository measurementRepository;
    private final AlertRepository alertRepository;
    private final UserRepository userRepository;
    private final DeviceRepository deviceRepository;
    private final SessionRepository sessionRepository;

    @Transactional(readOnly = true)
    public Optional<MeasurementResponse> getLatestMeasurement(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return measurementRepository.findFirstByUserOrderByMeasuredAtDesc(user)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public List<MeasurementResponse> getUserMeasurements(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return measurementRepository.findByUserOrderByMeasuredAtDesc(user)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public BatchMeasurementResponse saveBatch(UUID userId, BatchMeasurementRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Device device = deviceRepository.findById(request.getDeviceId())
                .orElseThrow(() -> new ResourceNotFoundException("Device not found"));
        Session session = sessionRepository.findById(request.getSessionId())
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));

        if (!device.getUser().getUserId().equals(userId)) {
            throw new UnauthorizedException("Device does not belong to this user");
        }

        int savedCount = 0;
        int alertCount = 0;

        for (BatchMeasurementRequest.MeasurementItemRequest item : request.getMeasurements()) {
            Measurement measurement = Measurement.builder()
                    .user(user)
                    .device(device)
                    .session(session)
                    .heartRate(item.getHeartRate())
                    .spo2(item.getSpo2())
                    .measuredAt(item.getMeasuredAt())
                    .build();

            measurement = measurementRepository.save(measurement);
            savedCount++;

            List<Alert> alerts = generateAlerts(user, measurement);
            if (!alerts.isEmpty()) {
                alertRepository.saveAll(alerts);
                alertCount += alerts.size();
            }
        }

        return BatchMeasurementResponse.builder()
                .savedCount(savedCount)
                .alertCount(alertCount)
                .message(String.format("Saved %d measurement(s), generated %d alert(s)",
                        savedCount, alertCount))
                .build();
    }

    private MeasurementResponse mapToResponse(Measurement m) {
        return MeasurementResponse.builder()
                .measurementId(m.getMeasurementId())
                .deviceId(m.getDevice().getDeviceId())
                .sessionId(m.getSession() != null ? m.getSession().getSessionId() : null)
                .heartRate(m.getHeartRate())
                .spo2(m.getSpo2())
                .measuredAt(m.getMeasuredAt())
                .build();
    }

    // ── Alert thresholds ──────────────────────────────────────────────────────
    // LOW_SPO2  → SpO2 < 92 %       (clinical hypoxemia threshold)
    // HIGH_HR   → Heart rate > 120  (tachycardia boundary)
    // LOW_HR    → Heart rate < 50   (bradycardia boundary)
    private List<Alert> generateAlerts(User user, Measurement measurement) {
        List<Alert> alerts = new ArrayList<>();

        if (measurement.getSpo2() < 92) {
            alerts.add(Alert.builder()
                    .user(user).measurement(measurement)
                    .type(AlertType.LOW_SPO2).severity(AlertSeverity.HIGH)
                    .acknowledged(false).build());
        }
        if (measurement.getHeartRate() > 120) {
            alerts.add(Alert.builder()
                    .user(user).measurement(measurement)
                    .type(AlertType.HIGH_HR).severity(AlertSeverity.MEDIUM)
                    .acknowledged(false).build());
        }
        if (measurement.getHeartRate() < 50) {
            alerts.add(Alert.builder()
                    .user(user).measurement(measurement)
                    .type(AlertType.LOW_HR).severity(AlertSeverity.MEDIUM)
                    .acknowledged(false).build());
        }

        return alerts;
    }
}

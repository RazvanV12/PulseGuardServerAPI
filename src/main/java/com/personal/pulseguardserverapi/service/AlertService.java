package com.personal.pulseguardserverapi.service;

import com.personal.pulseguardserverapi.dto.response.AlertResponse;
import com.personal.pulseguardserverapi.entity.Alert;
import com.personal.pulseguardserverapi.entity.Measurement;
import com.personal.pulseguardserverapi.entity.User;
import com.personal.pulseguardserverapi.exception.ResourceNotFoundException;
import com.personal.pulseguardserverapi.exception.UnauthorizedException;
import com.personal.pulseguardserverapi.repository.AlertRepository;
import com.personal.pulseguardserverapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AlertService {

    private final AlertRepository alertRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<AlertResponse> getAlerts(UUID userId, Boolean acknowledged) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<Alert> alerts = (acknowledged != null)
                ? alertRepository.findByUserAndAcknowledgedOrderByCreatedAtDesc(user, acknowledged)
                : alertRepository.findByUserOrderByCreatedAtDesc(user);

        return alerts.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional
    public AlertResponse acknowledgeAlert(UUID userId, UUID alertId) {
        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found"));

        if (!alert.getUser().getUserId().equals(userId)) {
            throw new UnauthorizedException("Alert does not belong to this user");
        }

        alert.setAcknowledged(true);
        return mapToResponse(alertRepository.save(alert));
    }

    private AlertResponse mapToResponse(Alert alert) {
        AlertResponse.AlertResponseBuilder builder = AlertResponse.builder()
                .alertId(alert.getAlertId())
                .type(alert.getType().name())
                .severity(alert.getSeverity().name())
                .acknowledged(alert.isAcknowledged())
                .createdAt(alert.getCreatedAt());

        Measurement m = alert.getMeasurement();
        if (m != null) {
            builder.measurementId(m.getMeasurementId())
                    .heartRate(m.getHeartRate())
                    .spo2(m.getSpo2())
                    .measuredAt(m.getMeasuredAt());
        }

        return builder.build();
    }
}

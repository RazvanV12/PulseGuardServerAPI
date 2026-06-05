package com.personal.pulseguardserverapi.service;

import com.personal.pulseguardserverapi.dto.request.StartSessionRequest;
import com.personal.pulseguardserverapi.dto.response.SessionResponse;
import com.personal.pulseguardserverapi.entity.Device;
import com.personal.pulseguardserverapi.entity.Measurement;
import com.personal.pulseguardserverapi.entity.Session;
import com.personal.pulseguardserverapi.entity.User;
import com.personal.pulseguardserverapi.exception.ResourceNotFoundException;
import com.personal.pulseguardserverapi.exception.UnauthorizedException;
import com.personal.pulseguardserverapi.repository.DeviceRepository;
import com.personal.pulseguardserverapi.repository.MeasurementRepository;
import com.personal.pulseguardserverapi.repository.SessionRepository;
import com.personal.pulseguardserverapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;
    private final DeviceRepository deviceRepository;
    private final MeasurementRepository measurementRepository;

    @Transactional
    public SessionResponse startSession(UUID userId, StartSessionRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Device device = deviceRepository.findById(request.getDeviceId())
                .orElseThrow(() -> new ResourceNotFoundException("Device not found"));

        if (!device.getUser().getUserId().equals(userId)) {
            throw new UnauthorizedException("Device does not belong to this user");
        }

        Session session = Session.builder()
                .user(user)
                .device(device)
                .startedAt(LocalDateTime.now())
                .build();

        return mapToResponse(sessionRepository.save(session));
    }

    @Transactional
    public SessionResponse endSession(UUID userId, UUID sessionId) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));

        if (!session.getUser().getUserId().equals(userId)) {
            throw new UnauthorizedException("Session does not belong to this user");
        }

        session.setEndedAt(LocalDateTime.now());

        // Calculate averages from all measurements in this session.
        List<Measurement> measurements = measurementRepository.findBySession(session);
        if (!measurements.isEmpty()) {
            double avgHr = measurements.stream()
                    .mapToInt(Measurement::getHeartRate)
                    .average()
                    .orElse(0);
            double avgSpo2 = measurements.stream()
                    .mapToDouble(Measurement::getSpo2)
                    .average()
                    .orElse(0);
            session.setAvgHr(Math.round(avgHr * 10.0) / 10.0);
            session.setAvgSpo2(Math.round(avgSpo2 * 10.0) / 10.0);
        }

        return mapToResponse(sessionRepository.save(session));
    }

    private SessionResponse mapToResponse(Session session) {
        return SessionResponse.builder()
                .sessionId(session.getSessionId())
                .userId(session.getUser().getUserId())
                .deviceId(session.getDevice().getDeviceId())
                .startedAt(session.getStartedAt())
                .endedAt(session.getEndedAt())
                .avgHr(session.getAvgHr())
                .avgSpo2(session.getAvgSpo2())
                .build();
    }
}

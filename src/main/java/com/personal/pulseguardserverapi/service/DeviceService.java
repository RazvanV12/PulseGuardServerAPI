package com.personal.pulseguardserverapi.service;

import com.personal.pulseguardserverapi.dto.request.CreateDeviceRequest;
import com.personal.pulseguardserverapi.dto.response.DeviceResponse;
import com.personal.pulseguardserverapi.entity.Device;
import com.personal.pulseguardserverapi.entity.DeviceStatus;
import com.personal.pulseguardserverapi.entity.User;
import com.personal.pulseguardserverapi.exception.ResourceNotFoundException;
import com.personal.pulseguardserverapi.repository.DeviceRepository;
import com.personal.pulseguardserverapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final UserRepository userRepository;

    @Transactional
    public DeviceResponse registerDevice(UUID userId, CreateDeviceRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        DeviceStatus status;
        try {
            status = DeviceStatus.valueOf(request.getStatus().toUpperCase());
        } catch (IllegalArgumentException e) {
            status = DeviceStatus.ACTIVE;
        }

        Device device = Device.builder()
                .user(user)
                .ipAddress(request.getIpAddress())
                .firmwareVersion(request.getFirmwareVersion())
                .status(status)
                .build();

        return mapToResponse(deviceRepository.save(device));
    }

    @Transactional(readOnly = true)
    public List<DeviceResponse> getUserDevices(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return deviceRepository.findByUser(user).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private DeviceResponse mapToResponse(Device device) {
        return DeviceResponse.builder()
                .deviceId(device.getDeviceId())
                .userId(device.getUser().getUserId())
                .ipAddress(device.getIpAddress())
                .firmwareVersion(device.getFirmwareVersion())
                .status(device.getStatus().name())
                .pairedAt(device.getPairedAt())
                .build();
    }
}

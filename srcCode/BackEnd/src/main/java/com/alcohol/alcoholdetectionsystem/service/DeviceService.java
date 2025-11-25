package com.alcohol.alcoholdetectionsystem.service;

import com.alcohol.alcoholdetectionsystem.dto.request.DeviceRegisterRequest;
import com.alcohol.alcoholdetectionsystem.dto.response.*;
import com.alcohol.alcoholdetectionsystem.entity.DeviceEntity;
import com.alcohol.alcoholdetectionsystem.entity.UserEntity;
import com.alcohol.alcoholdetectionsystem.enums.DeviceStatus;
import com.alcohol.alcoholdetectionsystem.repository.DeviceRepository;
import com.alcohol.alcoholdetectionsystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class DeviceService {
    private final DeviceRepository deviceRepository;
    private final UserRepository userRepository;

    private DeviceResponse toDeviceResponse(DeviceEntity entity) {
        return DeviceResponse.builder()
                .deviceId(entity.getDeviceId())
                .name(entity.getName())
                .model(entity.getModel())
                .status(entity.getStatus())
                .lastCalibration(entity.getLastCalibration())
                .nextCalibration(entity.getNextCalibration())
                .registeredBy(entity.getRegisteredBy().getId())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public DeviceResponse registerDevice(DeviceRegisterRequest request, Authentication authentication) {
        if (deviceRepository.existsByDeviceId(request.getDeviceId())) {
            throw new IllegalArgumentException("Device ID already registered");
        }

        UserEntity userEntity = userRepository.findByUsername(authentication.getName()).orElseThrow(() -> new IllegalArgumentException("Username not found"));
        DeviceEntity deviceEntity = DeviceEntity.builder()
                .deviceId(request.getDeviceId())
                .name(request.getName())
                .model(request.getModel())
                .registeredBy(userEntity)
                .status(DeviceStatus.ACTIVE)
                .lastCalibration(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .nextCalibration(LocalDateTime.now().plusDays(180))
                .build();

        deviceRepository.save(deviceEntity);
        return toDeviceResponse(deviceEntity);
    }

    public DeviceListResponse getAllDevices(String status, String search, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());

        Page<DeviceEntity> devicePage;

        boolean hasStatus = status != null && !status.isEmpty();
        boolean hasSearch = search != null && !search.isEmpty();

        if (hasStatus && hasSearch) {
            DeviceStatus deviceStatus = DeviceStatus.valueOf(status);
            devicePage = deviceRepository.findByStatusAndNameContainingIgnoreCaseOrDeviceIdContainingIgnoreCase(deviceStatus, search, search, pageable);
        } else if (hasStatus) {
            DeviceStatus deviceStatus = DeviceStatus.valueOf(status);
            devicePage = deviceRepository.findByStatus(deviceStatus, pageable);
        } else if (hasSearch) {
            devicePage = deviceRepository.findByNameContainingIgnoreCaseOrDeviceIdContainingIgnoreCase(search, search, pageable);
        } else {
            devicePage = deviceRepository.findAll(pageable);
        }

        List<DeviceResponse> devices = devicePage.getContent().stream()
                .map(this::toDeviceResponse)
                .toList();

        return DeviceListResponse.builder()
                .devices(devices)
                .page(page)
                .size(size)
                .total(devicePage.getTotalElements())
                .totalPages(devicePage.getTotalPages())
                .build();
    }


    public void deleteDevice(String deviceId) {
        DeviceEntity deviceEntity = deviceRepository.findByDeviceId(deviceId)
                .orElseThrow(() -> new IllegalArgumentException("Device not found"));
        deviceRepository.delete(deviceEntity);
    }

    public DeviceResponse getDeviceById(String deviceId) {
        DeviceEntity device = deviceRepository.findByDeviceId(deviceId).orElseThrow(() -> new IllegalArgumentException("Device not found"));
        return toDeviceResponse(device);
    }

    public DeviceCheckResponse checkDevice(String deviceId) {
        DeviceEntity deviceEntity = deviceRepository.findByDeviceId(deviceId).orElseThrow(() -> new IllegalArgumentException("Device not found"));
        return DeviceCheckResponse.builder()
                .deviceId(deviceEntity.getDeviceId())
                .deviceName(deviceEntity.getName())
                .exists(true)
                .status(deviceEntity.getStatus())
                .lastCalibration(deviceEntity.getLastCalibration())
                .nextCalibration(deviceEntity.getNextCalibration())
                .build();
    }

    public DeviceResponse updateDeviceStatus(String deviceId, String status) {
        DeviceEntity device = deviceRepository.findByDeviceId(deviceId)
                .orElseThrow(() -> new IllegalArgumentException("Device not found"));

        try {
            DeviceStatus newStatus = DeviceStatus.valueOf(status.toUpperCase());
            device.setStatus(newStatus);
            deviceRepository.save(device);
            return toDeviceResponse(device);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid device status: " + status);
        }
    }

    public DeviceResponse updateCalibration(String deviceId) {
        DeviceEntity device = deviceRepository.findByDeviceId(deviceId)
                .orElseThrow(() -> new IllegalArgumentException("Device not found"));

        device.setLastCalibration(LocalDateTime.now());
        device.setNextCalibration(LocalDateTime.now().plusDays(180));
        deviceRepository.save(device);
        return toDeviceResponse(device);
    }

    public List<DeviceCalibrationResponse> getDevicesNeedCalibration() {
        LocalDateTime now = LocalDateTime.now();
        List<DeviceEntity> devices = deviceRepository.findByNextCalibrationBefore(now);

        return devices.stream()
                .map(device -> {
                    long daysOverdue = ChronoUnit.DAYS.between(device.getNextCalibration(), now);
                    return DeviceCalibrationResponse.builder()
                            .deviceId(device.getDeviceId())
                            .name(device.getName())
                            .lastCalibration(device.getLastCalibration())
                            .nextCalibration(device.getNextCalibration())
                            .daysOverdue(daysOverdue)
                            .build();
                })
                .collect(Collectors.toList());
    }

    public DeviceStatisticsResponse getDeviceStatistics() {
        long totalDevices = deviceRepository.count();
        long activeDevices = deviceRepository.countByStatus(DeviceStatus.ACTIVE);
        long maintenanceDevices = deviceRepository.countByStatus(DeviceStatus.MAINTENANCE);
        long devicesNeedCalibration = deviceRepository.countByNextCalibrationBefore(LocalDateTime.now());

        return DeviceStatisticsResponse.builder()
                .totalDevices(totalDevices)
                .activeDevices(activeDevices)
                .maintenanceDevices(maintenanceDevices)
                .devicesNeedCalibration(devicesNeedCalibration)
                .build();
    }
}

package com.alcohol.alcoholdetectionsystem.service;

import com.alcohol.alcoholdetectionsystem.dto.request.DeviceRegisterRequest;
import com.alcohol.alcoholdetectionsystem.dto.response.DeviceCheckResponse;
import com.alcohol.alcoholdetectionsystem.dto.response.DeviceListResponse;
import com.alcohol.alcoholdetectionsystem.dto.response.DeviceResponse;
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

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DeviceService {
    private final DeviceRepository deviceRepository;
    private final UserRepository userRepository;

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
        return DeviceResponse.builder()
                .deviceId(deviceEntity.getDeviceId())
                .name(deviceEntity.getName())
                .model(deviceEntity.getModel())
                .build();
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
                .map(deviceEntity -> DeviceResponse.builder()
                        .deviceId(deviceEntity.getDeviceId())
                        .name(deviceEntity.getName())
                        .model(deviceEntity.getModel())
                        .build()
                )
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
        return DeviceResponse.builder()
                .deviceId(device.getDeviceId())
                .name(device.getName())
                .model(device.getModel())
                .build();
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
}

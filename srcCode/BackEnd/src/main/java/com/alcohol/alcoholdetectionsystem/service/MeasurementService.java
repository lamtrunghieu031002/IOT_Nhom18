package com.alcohol.alcoholdetectionsystem.service;

import com.alcohol.alcoholdetectionsystem.dto.request.MeasurementRequest;
import com.alcohol.alcoholdetectionsystem.dto.response.AlcoholTestResponse;
import com.alcohol.alcoholdetectionsystem.dto.response.MeasurementListResponse;
import com.alcohol.alcoholdetectionsystem.dto.response.MeasurementStatisticsResponse;
import com.alcohol.alcoholdetectionsystem.dto.response.ViolationDetailResponse;
import com.alcohol.alcoholdetectionsystem.entity.AlcoholTestEntity;
import com.alcohol.alcoholdetectionsystem.entity.DeviceEntity;
import com.alcohol.alcoholdetectionsystem.entity.UserEntity;
import com.alcohol.alcoholdetectionsystem.entity.ViolationEntity;
import com.alcohol.alcoholdetectionsystem.repository.AlcoholTestRepository;
import com.alcohol.alcoholdetectionsystem.repository.DeviceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class MeasurementService {
    private final AlcoholTestRepository alcoholTestRepository;
    private final UserService userService;
    private final ViolationService violationService;
    private final DeviceRepository deviceRepository;

    private AlcoholTestResponse toAlcoholTestResponse(AlcoholTestEntity alcoholEntity) {
        ViolationDetailResponse violationResponse = null;
        if (alcoholEntity.getViolationEntity() != null) {
            violationResponse = violationService.toViolationDetailResponse(alcoholEntity.getViolationEntity());
        }

        return AlcoholTestResponse.builder()
                .id(alcoholEntity.getId())
                .deviceId(alcoholEntity.getDeviceEntity().getDeviceId())
                .deviceName(alcoholEntity.getDeviceEntity().getName())
                .officerId(alcoholEntity.getOfficer().getId())
                .officerFullName(alcoholEntity.getOfficer().getFullName())
                .subjectName(alcoholEntity.getSubjectName())
                .subjectId(alcoholEntity.getSubjectId())
                .subjectAge(alcoholEntity.getSubjectAge())
                .subjectGender(alcoholEntity.getSubjectGenders())
                .alcoholLevel(alcoholEntity.getAlcoholLevel())
                .location(alcoholEntity.getLocation())
                .locationCoordinates(alcoholEntity.getLocationCoordinates())
                .violationLevel(alcoholEntity.getViolationLevel())
                .testTime(alcoholEntity.getTestTime())
                .violation(violationResponse)
                .build();
    }
    public AlcoholTestResponse createMeasurement(MeasurementRequest measurementRequest, Authentication authentication) {

        String username = authentication.getName();
        UserEntity user = userService.findByUsername(username);
        AlcoholTestEntity alcoholTest = new AlcoholTestEntity();

        alcoholTest.setSubjectName(measurementRequest.getSubjectName());
        alcoholTest.setSubjectId(measurementRequest.getSubjectId());
        alcoholTest.setSubjectAge(measurementRequest.getSubjectAge());
        alcoholTest.setSubjectGenders(measurementRequest.getSubjectGender());
        alcoholTest.setAlcoholLevel(measurementRequest.getAlcoholLevel());
        alcoholTest.setLocation(measurementRequest.getLocation());
        alcoholTest.setLocationCoordinates(measurementRequest.getLocationCoordinates());
        alcoholTest.setStatus("COMPLETED");
        DeviceEntity device = deviceRepository.findByDeviceId(measurementRequest.getDeviceId())
                .orElseThrow(() -> new IllegalArgumentException("Device not found"));


        if (device.getStatus() != com.alcohol.alcoholdetectionsystem.enums.DeviceStatus.ACTIVE) {
            throw new IllegalArgumentException("Device is not active");
        }

        alcoholTest.setDeviceEntity(device);
        alcoholTest.setOfficer(user);
        alcoholTest.setTestTime(LocalDateTime.now());
        String violationLevel = determineViolationLevel(measurementRequest.getAlcoholLevel());
        alcoholTest.setViolationLevel(violationLevel);
        alcoholTest.setStatus("pending");

        AlcoholTestEntity savedMeasurement = alcoholTestRepository.save(alcoholTest);

        if (!"none".equals(violationLevel)) {
            ViolationEntity violationEntity = violationService.createViolation(savedMeasurement);
            savedMeasurement.setViolationEntity(violationEntity);
        }

        return toAlcoholTestResponse(savedMeasurement);
    }

    private String determineViolationLevel(Double alcoholLevel) {
        if (alcoholLevel < 0.2) {
            return "none";
        } else if (alcoholLevel <= 0.4) {
            return "low";
        } else {
            return "high";
        }
    }

    public MeasurementListResponse getAllMeasurements(int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());

        Page<AlcoholTestEntity> measurementPage = alcoholTestRepository.findAll(pageable);

        List<AlcoholTestResponse> measurements = measurementPage.getContent().stream()
                .map(this::toAlcoholTestResponse)
                .toList();

        return MeasurementListResponse.builder()
                .measurements(measurements)
                .page(page)
                .size(size)
                .total(measurementPage.getTotalElements())
                .totalPages(measurementPage.getTotalPages())
                .build();
    }

    public AlcoholTestResponse getMeasurementById(Long id) {
        AlcoholTestEntity alcoholEntity = alcoholTestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Measurement not found"));
        return toAlcoholTestResponse(alcoholEntity);
    }

    public MeasurementStatisticsResponse getMeasurementStatistics(LocalDateTime startDate, LocalDateTime endDate) {
        List<AlcoholTestEntity> measurements;

        if (startDate != null && endDate != null) {
            measurements = alcoholTestRepository.findByCreatedAtBetween(startDate, endDate);
        } else {
            measurements = alcoholTestRepository.findAll();
        }

        long totalTests = measurements.size();
        long violations = measurements.stream()
                .filter(m -> !"none".equals(m.getViolationLevel()))
                .count();

        double averageLevel = measurements.stream()
                .mapToDouble(AlcoholTestEntity::getAlcoholLevel)
                .average()
                .orElse(0.0);

        Map<String, Long> byLevel = new HashMap<>();
        byLevel.put("none", measurements.stream().filter(m -> "none".equals(m.getViolationLevel())).count());
        byLevel.put("low", measurements.stream().filter(m -> "low".equals(m.getViolationLevel())).count());
        byLevel.put("high", measurements.stream().filter(m -> "high".equals(m.getViolationLevel())).count());

        return MeasurementStatisticsResponse.builder()
                .totalTests(totalTests)
                .violations(violations)
                .averageLevel(averageLevel)
                .byLevel(byLevel)
                .build();
    }

    public List<AlcoholTestResponse> getMeasurementsByOfficer(Long officerId) {
        return alcoholTestRepository.findByOfficerId(officerId).stream()
                .map(this::toAlcoholTestResponse)
                .toList();
    }

    public List<AlcoholTestResponse> getMeasurementsByDevice(String deviceId) {
        return alcoholTestRepository.findByDeviceEntityDeviceId(deviceId).stream()
                .map(this::toAlcoholTestResponse)
                .toList();
    }
}
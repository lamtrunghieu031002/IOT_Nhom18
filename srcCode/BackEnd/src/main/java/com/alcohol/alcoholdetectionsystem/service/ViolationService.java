package com.alcohol.alcoholdetectionsystem.service;

import com.alcohol.alcoholdetectionsystem.dto.response.ViolationStatisticsResponse;
import com.alcohol.alcoholdetectionsystem.entity.AlcoholTestEntity;
import com.alcohol.alcoholdetectionsystem.entity.ViolationEntity;
import com.alcohol.alcoholdetectionsystem.repository.ViolationRepository;
import com.alcohol.alcoholdetectionsystem.dto.response.ViolationDetailResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class ViolationService {
    private final ViolationRepository violationRepository;

    private static final Map<String, String> VIOLATION_CODES = Map.of(
            "low", "ALC001",
            "high", "ALC002"
    );

    private static final Map<String, Double> FINE_AMOUNTS = Map.of(
            "low", 2500000.0,
            "high", 5000000.0
    );

    public ViolationDetailResponse toViolationDetailResponse(ViolationEntity entity) {
        return ViolationDetailResponse.builder()
                .id(entity.getId())
                .testId(entity.getAlcoholTest().getId())
                .processedByOfficerId(entity.getProcessedBy().getId())
                .processedByOfficerFullName(entity.getProcessedBy().getFullName())
                .level(entity.getLevel())
                .violationCode(entity.getViolationCode())
                .fineAmount(entity.getFineAmount())
                .paymentDeadline(entity.getPaymentDeadline())
                .paymentDate(entity.getPaidAt())
                .build();
    }

    @Transactional
    public ViolationEntity createViolation(AlcoholTestEntity measurement) {
        String violationLevel = measurement.getViolationLevel();

        if ("none".equals(violationLevel)) {
            return null;
        }

        ViolationEntity violation = new ViolationEntity();

        violation.setAlcoholTest(measurement);
        violation.setProcessedBy(measurement.getOfficer());

        violation.setLevel(violationLevel);
        violation.setViolationCode(VIOLATION_CODES.get(violationLevel));
        violation.setFineAmount(FINE_AMOUNTS.get(violationLevel));
        violation.setStatus("unpaid");
        violation.setPaymentDeadline(LocalDateTime.now().plusDays(30));

        if ("high".equals(violationLevel)) {
            violation.setLicenseConfiscated(true);
            violation.setVehicleDetained(true);
        }

        return violationRepository.save(violation);
    }

    public ViolationStatisticsResponse getViolationStatistics(LocalDateTime startDate, LocalDateTime endDate) {
        List<ViolationEntity> violations;

        if (startDate != null && endDate != null) {
            violations = violationRepository.findByCreatedAtBetween(startDate, endDate);
        } else {
            violations = violationRepository.findAll();
        }

        long total = violations.size();

        Map<String, Long> byLevel = new HashMap<>();
        byLevel.put("low", violations.stream()
                .filter(v -> "low".equals(v.getLevel()))
                .count());
        byLevel.put("high", violations.stream()
                .filter(v -> "high".equals(v.getLevel()))
                .count());

        double totalFines = violations.stream()
                .mapToDouble(ViolationEntity::getFineAmount)
                .sum();

        double paidFines = violations.stream()
                .filter(v -> "paid".equals(v.getStatus()))
                .mapToDouble(ViolationEntity::getFineAmount)
                .sum();

        return ViolationStatisticsResponse.builder()
                .total(total)
                .byLevel(byLevel)
                .totalFines(totalFines)
                .paidFines(paidFines)
                .build();
    }
}
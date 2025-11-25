package com.alcohol.alcoholdetectionsystem.service;

import com.alcohol.alcoholdetectionsystem.dto.response.StatisticsResponse;
import com.alcohol.alcoholdetectionsystem.entity.AlcoholTestEntity;
import com.alcohol.alcoholdetectionsystem.entity.ViolationEntity;
import com.alcohol.alcoholdetectionsystem.repository.AlcoholTestRepository;
import com.alcohol.alcoholdetectionsystem.repository.ViolationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final AlcoholTestRepository alcoholTestRepository;
    private final ViolationRepository violationRepository;

    public StatisticsResponse generateStatistics(String timeRange) {
        LocalDateTime startDate = calculateStartDate(timeRange);

        List<AlcoholTestEntity> measurements = alcoholTestRepository.findByCreatedAtAfter(startDate);
        List<ViolationEntity> violations = violationRepository.findByCreatedAtAfter(startDate);
        Map<String, Long> violationLevels = calculateViolationLevels(measurements);
        Double totalFines = calculateTotalFines(violations);
        Double paidFines = calculatePaidFines(violations);
        Map<String, Long> ageDistribution = calculateAgeDistribution(measurements);
        return StatisticsResponse.builder()
                .totalTests(measurements.size())
                .totalViolations(violations.size())
                .violationLevels(violationLevels)
                .totalFines(totalFines)
                .paidFines(paidFines)
                .ageDistribution(ageDistribution)
                .build();
    }

    private LocalDateTime calculateStartDate(String timeRange) {
        LocalDateTime now = LocalDateTime.now();
        return switch (timeRange) {
            case "week" -> now.minusDays(7);
            case "month" -> now.minusDays(30);
            case "year" -> now.minusDays(365);
            default -> now.minusDays(7);
        };
    }

    private Map<String, Long> calculateViolationLevels(List<AlcoholTestEntity> measurements) {
        Map<String, Long> levels = new HashMap<>();
        levels.put("none", measurements.stream().filter(m -> "none".equals(m.getViolationLevel())).count());
        levels.put("low", measurements.stream().filter(m -> "low".equals(m.getViolationLevel())).count());
        levels.put("high", measurements.stream().filter(m -> "high".equals(m.getViolationLevel())).count());
        return levels;
    }

    private Double calculateTotalFines(List<ViolationEntity> violations) {
        return violations.stream()
                .mapToDouble(ViolationEntity::getFineAmount)
                .sum();
    }

    private Double calculatePaidFines(List<ViolationEntity> violations) {
        return violations.stream()
                .filter(v -> "paid".equals(v.getStatus())) // Sửa: Dùng status field
                .mapToDouble(ViolationEntity::getFineAmount)
                .sum();
    }

    private Map<String, Long> calculateAgeDistribution(List<AlcoholTestEntity> measurements) {
        Map<String, Long> distribution = new HashMap<>();
        distribution.put("18-25", measurements.stream().filter(m -> m.getSubjectAge() != null && m.getSubjectAge() <= 25).count());
        distribution.put("26-35", measurements.stream().filter(m -> m.getSubjectAge() != null && m.getSubjectAge() > 25 && m.getSubjectAge() <= 35).count());
        distribution.put("36-45", measurements.stream().filter(m -> m.getSubjectAge() != null && m.getSubjectAge() > 35 && m.getSubjectAge() <= 45).count());
        distribution.put("46-55", measurements.stream().filter(m -> m.getSubjectAge() != null && m.getSubjectAge() > 45 && m.getSubjectAge() <= 55).count());
        distribution.put("55+", measurements.stream().filter(m -> m.getSubjectAge() != null && m.getSubjectAge() > 55).count());
        return distribution;
    }
}
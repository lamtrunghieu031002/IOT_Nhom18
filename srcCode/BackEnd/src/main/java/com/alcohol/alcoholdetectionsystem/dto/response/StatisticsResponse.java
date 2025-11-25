package com.alcohol.alcoholdetectionsystem.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatisticsResponse {
    private Integer totalTests;
    private Integer totalViolations;
    private Map<String, Long> violationLevels;
    private Double totalFines;
    private Double paidFines;
    private Map<String, Long> ageDistribution;
}

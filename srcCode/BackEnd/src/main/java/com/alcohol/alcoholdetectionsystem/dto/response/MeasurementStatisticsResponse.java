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
public class MeasurementStatisticsResponse {
    private Long totalTests;
    private Long violations;
    private Double averageLevel;
    private Map<String, Long> byLevel;
}
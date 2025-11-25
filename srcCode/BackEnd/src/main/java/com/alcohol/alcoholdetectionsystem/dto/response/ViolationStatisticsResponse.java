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
public class ViolationStatisticsResponse {
    private Long total;
    private Map<String, Long> byLevel;
    private Double totalFines;
    private Double paidFines;
}
package com.alcohol.alcoholdetectionsystem.controller;

import com.alcohol.alcoholdetectionsystem.dto.response.ApiResponse;
import com.alcohol.alcoholdetectionsystem.dto.response.StatisticsResponse;
import com.alcohol.alcoholdetectionsystem.dto.response.ViolationStatisticsResponse;
import com.alcohol.alcoholdetectionsystem.service.StatisticsService;
import com.alcohol.alcoholdetectionsystem.service.ViolationService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;
    private final ViolationService violationService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping()
    public ResponseEntity<ApiResponse<StatisticsResponse>> getStatistics(
            @RequestParam(defaultValue = "week") String range) {
        try {
            StatisticsResponse stats = statisticsService.generateStatistics(range);
            return ResponseEntity.ok(new ApiResponse<>(true, null, stats));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/violations")
    public ResponseEntity<ApiResponse<ViolationStatisticsResponse>> getViolationStatistics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        try {
            ViolationStatisticsResponse stats = violationService.getViolationStatistics(startDate, endDate);
            return ResponseEntity.ok(new ApiResponse<>(true, null, stats));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }
}
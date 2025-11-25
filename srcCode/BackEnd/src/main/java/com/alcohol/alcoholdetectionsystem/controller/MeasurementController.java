package com.alcohol.alcoholdetectionsystem.controller;

import com.alcohol.alcoholdetectionsystem.dto.response.AlcoholTestResponse;
import com.alcohol.alcoholdetectionsystem.dto.response.ApiResponse;
import com.alcohol.alcoholdetectionsystem.dto.request.MeasurementRequest;
import com.alcohol.alcoholdetectionsystem.dto.response.MeasurementListResponse;
import com.alcohol.alcoholdetectionsystem.dto.response.MeasurementStatisticsResponse;
import com.alcohol.alcoholdetectionsystem.service.MeasurementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/measurements")
@RequiredArgsConstructor
public class MeasurementController {

    private final MeasurementService measurementService;

    @PostMapping()
    public ResponseEntity<ApiResponse<AlcoholTestResponse>> createMeasurement(
            @Valid @RequestBody MeasurementRequest request,
            Authentication authentication) {
        try {
            AlcoholTestResponse savedMeasurement = measurementService.createMeasurement(request, authentication);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(true, "Measurement recorded successfully", savedMeasurement));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "An error occurred", null));
        }
    }

    @GetMapping("")
    public ResponseEntity<ApiResponse<MeasurementListResponse>> getAllMeasurements(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            MeasurementListResponse response = measurementService.getAllMeasurements(page, size);
            return ResponseEntity.ok(new ApiResponse<>(true, null, response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AlcoholTestResponse>> getMeasurementById(@PathVariable Long id) {
        try {
            AlcoholTestResponse measurement = measurementService.getMeasurementById(id);
            return ResponseEntity.ok(new ApiResponse<>(true, null, measurement));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<MeasurementStatisticsResponse>> getMeasurementStatistics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        try {
            MeasurementStatisticsResponse stats = measurementService.getMeasurementStatistics(startDate, endDate);
            return ResponseEntity.ok(new ApiResponse<>(true, null, stats));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    @GetMapping("/officer/{officerId}")
    public ResponseEntity<ApiResponse<List<AlcoholTestResponse>>> getMeasurementsByOfficer(
            @PathVariable Long officerId) {
        try {
            List<AlcoholTestResponse> measurements = measurementService.getMeasurementsByOfficer(officerId);
            return ResponseEntity.ok(new ApiResponse<>(true, null, measurements));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    @GetMapping("/device/{deviceId}")
    public ResponseEntity<ApiResponse<List<AlcoholTestResponse>>> getMeasurementsByDevice(
            @PathVariable String deviceId) {
        try {
            List<AlcoholTestResponse> measurements = measurementService.getMeasurementsByDevice(deviceId);
            return ResponseEntity.ok(new ApiResponse<>(true, null, measurements));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }
}
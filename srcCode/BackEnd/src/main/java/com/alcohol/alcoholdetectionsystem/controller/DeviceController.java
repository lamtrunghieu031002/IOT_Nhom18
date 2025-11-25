package com.alcohol.alcoholdetectionsystem.controller;

import com.alcohol.alcoholdetectionsystem.dto.request.DeviceRegisterRequest;
import com.alcohol.alcoholdetectionsystem.dto.response.*;
import com.alcohol.alcoholdetectionsystem.service.DeviceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/devices")
@RequiredArgsConstructor
public class DeviceController {
    private final DeviceService deviceService;

    @PreAuthorize("hasAnyRole('ADMIN', 'OFFICER')")
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<DeviceResponse>> registerDevice(
            @Valid @RequestBody DeviceRegisterRequest request,
            Authentication authentication) {
        try {
            DeviceResponse deviceResponse = deviceService.registerDevice(request, authentication);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(true, "Device registered successfully", deviceResponse));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'OFFICER')")
    @GetMapping("/check/{deviceId}")
    public ResponseEntity<ApiResponse<DeviceCheckResponse>> checkDevice(@PathVariable String deviceId) {
        try {
            DeviceCheckResponse response = deviceService.checkDevice(deviceId);
            return ResponseEntity.ok(new ApiResponse<>(true, null, response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    @GetMapping("")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DeviceListResponse>> getAllDevices(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            DeviceListResponse response = deviceService.getAllDevices(status, search, page, size);
            return ResponseEntity.ok(new ApiResponse<>(true, null, response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{deviceId}")
    public ResponseEntity<ApiResponse<Void>> deleteDevice(@PathVariable String deviceId) {
        try {
            deviceService.deleteDevice(deviceId);
            return ResponseEntity.ok(
                    new ApiResponse<>(true, "Device deleted successfully", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'OFFICER')")
    @GetMapping("/{deviceId}")
    public ResponseEntity<ApiResponse<DeviceResponse>> getDeviceDetails(@PathVariable String deviceId) {
        try {
            DeviceResponse device = deviceService.getDeviceById(deviceId);
            return ResponseEntity.ok(new ApiResponse<>(true, null, device));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'OFFICER')")
    @PutMapping("/{deviceId}/status")
    public ResponseEntity<ApiResponse<DeviceResponse>> updateDeviceStatus(
            @PathVariable String deviceId,
            @RequestParam String status) {
        try {
            DeviceResponse updatedDevice = deviceService.updateDeviceStatus(deviceId, status);
            return ResponseEntity.ok(new ApiResponse<>(true, "Device status updated successfully", updatedDevice));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{deviceId}/calibration")
    public ResponseEntity<ApiResponse<DeviceResponse>> updateDeviceCalibration(@PathVariable String deviceId) {
        try {
            DeviceResponse updatedDevice = deviceService.updateCalibration(deviceId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Device calibration updated successfully", updatedDevice));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/calibration/needed")
    public ResponseEntity<ApiResponse<List<DeviceCalibrationResponse>>> getDevicesNeedCalibration() {
        try {
            List<DeviceCalibrationResponse> devices = deviceService.getDevicesNeedCalibration();
            return ResponseEntity.ok(new ApiResponse<>(true, null, devices));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<DeviceStatisticsResponse>> getDeviceStatistics() {
        try {
            DeviceStatisticsResponse stats = deviceService.getDeviceStatistics();
            return ResponseEntity.ok(new ApiResponse<>(true, null, stats));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }
}

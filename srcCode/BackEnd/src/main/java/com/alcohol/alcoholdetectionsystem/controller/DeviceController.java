package com.alcohol.alcoholdetectionsystem.controller;

import com.alcohol.alcoholdetectionsystem.dto.request.DeviceRegisterRequest;
import com.alcohol.alcoholdetectionsystem.dto.response.ApiResponse;
import com.alcohol.alcoholdetectionsystem.dto.response.DeviceCheckResponse;
import com.alcohol.alcoholdetectionsystem.dto.response.DeviceListResponse;
import com.alcohol.alcoholdetectionsystem.dto.response.DeviceResponse;
import com.alcohol.alcoholdetectionsystem.enums.DeviceStatus;
import com.alcohol.alcoholdetectionsystem.service.DeviceService;
import com.alcohol.alcoholdetectionsystem.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/devices")
@RequiredArgsConstructor
public class DeviceController {
    private final DeviceService deviceService;
    private final UserService userService;

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
}

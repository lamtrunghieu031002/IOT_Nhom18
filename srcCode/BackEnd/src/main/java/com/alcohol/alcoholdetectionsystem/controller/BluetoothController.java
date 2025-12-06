package com.alcohol.alcoholdetectionsystem.controller;

import com.alcohol.alcoholdetectionsystem.dto.response.ApiResponse;
import com.alcohol.alcoholdetectionsystem.dto.response.DeviceResponse; // Reuse DTO cũ
import com.alcohol.alcoholdetectionsystem.service.BluetoothService;
import com.alcohol.alcoholdetectionsystem.service.DeviceService; // Service check DB
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bluetooth")
@RequiredArgsConstructor
public class BluetoothController {

    @Autowired
    private BluetoothService bluetoothService;

    @Autowired
    private DeviceService deviceService; // Để check DB ngay sau khi quét

    @PreAuthorize("hasAnyRole('ADMIN', 'OFFICER')")
    @GetMapping("/scan-and-check")
    public ResponseEntity<ApiResponse<List<DeviceResponse>>> scanAndCheck() {
        try {
            // 1. Quét thực tế (Mất khoảng 10s)
            List<String> foundMacs = bluetoothService.scanDevices();

            if (foundMacs.isEmpty()) {
                return ResponseEntity.ok(new ApiResponse<>(true, "Không tìm thấy thiết bị Bluetooth nào gần đây.", List.of()));
            }

            // 2. Check với Database (Hàm checkBatchDevices bạn đã viết lúc nãy)
            List<DeviceResponse> registeredDevices = deviceService.checkBatchDevices(foundMacs);

            return ResponseEntity.ok(new ApiResponse<>(true, "Quét thành công. Tìm thấy " + registeredDevices.size() + " thiết bị đã đăng ký.", registeredDevices));

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse<>(false, "Lỗi quét Bluetooth: " + e.getMessage(), null));
        }
    }
}
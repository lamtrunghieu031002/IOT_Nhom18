package com.alcohol.alcoholdetectionsystem.dto.response;

import com.alcohol.alcoholdetectionsystem.enums.DeviceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceCheckResponse {
    private boolean exists;
    private String deviceId;
    private String deviceName;
    private DeviceStatus status;
    private LocalDateTime lastCalibration;
    private LocalDateTime nextCalibration;
}
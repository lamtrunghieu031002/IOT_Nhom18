package com.alcohol.alcoholdetectionsystem.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceCalibrationResponse {
    private String deviceId;
    private String name;
    private LocalDateTime lastCalibration;
    private LocalDateTime nextCalibration;
    private Long daysOverdue;
}

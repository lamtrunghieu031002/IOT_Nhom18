package com.alcohol.alcoholdetectionsystem.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceStatisticsResponse {
    private Long totalDevices;
    private Long activeDevices;
    private Long maintenanceDevices;
    private Long devicesNeedCalibration;
}
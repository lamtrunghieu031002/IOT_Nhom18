package com.alcohol.alcoholdetectionsystem.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceListResponse {
    private List<DeviceResponse> devices;
    private int page;
    private int size;
    private long total;
    private int totalPages;
}
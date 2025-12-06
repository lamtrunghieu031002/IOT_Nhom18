package com.alcohol.alcoholdetectionsystem.dto.request;

import lombok.Data;
import java.util.List;

@Data
public class DeviceScanCheckRequest {
    private List<String> macAddresses;
}
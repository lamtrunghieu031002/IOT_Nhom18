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
public class MeasurementListResponse {
    private List<AlcoholTestResponse> measurements;
    private int page;
    private int size;
    private long total;
    private int totalPages;
}
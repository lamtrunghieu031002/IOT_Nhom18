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
public class ViolationDetailResponse {
    private Long id;
    private Long testId;
    private Long processedByOfficerId;
    private String processedByOfficerFullName;
    private String level;
    private String violationCode;
    private Double fineAmount;
    private LocalDateTime paymentDeadline;
    private LocalDateTime paymentDate;
}
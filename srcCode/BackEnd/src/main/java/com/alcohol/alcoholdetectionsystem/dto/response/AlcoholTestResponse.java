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
public class AlcoholTestResponse {
    private Long id;
    private String deviceId;
    private String deviceName;
    private Long officerId;
    private String officerFullName;
    private String subjectName;
    private String subjectId;
    private Integer subjectAge;
    private String subjectGender;
    private Double alcoholLevel;
    private String location;
    private String locationCoordinates;
    private String violationLevel;
    private LocalDateTime testTime;
    private ViolationDetailResponse violation;
}
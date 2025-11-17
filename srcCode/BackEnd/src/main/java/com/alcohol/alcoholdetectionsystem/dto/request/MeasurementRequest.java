package com.alcohol.alcoholdetectionsystem.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MeasurementRequest {
    @NotBlank(message = "Device ID is required")
    private String deviceId;

    @NotBlank(message = "Subject name is required")
    private String subjectName;

    @NotBlank(message = "Subject ID is required")
    private String subjectId;

    @NotNull(message = "Subject age is required")
    @Min(value = 16, message = "Subject age must be at least 16")
    @Max(value = 100, message = "Subject age must not exceed 100")
    private Integer subjectAge;

    private String subjectGender;

    @NotNull(message = "Alcohol level is required")
    @DecimalMin(value = "0.0", message = "Alcohol level must be positive")
    private Double alcoholLevel;

    @Size(max = 200, message = "Location must not exceed 200 characters")
    private String location;

    @Size(max = 50, message = "Location coordinates must not exceed 50 characters")
    private String locationCoordinates;
}
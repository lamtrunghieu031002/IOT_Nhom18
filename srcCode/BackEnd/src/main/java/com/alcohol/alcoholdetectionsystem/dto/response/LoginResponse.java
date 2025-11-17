package com.alcohol.alcoholdetectionsystem.dto.response;

import com.alcohol.alcoholdetectionsystem.enums.RoleEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {
    private String accessToken;
    private RoleEnum role;
    private Long userId;
}
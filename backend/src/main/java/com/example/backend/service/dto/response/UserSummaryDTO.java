package com.example.backend.service.dto.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class UserSummaryDTO {
    String username;
    String email;
    String displayName;
    int gender;
    String phoneNumber;
    String avatarUrl;
    double creditPoint;
}

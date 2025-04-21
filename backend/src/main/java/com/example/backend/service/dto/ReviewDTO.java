package com.example.backend.service.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class ReviewDTO {
    int starsNum;
    String comment;
    String accountDisplayName;
    String avatarUrl;
}

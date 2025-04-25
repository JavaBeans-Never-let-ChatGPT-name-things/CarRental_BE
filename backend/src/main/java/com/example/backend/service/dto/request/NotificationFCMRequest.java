package com.example.backend.service.dto.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NotificationFCMRequest {
    private String token;
    private String title;
    private String body;
}

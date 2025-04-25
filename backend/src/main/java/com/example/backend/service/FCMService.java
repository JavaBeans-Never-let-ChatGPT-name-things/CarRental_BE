package com.example.backend.service;

import com.example.backend.service.dto.request.NotificationFCMRequest;

public interface FCMService {
    String findFCMTokenByToken(String accessToken, String token);
    String sendNotification(Long userId, NotificationFCMRequest notificationRequest);
}

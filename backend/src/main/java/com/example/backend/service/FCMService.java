package com.example.backend.service;

import com.example.backend.entity.NotificationEntity;
import com.example.backend.service.dto.request.NotificationFCMRequest;

import java.util.List;

public interface FCMService {
    String findFCMTokenByToken(String accessToken, String token);
    String sendNotification(Long userId, Long contractId, NotificationFCMRequest notificationFCMRequest);
    List<NotificationEntity> getNotifications(String token);
    String markAllNotificationAsRead(String token);
}

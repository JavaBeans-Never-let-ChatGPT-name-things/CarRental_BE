package com.example.backend.controller;

import com.example.backend.service.FCMService;
import com.example.backend.service.FirebaseMessagingService;
import com.example.backend.service.dto.request.NotificationFCMRequest;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final FCMService fcmService;
    @PostMapping("/register/{fcmToken}")
    public String registerFCMToken(HttpServletRequest request,@PathVariable String fcmToken) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return fcmService.findFCMTokenByToken(token, fcmToken);
    }

    @PostMapping("/send/{userId}")
    public String sendNotification(@PathVariable Long userId, @RequestBody NotificationFCMRequest notificationRequest) {
        return fcmService.sendNotification(userId, notificationRequest);
    }
}

package com.example.backend.controller;

import com.example.backend.entity.NotificationEntity;
import com.example.backend.service.FCMService;
import com.example.backend.service.dto.request.NotificationFCMRequest;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


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

    @PostMapping("/send/{userId}/{contractId}")
    public String sendNotification(@PathVariable Long userId,
                                   @PathVariable Long contractId,
                                   @RequestBody NotificationFCMRequest notificationFCMRequest) {
        return fcmService.sendNotification(userId, contractId, notificationFCMRequest);
    }

    @GetMapping("/get")
    public List<NotificationEntity> getNotifications(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return fcmService.getNotifications(token);
    }

    @PostMapping("/readAll")
    public ResponseEntity<?> markAllNotificationAsRead(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return ResponseEntity.ok().body(Map.of("message", fcmService.markAllNotificationAsRead(token)));
    }
    @DeleteMapping("/deleteAll")
    public ResponseEntity<?> deleteAllNotifications(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return ResponseEntity.ok().body(Map.of("message", fcmService.deleteAllNotifications(token)));
    }
}

package com.example.backend.service;

import com.example.backend.entity.AccountEntity;
import com.example.backend.entity.FCMTokenEntity;
import com.example.backend.repository.AccountRepository;
import com.example.backend.repository.FCMRepository;
import com.example.backend.service.dto.request.NotificationFCMRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Slf4j
public class FCMServiceImpl implements FCMService{
    FCMRepository fcmTokenRepository;
    AccountRepository accountRepository;
    JwtService jwtService;
    FirebaseMessagingService firebaseMessagingService;
    @Override
    public String findFCMTokenByToken(String accessToken, String token) {
        log.info("Access Token: {}", accessToken);
        log.info("Device Token: {}", token);
        String username = jwtService.extractUserName(accessToken);
        AccountEntity account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        if (token == null || token.isEmpty()) {
            log.info("Token is null or empty");
            throw new RuntimeException("Required Token");
        }
        if (fcmTokenRepository.findByToken(token).isPresent())
        {
            return "Token exists";
        }
        else{
            FCMTokenEntity fcmTokenEntity = FCMTokenEntity.builder()
                    .user(account)
                    .token(token)
                    .build();
            log.info("Saving token: {}", fcmTokenEntity.getToken());
            account.addFCMToken(fcmTokenEntity);
            accountRepository.save(account);
            log.info("Token saved successfully");
            return "Successfully saved token";
        }
    }

    @Override
    public String sendNotification(Long userId, NotificationFCMRequest notificationRequest) {
        List<FCMTokenEntity> fcmTokenEntities = fcmTokenRepository.findAllByUserId(userId);
        if (fcmTokenEntities.isEmpty()) {
            return "No tokens found for user";
        }
        for (FCMTokenEntity fcmTokenEntity : fcmTokenEntities) {
            String token = fcmTokenEntity.getToken();
            notificationRequest.setToken(token);
            log.info("Sending notification to token: {}", token);
            log.info("Notification request: {}", notificationRequest);
            String result = firebaseMessagingService.sendNotification(notificationRequest);
            if (!result.equals("Notification sent successfully")) {
                return result;
            }
        }
        return "Successfully sent notification to user with ID: " + userId;
    }
}

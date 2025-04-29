package com.example.backend.service;

import com.example.backend.entity.*;
import com.example.backend.repository.*;
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
    NotificationRepository notificationRepository;
    FCMRepository fcmTokenRepository;
    AccountRepository accountRepository;
    ContractRepository contractRepository;
    CarRepository carRepository;
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
    public String sendNotification(Long userId, Long contractId, NotificationFCMRequest notificationFCMRequest) {
        List<FCMTokenEntity> fcmTokenEntities = fcmTokenRepository.findAllByUserId(userId);
        AccountEntity account = accountRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        if (fcmTokenEntities.isEmpty()) {
            return "No tokens found for user";
        }
        RentalContractEntity rentalContract = contractRepository.findById(contractId)
                .orElseThrow(() -> new RuntimeException("Rental contract not found"));
        List<RentalContractEntity> rentalContracts = account.getRentalContracts();
        if (!rentalContracts.contains(rentalContract)){
            return "User does not own this rental contract";
        }
        CarEntity car = carRepository.findByContractId(contractId)
                .orElseThrow(() -> new RuntimeException("Car not found"));
        notificationFCMRequest.setImage(car.getCarImageUrl());
        for (FCMTokenEntity fcmTokenEntity : fcmTokenEntities) {
            String token = fcmTokenEntity.getToken();
            notificationFCMRequest.setToken(token);
            log.info("Sending notification to token: {}", token);
            log.info("Notification request: {}", notificationFCMRequest);
            String result = firebaseMessagingService.sendNotification(notificationFCMRequest);
            if (!result.equals("Notification sent successfully")) {
                log.info("Failed to send notification due to: {}", result);
            }
        }
        NotificationEntity notificationEntity = NotificationEntity.builder()
                .title(notificationFCMRequest.getTitle())
                .message(notificationFCMRequest.getBody())
                .isRead(false)
                .imageUrl(car.getCarImageUrl())
                .account(account)
                .build();
        account.addNotification(notificationEntity);
        accountRepository.save(account);
        log.info("Notification saved successfully");
        return "Successfully sent notification to user with ID: " + userId;
    }

    @Override
    public List<NotificationEntity> getNotifications(String token) {
        String username = jwtService.extractUserName(token);
        AccountEntity account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        return notificationRepository.findByAccountId(account.getId());
    }

    @Override
    public String markAllNotificationAsRead(String token) {
        String username = jwtService.extractUserName(token);
        AccountEntity account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        List<NotificationEntity> notifications = notificationRepository.findByAccountId(account.getId());
        for (NotificationEntity notification : notifications) {
            notification.setIsRead(true);
            notificationRepository.save(notification);
        }
        return "All notifications marked as read";
    }
}

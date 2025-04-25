package com.example.backend.service;

import com.example.backend.service.dto.request.NotificationFCMRequest;
import com.google.firebase.messaging.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FirebaseMessagingService {
    FirebaseMessaging firebaseMessaging;
    public String sendNotification(NotificationFCMRequest notificationRequest){
        Notification notification = Notification.builder()
                .setBody(notificationRequest.getBody())
                .setTitle(notificationRequest.getTitle())
                .build();
        Message message = Message.builder()
                .setToken(notificationRequest.getToken())
                .setNotification(notification)
                .build();
        try{
            firebaseMessaging.send(message);
            return "Notification sent successfully";
        } catch (FirebaseMessagingException e)
        {
            return "Failed to send notification: " + e.getMessage();
        }
    }
}

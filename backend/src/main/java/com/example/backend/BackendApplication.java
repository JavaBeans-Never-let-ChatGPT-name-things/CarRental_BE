package com.example.backend;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;

@SpringBootApplication
@OpenAPIDefinition(info = @Info(title = "API Documentation", version = "1.0", description = "API Information"))
public class BackendApplication {

    @Bean
    FirebaseMessaging firebaseMessaging() throws Exception {
        GoogleCredentials googleCredentials = GoogleCredentials
                .fromStream(new ClassPathResource("gosafe-carrental-firebase-adminsdk-fbsvc-212d4ef31d.json").getInputStream());
        FirebaseOptions firebaseOptions = FirebaseOptions.builder()
                .setCredentials(googleCredentials)
                .build();
        FirebaseApp app = FirebaseApp.initializeApp(firebaseOptions, "gosafe");
        return FirebaseMessaging.getInstance(app);
    }

    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }

}

package com.example.backend.config;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

import jakarta.annotation.PostConstruct;

@Configuration
public class FirebaseConfig {

    private static final Logger log = LoggerFactory.getLogger(FirebaseConfig.class);

    @Value("${FIREBASE_SERVICE_ACCOUNT_JSON:}")
    private String serviceAccountJson;

    @PostConstruct
    public void init() {
        try {
            if (!FirebaseApp.getApps().isEmpty()) return;

            if (serviceAccountJson == null || serviceAccountJson.isBlank()) {
                log.warn("[FCM] FIREBASE_SERVICE_ACCOUNT_JSON not set — push notifications disabled");
                return;
            }

            InputStream serviceAccount = new ByteArrayInputStream(
                    serviceAccountJson.getBytes(StandardCharsets.UTF_8));

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            FirebaseApp.initializeApp(options);
            log.info("[FCM] FirebaseApp initialized");
        } catch (Exception e) {
            log.error("[FCM] Failed to initialize FirebaseApp: {}", e.getMessage());
        }
    }
}

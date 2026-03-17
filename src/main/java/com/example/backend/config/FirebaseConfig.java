package com.example.backend.config;

import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

import jakarta.annotation.PostConstruct;

@Configuration
public class FirebaseConfig {

    private static final Logger log = LoggerFactory.getLogger(FirebaseConfig.class);

    @PostConstruct
    public void init() {
        try {
            if (!FirebaseApp.getApps().isEmpty()) return;

            InputStream serviceAccount = getClass().getClassLoader()
                    .getResourceAsStream("firebase-service-account.json");

            if (serviceAccount == null) {
                log.warn("[FCM] firebase-service-account.json not found — push notifications disabled");
                return;
            }

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

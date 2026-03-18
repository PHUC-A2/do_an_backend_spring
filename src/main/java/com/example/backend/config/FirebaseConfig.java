package com.example.backend.config;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

@Configuration
public class FirebaseConfig {

    private static final Logger log = LoggerFactory.getLogger(FirebaseConfig.class);

    @Value("${FIREBASE_SERVICE_ACCOUNT_JSON:}")
    private String serviceAccountJson;

    @PostConstruct
    public void init() {
        // Kiểm tra Firebase class có sẵn không (tránh lỗi DevTools RestartClassLoader)
        try {
            Class.forName("com.google.firebase.FirebaseApp");
        } catch (ClassNotFoundException e) {
            log.warn("[FCM] Firebase classes not available in current classloader — skipping init");
            return;
        }

        try {
            initFirebase();
        } catch (Exception e) {
            log.error("[FCM] Failed to initialize FirebaseApp: {}", e.getMessage());
        }
    }

    private void initFirebase() throws Exception {
        com.google.firebase.FirebaseApp[] apps = com.google.firebase.FirebaseApp.getApps()
                .toArray(new com.google.firebase.FirebaseApp[0]);
        if (apps.length > 0) return;

        if (serviceAccountJson == null || serviceAccountJson.isBlank()) {
            log.warn("[FCM] FIREBASE_SERVICE_ACCOUNT_JSON not set — push notifications disabled");
            return;
        }

        InputStream serviceAccount = new ByteArrayInputStream(
                serviceAccountJson.getBytes(StandardCharsets.UTF_8));

        com.google.firebase.FirebaseOptions options = com.google.firebase.FirebaseOptions.builder()
                .setCredentials(com.google.auth.oauth2.GoogleCredentials.fromStream(serviceAccount))
                .build();

        com.google.firebase.FirebaseApp.initializeApp(options);
        log.info("[FCM] FirebaseApp initialized");
    }
}

package com.example.backend.controller.client;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.service.NotificationService;
import com.example.backend.util.SecurityUtil;
import com.example.backend.util.annotation.ApiMessage;

@RestController
@RequestMapping("/api/v1/devices")
public class ClientDeviceController {

    private final NotificationService notificationService;

    public ClientDeviceController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping("/register-token")
    @ApiMessage("Đăng ký mobile push token")
    public ResponseEntity<Void> registerToken(@RequestBody Map<String, String> body) {
        String email = SecurityUtil.getCurrentUserLogin().orElse("");
        String token = body.get("token");
        if (token != null && !token.isBlank()) {
            notificationService.saveFcmToken(email, token);
        }
        return ResponseEntity.ok(null);
    }
}

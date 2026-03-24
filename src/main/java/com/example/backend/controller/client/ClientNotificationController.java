package com.example.backend.controller.client;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.domain.response.notification.ResNotificationDTO;
import com.example.backend.service.NotificationService;
import com.example.backend.util.SecurityUtil;
import com.example.backend.util.annotation.ApiMessage;
import com.example.backend.util.error.IdInvalidException;

@RestController
@RequestMapping("/api/v1/client")
public class ClientNotificationController {

    private final NotificationService notificationService;
    public ClientNotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/notifications")
    @ApiMessage("Lấy danh sách thông báo")
    public ResponseEntity<List<ResNotificationDTO>> getMyNotifications() throws IdInvalidException {
        String email = SecurityUtil.getCurrentUserLogin().orElse("");
        return ResponseEntity.ok(notificationService.getMyNotifications(email));
    }

    @PatchMapping("/notifications/read-all")
    @ApiMessage("Đánh dấu tất cả đã đọc")
    public ResponseEntity<Void> markAllRead() throws IdInvalidException {
        String email = SecurityUtil.getCurrentUserLogin().orElse("");
        notificationService.markAllRead(email);
        return ResponseEntity.ok(null);
    }

    @PatchMapping("/notifications/{id}/read")
    @ApiMessage("Đánh dấu đã đọc")
    public ResponseEntity<Void> markOneRead(@PathVariable Long id) throws IdInvalidException {
        String email = SecurityUtil.getCurrentUserLogin().orElse("");
        notificationService.markOneRead(id, email);
        return ResponseEntity.ok(null);
    }

    @DeleteMapping("/notifications/{id}")
    @ApiMessage("Xóa thông báo khỏi lịch sử")
    public ResponseEntity<Void> softDelete(@PathVariable Long id) throws IdInvalidException {
        String email = SecurityUtil.getCurrentUserLogin().orElse("");
        notificationService.softDeleteByUser(id, email);
        return ResponseEntity.ok(null);
    }

    /** Path tách khỏi `/notifications/{id}` để tránh nhầm `id=all`. */
    @DeleteMapping("/notifications/clear")
    @ApiMessage("Xóa tất cả thông báo khỏi lịch sử")
    public ResponseEntity<Void> softDeleteAll() throws IdInvalidException {
        String email = SecurityUtil.getCurrentUserLogin().orElse("");
        notificationService.softDeleteAllForUser(email);
        return ResponseEntity.ok(null);
    }

    @PostMapping("/notifications/fcm-token")
    @ApiMessage("Lưu FCM token")
    public ResponseEntity<Void> saveFcmToken(@RequestBody java.util.Map<String, String> body) {
        String email = SecurityUtil.getCurrentUserLogin().orElse("");
        String token = body.get("token");
        if (token != null && !token.isBlank()) {
            notificationService.saveFcmToken(email, token);
        }
        return ResponseEntity.ok(null);
    }
}

package com.example.backend.controller.client;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.example.backend.domain.response.notification.ResNotificationDTO;
import com.example.backend.service.NotificationService;
import com.example.backend.util.SecurityUtil;
import com.example.backend.util.annotation.ApiMessage;
import com.example.backend.util.error.IdInvalidException;

@RestController
@RequestMapping("/api/v1/client")
public class ClientNotificationController {

    private final NotificationService notificationService;
    private final SecurityUtil securityUtil;

    public ClientNotificationController(NotificationService notificationService, SecurityUtil securityUtil) {
        this.notificationService = notificationService;
        this.securityUtil = securityUtil;
    }

    /**
     * SSE subscribe endpoint.
     * EventSource cannot send custom headers, so token is passed as ?token=
     * The endpoint is in the security whitelist; we decode the token here manually.
     */
    @GetMapping(value = "/notifications/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@RequestParam(required = false) String token) {
        String email = "";
        if (token != null && !token.isBlank()) {
            email = securityUtil.extractSubjectFromToken(token).orElse("");
        }
        if (email.isBlank()) {
            email = SecurityUtil.getCurrentUserLogin().orElse("");
        }
        return notificationService.subscribe(email);
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

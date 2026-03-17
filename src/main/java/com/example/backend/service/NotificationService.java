package com.example.backend.service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;

import com.example.backend.domain.entity.Booking;
import com.example.backend.domain.entity.Notification;
import com.example.backend.domain.entity.User;
import com.example.backend.domain.response.notification.ResNotificationDTO;
import com.example.backend.repository.BookingRepository;
import com.example.backend.repository.NotificationRepository;
import com.example.backend.util.constant.booking.BookingStatusEnum;
import com.example.backend.util.constant.notification.NotificationTypeEnum;
import com.example.backend.util.error.IdInvalidException;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;
    private final BookingRepository bookingRepository;
    private final UserService userService;

    // email -> list of emitters (một user có thể mở nhiều tab)
    private final Map<String, List<SseEmitter>> emitters = new ConcurrentHashMap<>();

    public NotificationService(NotificationRepository notificationRepository,
            BookingRepository bookingRepository,
            UserService userService) {
        this.notificationRepository = notificationRepository;
        this.bookingRepository = bookingRepository;
        this.userService = userService;
    }

    // ──────────────────────────────────────────────────────────────
    // SSE subscription
    // ──────────────────────────────────────────────────────────────

    public SseEmitter subscribe(String email) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);

        emitters.computeIfAbsent(email, k -> new ArrayList<>()).add(emitter);

        Runnable cleanup = () -> {
            List<SseEmitter> list = emitters.get(email);
            if (list != null) {
                list.remove(emitter);
                if (list.isEmpty()) emitters.remove(email);
            }
        };

        emitter.onCompletion(cleanup);
        emitter.onTimeout(cleanup);
        emitter.onError(e -> cleanup.run());

        // Send a keep-alive comment immediately to establish connection
        try {
            emitter.send(SseEmitter.event().comment("connected"));
        } catch (Exception ignored) {
        }

        return emitter;
    }

    // ──────────────────────────────────────────────────────────────
    // Create & push notification
    // ──────────────────────────────────────────────────────────────

    public void createAndPush(User user, NotificationTypeEnum type, String message) {
        Notification n = new Notification();
        n.setUser(user);
        n.setType(type);
        n.setMessage(message);
        n.setRead(false);
        notificationRepository.save(n);

        pushToUser(user.getEmail(), convertToDTO(n));
        sendFcmPush(user, type.name(), message);
    }

    private void pushToUser(String email, ResNotificationDTO dto) {
        List<SseEmitter> userEmitters = emitters.get(email);
        if (userEmitters == null || userEmitters.isEmpty()) return;

        List<SseEmitter> dead = new ArrayList<>();
        for (SseEmitter emitter : userEmitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("notification")
                        .data(dto));
            } catch (Exception e) {
                dead.add(emitter);
            }
        }
        userEmitters.removeAll(dead);
    }

    // ──────────────────────────────────────────────────────────────
    // Get notifications for current user
    // ──────────────────────────────────────────────────────────────

    public List<ResNotificationDTO> getMyNotifications(String email) throws IdInvalidException {
        User user = userService.handleGetUserByUsername(email);
        return notificationRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .filter(n -> !n.isDeletedByUser())
                .map(this::convertToDTO)
                .toList();
    }

    public void softDeleteByUser(Long id, String email) throws IdInvalidException {
        Notification n = notificationRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Thông báo không tồn tại"));
        if (!n.getUser().getEmail().equals(email)) {
            throw new IdInvalidException("Không có quyền");
        }
        n.setDeletedByUser(true);
        notificationRepository.save(n);
    }

    public long countUnread(String email) throws IdInvalidException {
        User user = userService.handleGetUserByUsername(email);
        return notificationRepository.countByUserAndIsReadFalse(user);
    }

    public void markAllRead(String email) throws IdInvalidException {
        User user = userService.handleGetUserByUsername(email);
        List<Notification> unread = notificationRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .filter(n -> !n.isRead())
                .toList();
        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
    }

    public void saveFcmToken(String email, String token) {
        userService.updateFcmToken(email, token);
    }

    public void markOneRead(Long id, String email) throws IdInvalidException {
        Notification n = notificationRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Thông báo không tồn tại"));
        if (!n.getUser().getEmail().equals(email)) {
            throw new IdInvalidException("Không có quyền");
        }
        n.setRead(true);
        notificationRepository.save(n);
    }

    // ──────────────────────────────────────────────────────────────
    // Scheduled: check match reminders every minute
    // ──────────────────────────────────────────────────────────────

    @Scheduled(fixedDelay = 60_000)
    public void sendMatchReminders() {
        LocalDateTime now = LocalDateTime.now();
        // Tìm các booking bắt đầu trong khoảng 15-16 phút tới
        LocalDateTime from = now.plusMinutes(15);
        LocalDateTime to = now.plusMinutes(16);

        List<Booking> upcoming = bookingRepository
                .findByStatusInAndStartDateTimeBetween(
                        List.of(BookingStatusEnum.ACTIVE, BookingStatusEnum.PAID),
                        from,
                        to);

        Instant alreadySentFrom = Instant.now().minusSeconds(120); // tránh gửi 2 lần
        Instant alreadySentTo = Instant.now();

        for (Booking booking : upcoming) {
            User user = booking.getUser();
            // Kiểm tra đã gửi nhắc nhở cho booking này chưa (trong 2 phút gần đây)
            boolean alreadySent = notificationRepository
                    .findByTypeAndCreatedAtBetween(NotificationTypeEnum.MATCH_REMINDER, alreadySentFrom, alreadySentTo)
                    .stream()
                    .anyMatch(n -> n.getUser().getId().equals(user.getId())
                            && n.getMessage().contains("#" + booking.getId()));

            if (!alreadySent) {
                String pitchName = booking.getPitch() != null ? booking.getPitch().getName() : "sân";
                String msg = String.format("Sắp đến giờ đá! Booking #%d – sân %s lúc %s",
                        booking.getId(), pitchName,
                        booking.getStartDateTime().toString().replace("T", " ").substring(0, 16));
                createAndPush(user, NotificationTypeEnum.MATCH_REMINDER, msg);
            }
        }
    }

    // ──────────────────────────────────────────────────────────────
    // FCM push notification
    // ──────────────────────────────────────────────────────────────

    private void sendFcmPush(User user, String title, String body) {
        if (user.getFcmToken() == null || user.getFcmToken().isBlank()) return;
        if (FirebaseApp.getApps().isEmpty()) return;
        try {
            Message msg = Message.builder()
                    .setToken(user.getFcmToken())
                    .setNotification(com.google.firebase.messaging.Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .build();
            FirebaseMessaging.getInstance().send(msg);
        } catch (Exception e) {
            log.warn("[FCM] Failed to send push to {}: {}", user.getEmail(), e.getMessage());
        }
    }

    // ──────────────────────────────────────────────────────────────
    // Convert
    // ──────────────────────────────────────────────────────────────

    private ResNotificationDTO convertToDTO(Notification n) {
        ResNotificationDTO dto = new ResNotificationDTO();
        dto.setId(n.getId());
        dto.setType(n.getType());
        dto.setMessage(n.getMessage());
        dto.setRead(n.isRead());
        dto.setDeletedByUser(n.isDeletedByUser());
        dto.setCreatedAt(n.getCreatedAt());
        return dto;
    }
}

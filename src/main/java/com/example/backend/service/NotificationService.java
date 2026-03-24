package com.example.backend.service;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

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
import com.example.backend.repository.UserRepository;
import com.example.backend.util.constant.booking.BookingStatusEnum;
import com.example.backend.util.constant.notification.NotificationTypeEnum;
import com.example.backend.util.constant.user.UserStatusEnum;
import com.example.backend.util.error.IdInvalidException;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;
    private final BookingRepository bookingRepository;
    private final UserService userService;
    private final UserRepository userRepository;

    // email -> list of emitters (một user có thể mở nhiều tab)
    private final Map<String, List<SseEmitter>> emitters = new ConcurrentHashMap<>();

    public NotificationService(NotificationRepository notificationRepository,
            BookingRepository bookingRepository,
            UserService userService,
            UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.bookingRepository = bookingRepository;
        this.userService = userService;
        this.userRepository = userRepository;
    }

    // ──────────────────────────────────────────────────────────────
    // Notify all active admins
    // ──────────────────────────────────────────────────────────────

    /** Gửi thông báo cho mọi admin đang hoạt động. */
    public void notifyAdmins(NotificationTypeEnum type, String message) {
        notifyAdmins(type, message, null);
    }

    /**
     * Gửi thông báo cho admin, bỏ qua email (tránh trùng khi khách đặt sân vừa có vai trò ADMIN đã nhận thông báo phía khách).
     */
    public void notifyAdmins(NotificationTypeEnum type, String message, String excludeEmail) {
        userRepository.findDistinctByRoles_Name("ADMIN").stream()
                .filter(admin -> admin.getStatus() == UserStatusEnum.ACTIVE)
                .filter(admin -> excludeEmail == null || excludeEmail.isBlank()
                        || !admin.getEmail().equalsIgnoreCase(excludeEmail.trim()))
                .forEach(admin -> createAndPush(admin, type, message));
    }

    // ──────────────────────────────────────────────────────────────
    // SSE subscription
    // ──────────────────────────────────────────────────────────────

    public SseEmitter subscribe(String email) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);

        emitters.computeIfAbsent(email, k -> new CopyOnWriteArrayList<>()).add(emitter);

        Runnable cleanup = () -> {
            List<SseEmitter> list = emitters.get(email);
            if (list != null) {
                list.remove(emitter);
                if (list.isEmpty())
                    emitters.remove(email, list);
            }
        };

        emitter.onCompletion(cleanup);
        emitter.onTimeout(cleanup);
        emitter.onError(e -> cleanup.run());

        // Send an initial event immediately so reverse proxies flush headers/body.
        try {
            emitter.send(SseEmitter.event().name("connected").data("ok"));
        } catch (Exception ignored) {
        }

        return emitter;
    }

    @Scheduled(fixedDelay = 25_000)
    public void sendSseKeepAlive() {
        // Ping SSE định kỳ; client đóng tab / refresh → kết nối gãy (IOException) là bình thường.
        // Lặp trên snapshot map + list để tránh xung đột với cleanup/onError; không để exception thoát ra @Scheduled (tránh stack đầy console).
        try {
            for (var entry : new ArrayList<>(emitters.entrySet())) {
                String email = entry.getKey();
                List<SseEmitter> userEmitters = entry.getValue();
                if (userEmitters == null || userEmitters.isEmpty()) {
                    continue;
                }

                List<SseEmitter> dead = new ArrayList<>();
                for (SseEmitter emitter : new ArrayList<>(userEmitters)) {
                    try {
                        emitter.send(SseEmitter.event().name("ping").data("keep-alive"));
                    } catch (IOException e) {
                        // Client đã ngắt (Windows: connection aborted by host) — chỉ dọn emitter.
                        dead.add(emitter);
                    } catch (IllegalStateException e) {
                        // Emitter đã complete/timeout.
                        dead.add(emitter);
                    } catch (Throwable t) {
                        dead.add(emitter);
                    }
                }

                for (SseEmitter d : dead) {
                    try {
                        d.complete();
                    } catch (Throwable ignored) {
                    }
                }
                userEmitters.removeAll(dead);
                if (userEmitters.isEmpty()) {
                    emitters.remove(email, userEmitters);
                }
            }
        } catch (Throwable t) {
            log.debug("[SSE] keep-alive batch skip: {}", t.toString());
        }
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

        // Chỉ đẩy tới người nhận; không gửi sự kiện "ring" cho người đang thao tác (tránh admin bị kêu chuông khi xác nhận/từ chối hộ khách).
        pushToUser(user.getEmail(), convertToDTO(n));
        sendFcmPush(user, resolvePushTitle(type), message);
    }

    private void pushToUser(String email, ResNotificationDTO dto) {
        List<SseEmitter> userEmitters = emitters.get(email);
        if (userEmitters == null || userEmitters.isEmpty())
            return;

        List<SseEmitter> dead = new ArrayList<>();
        for (SseEmitter emitter : userEmitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("notification")
                        .data(dto));
            } catch (Throwable e) {
                dead.add(emitter);
            }
        }
        userEmitters.removeAll(dead);
        if (userEmitters.isEmpty()) {
            emitters.remove(email, userEmitters);
        }
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

    /** Ẩn toàn bộ thông báo khỏi lịch sử người dùng (soft delete). */
    public void softDeleteAllForUser(String email) throws IdInvalidException {
        User user = userService.handleGetUserByUsername(email);
        List<Notification> list = notificationRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .filter(n -> !n.isDeletedByUser())
                .toList();
        list.forEach(n -> n.setDeletedByUser(true));
        notificationRepository.saveAll(list);
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
        sendMatchReminderAt(now, 30);
        sendMatchReminderAt(now, 15);
        sendMatchReminderAt(now, 10);
    }

    private void sendMatchReminderAt(LocalDateTime now, int minutesBefore) {
        LocalDateTime from = now.plusMinutes(minutesBefore);
        LocalDateTime to = from.plusMinutes(1);

        List<Booking> upcoming = bookingRepository
                .findByStatusInAndStartDateTimeBetween(
                        List.of(BookingStatusEnum.ACTIVE, BookingStatusEnum.PAID),
                        from,
                        to);

        if (upcoming.isEmpty()) {
            return;
        }

        Instant dedupeFrom = Instant.now().minusSeconds(6 * 60 * 60);
        Instant dedupeTo = Instant.now();
        List<Notification> recentReminders = notificationRepository
                .findByTypeAndCreatedAtBetween(NotificationTypeEnum.MATCH_REMINDER, dedupeFrom, dedupeTo);

        for (Booking booking : upcoming) {
            User user = booking.getUser();
            boolean alreadySent = recentReminders.stream().anyMatch(n -> n.getUser().getId().equals(user.getId())
                    && n.getMessage().contains("Booking #" + booking.getId())
                    && n.getMessage().contains("Còn " + minutesBefore + " phút"));

            if (alreadySent) {
                continue;
            }

            String pitchName = booking.getPitch() != null ? booking.getPitch().getName() : "san";
            String startAt = booking.getStartDateTime().toString().replace("T", " ").substring(0, 16);
            String msg = String.format("Còn %d phút đến giờ đá! Booking #%d - sân %s lúc %s",
                    minutesBefore,
                    booking.getId(),
                    pitchName,
                    startAt);

            createAndPush(user, NotificationTypeEnum.MATCH_REMINDER, msg);
        }
    }

    // ──────────────────────────────────────────────────────────────
    // FCM push notification
    // ──────────────────────────────────────────────────────────────

    private void sendFcmPush(User user, String title, String body) {
        if (user.getFcmToken() == null || user.getFcmToken().isBlank())
            return;
        try {
            if (FirebaseApp.getApps().isEmpty())
                return;
            Message msg = Message.builder()
                    .setToken(user.getFcmToken())
                    .setNotification(com.google.firebase.messaging.Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .build();
            FirebaseMessaging.getInstance().send(msg);
        } catch (NoClassDefFoundError | Exception e) {
            log.warn("[FCM] Push skipped for {}: {}", user.getEmail(), e.getMessage());
        }
    }

    private String resolvePushTitle(NotificationTypeEnum type) {
        return switch (type) {
            case BOOKING_CREATED -> "Dat san thanh cong";
            case BOOKING_PENDING_CONFIRMATION -> "Yeu cau dat san moi";
            case BOOKING_APPROVED -> "Booking da duoc xac nhan";
            case BOOKING_REJECTED -> "Booking bi tu choi";
            case PAYMENT_REQUESTED -> "Yeu cau thanh toan";
            case PAYMENT_PROOF_UPLOADED -> "Da tai len minh chung thanh toan";
            case PAYMENT_CONFIRMED -> "Thanh toan da xac nhan";
            case EQUIPMENT_BORROWED -> "Muon thiet bi";
            case EQUIPMENT_RETURNED -> "Tra thiet bi";
            case EQUIPMENT_LOST -> "Bao mat thiet bi";
            case EQUIPMENT_DAMAGED -> "Thiet bi bi hong";
            case MATCH_REMINDER -> "Nhac lich da bong";
            case AI_KEY_EXPIRED -> "Canh bao he thong";
        };
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

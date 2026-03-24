package com.example.backend.service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

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
import com.example.backend.util.SecurityUtil;
import com.example.backend.util.constant.booking.BookingStatusEnum;
import com.example.backend.util.constant.notification.NotificationTypeEnum;
import com.example.backend.util.constant.user.UserStatusEnum;
import com.example.backend.util.error.IdInvalidException;
import com.example.backend.websocket.NotificationSocketHandler;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;
    private final BookingRepository bookingRepository;
    private final UserService userService;
    private final UserRepository userRepository;
    private final NotificationSocketHandler notificationSocketHandler;

    public NotificationService(NotificationRepository notificationRepository,
            BookingRepository bookingRepository,
            UserService userService,
            UserRepository userRepository,
            NotificationSocketHandler notificationSocketHandler) {
        this.notificationRepository = notificationRepository;
        this.bookingRepository = bookingRepository;
        this.userService = userService;
        this.userRepository = userRepository;
        this.notificationSocketHandler = notificationSocketHandler;
    }

    // ──────────────────────────────────────────────────────────────
    // Notify all active admins
    // ──────────────────────────────────────────────────────────────

    public void notifyAdmins(NotificationTypeEnum type, String message) {
        userRepository.findDistinctByRoles_Name("ADMIN").stream()
                .filter(admin -> admin.getStatus() == UserStatusEnum.ACTIVE)
                .forEach(admin -> createAndPush(admin, type, message));
    }

    // ──────────────────────────────────────────────────────────────
    // Create & push notification
    // ──────────────────────────────────────────────────────────────

    public void createAndPush(User user, NotificationTypeEnum type, String message) {
        Notification n = new Notification();
        n.setUser(user);
        n.setType(type);
        n.setMessage(message);
        fillSenderSnapshot(n);
        n.setRead(false);
        notificationRepository.save(n);

        pushToUser(user.getEmail(), convertToDTO(n));
        pushRingToActor(user.getEmail());
        sendFcmPush(user, resolvePushTitle(type), message);
    }

    private void fillSenderSnapshot(Notification notification) {
        String actorEmail = SecurityUtil.getCurrentUserLogin().orElse("");
        if (actorEmail.isBlank()) {
            notification.setSenderName("Hệ thống");
            return;
        }
        User actor = userRepository.findByEmail(actorEmail);
        if (actor == null) {
            notification.setSenderName("Hệ thống");
            return;
        }
        notification.setSenderId(actor.getId());
        notification.setSenderName(actor.getFullName() != null && !actor.getFullName().isBlank() ? actor.getFullName()
                : actor.getName());
        notification.setSenderAvatarUrl(actor.getAvatarUrl());
    }

    private void pushRingToActor(String targetEmail) {
        String actorEmail = SecurityUtil.getCurrentUserLogin().orElse("");
        if (actorEmail.isBlank() || actorEmail.equalsIgnoreCase(targetEmail)) {
            return;
        }
        notificationSocketHandler.sendRingToUser(actorEmail);
    }

    private void pushToUser(String email, ResNotificationDTO dto) {
        notificationSocketHandler.sendNotificationToUser(email, dto);
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
        dto.setSenderId(n.getSenderId());
        dto.setSenderName(n.getSenderName());
        dto.setSenderAvatarUrl(n.getSenderAvatarUrl());
        dto.setRead(n.isRead());
        dto.setDeletedByUser(n.isDeletedByUser());
        dto.setCreatedAt(n.getCreatedAt());
        return dto;
    }
}

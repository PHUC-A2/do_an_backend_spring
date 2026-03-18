package com.example.backend.service;

import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.backend.domain.entity.AiApiKey;
import com.example.backend.domain.entity.User;
import com.example.backend.domain.request.ai.ReqAiKeyDTO;
import com.example.backend.domain.response.ai.ResAiKeyDTO;
import com.example.backend.repository.AiApiKeyRepository;
import com.example.backend.util.constant.ai.AiProviderEnum;
import com.example.backend.util.constant.notification.NotificationTypeEnum;
import com.example.backend.util.error.IdInvalidException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AiApiKeyService {

    private final AiApiKeyRepository keyRepo;
    private final NotificationService notificationService;
    private final UserService userService;

    @Value("${admin.email:admin@gmail.com}")
    private String adminEmail;

    // ─── CRUD ────────────────────────────────────────────────────────────────

    public ResAiKeyDTO addKey(ReqAiKeyDTO req) {
        AiApiKey key = new AiApiKey();
        key.setProvider(req.getProvider());
        key.setApiKey(req.getApiKey().trim());
        key.setLabel(req.getLabel() != null ? req.getLabel().trim() : null);
        key.setActive(true);
        return toDTO(keyRepo.save(key));
    }

    public void deleteKey(Long id) throws IdInvalidException {
        if (!keyRepo.existsById(id))
            throw new IdInvalidException("Key không tồn tại");
        keyRepo.deleteById(id);
    }

    public ResAiKeyDTO toggleActive(Long id) throws IdInvalidException {
        AiApiKey key = keyRepo.findById(id)
            .orElseThrow(() -> new IdInvalidException("Key không tồn tại"));
        key.setActive(!key.isActive());
        return toDTO(keyRepo.save(key));
    }

    public List<ResAiKeyDTO> listAll() {
        return keyRepo.findAll().stream()
            .sorted((a, b) -> {
                int cmp = a.getProvider().name().compareTo(b.getProvider().name());
                return cmp != 0 ? cmp : Long.compare(a.getId(), b.getId());
            })
            .map(this::toDTO)
            .toList();
    }

    public List<ResAiKeyDTO> listByProvider(AiProviderEnum provider) {
        return keyRepo.findByProviderOrderByIdAsc(provider).stream()
            .map(this::toDTO).toList();
    }

    // ─── Lấy key active để dùng, tự rotate (round-robin) ────────────────────

    /**
     * Lấy API key string của provider để gọi AI.
     * Cập nhật usageCount và lastUsedAt.
     * Trả null nếu không có key active.
     */
    public String getActiveKey(AiProviderEnum provider) {
        List<AiApiKey> activeKeys = keyRepo.findByProviderAndActiveTrueOrderByIdAsc(provider);
        if (activeKeys.isEmpty()) return null;

        // Chọn key có usageCount ít nhất (load balancing đơn giản)
        AiApiKey chosen = activeKeys.stream()
            .min((a, b) -> Long.compare(a.getUsageCount(), b.getUsageCount()))
            .get();

        chosen.setUsageCount(chosen.getUsageCount() + 1);
        chosen.setLastUsedAt(Instant.now());
        keyRepo.save(chosen);
        return chosen.getApiKey();
    }

    /**
     * Đánh dấu key bị lỗi (rate limit / expired) → tắt và thông báo admin.
     */
    public void markKeyFailed(AiProviderEnum provider, String failedKey) {
        keyRepo.findByProviderAndActiveTrueOrderByIdAsc(provider).stream()
            .filter(k -> k.getApiKey().equals(failedKey))
            .findFirst()
            .ifPresent(k -> {
                k.setActive(false);
                keyRepo.save(k);
            });

        long remaining = keyRepo.countByProviderAndActiveTrue(provider);
        notifyAdmins(provider, remaining);
    }

    // ─── Thông báo admin ─────────────────────────────────────────────────────

    private void notifyAdmins(AiProviderEnum provider, long remainingKeys) {
        try {
            User admin = userService.handleGetUserByUsername(adminEmail);
            if (admin == null) return;
            String msg = remainingKeys > 0
                ? "⚠️ Một key AI [" + provider.name() + "] đã hết hạn/lỗi. Còn " + remainingKeys + " key active. Vui lòng thêm key mới tại trang Quản lý AI."
                : "🚨 TẤT CẢ key AI [" + provider.name() + "] đã hết hạn! Hệ thống AI đang không hoạt động. Vui lòng thêm key ngay tại trang Quản lý AI.";
            notificationService.createAndPush(admin, NotificationTypeEnum.AI_KEY_EXPIRED, msg);
        } catch (Exception e) {
            // Không để lỗi thông báo ảnh hưởng luồng chính
        }
    }

    // ─── Helper ──────────────────────────────────────────────────────────────

    private ResAiKeyDTO toDTO(AiApiKey k) {
        return ResAiKeyDTO.builder()
            .id(k.getId())
            .provider(k.getProvider())
            .label(k.getLabel())
            .apiKeyMasked(maskKey(k.getApiKey()))
            .active(k.isActive())
            .usageCount(k.getUsageCount())
            .lastUsedAt(k.getLastUsedAt())
            .createdAt(k.getCreatedAt())
            .build();
    }

    private String maskKey(String key) {
        if (key == null || key.length() < 8) return "***";
        return key.substring(0, 8) + "***" + key.substring(key.length() - 4);
    }
}

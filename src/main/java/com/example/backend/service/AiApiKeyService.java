package com.example.backend.service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.backend.domain.entity.AiApiKey;
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
     * Đánh dấu key bị lỗi (401/429/hết hạn) → chỉ khi vừa tắt key đang active mới gửi thông báo (tránh spam).
     */
    public void markKeyFailed(AiProviderEnum provider, String failedKey) {
        if (failedKey == null || failedKey.isBlank()) {
            return;
        }
        // Tìm đúng bản ghi theo provider + chuỗi key (không lọc active) để biết đã tắt trước đó hay chưa
        Optional<AiApiKey> row = keyRepo.findFirstByProviderAndApiKeyOrderByIdAsc(provider, failedKey.trim());
        if (row.isEmpty()) {
            return;
        }
        AiApiKey k = row.get();
        if (!k.isActive()) {
            // Đã vô hiệu trước đó — không gửi lại thông báo
            return;
        }
        // Tắt key vừa gọi lỗi
        k.setActive(false);
        keyRepo.save(k);

        long remaining = keyRepo.countByProviderAndActiveTrue(provider);
        notifyAllAdminsKeyDisabled(k, remaining);
    }

    // ─── Thông báo admin ─────────────────────────────────────────────────────

    /** Gửi cho mọi admin đang hoạt động: nêu rõ provider, id, nhãn, mã che — để biết key nào cần thay. */
    private void notifyAllAdminsKeyDisabled(AiApiKey disabledKey, long remainingKeys) {
        try {
            String labelVi = providerLabelVi(disabledKey.getProvider());
            String labelPart = disabledKey.getLabel() != null && !disabledKey.getLabel().isBlank()
                ? "Nhãn: \"" + disabledKey.getLabel().trim() + "\". "
                : "";
            String masked = maskKey(disabledKey.getApiKey());
            String msg = remainingKeys > 0
                ? "⚠️ Key AI đã bị vô hiệu: " + labelVi + " (ID #" + disabledKey.getId() + "). "
                    + labelPart + "Mã: " + masked + ". Còn " + remainingKeys + " key đang bật cho nhà cung cấp này. Vào Quản lý AI để thêm key mới nếu cần."
                : "🚨 Key AI đã bị vô hiệu: " + labelVi + " (ID #" + disabledKey.getId() + "). "
                    + labelPart + "Mã: " + masked + ". Không còn key active cho " + labelVi + " — chat AI có thể ngừng hoạt động. Thêm key mới ngay tại Quản lý AI.";
            notificationService.notifyAdmins(NotificationTypeEnum.AI_KEY_EXPIRED, msg);
        } catch (Exception e) {
            // Không để lỗi thông báo ảnh hưởng luồng chính
        }
    }

    /** Tên hiển thị tiếng Việt/ngắn gọn để admin phân biệt Groq / Gemini / Cloudflare. */
    private String providerLabelVi(AiProviderEnum p) {
        return switch (p) {
            case GROQ -> "Groq";
            case GEMINI -> "Google Gemini";
            case CLOUDFLARE -> "Cloudflare AI";
        };
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

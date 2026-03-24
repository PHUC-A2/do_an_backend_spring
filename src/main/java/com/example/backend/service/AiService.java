package com.example.backend.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.backend.domain.entity.AiChatSession;
import com.example.backend.domain.entity.Booking;
import com.example.backend.domain.entity.Pitch;
import com.example.backend.domain.entity.User;
import com.example.backend.domain.request.ai.ReqChatDTO;
import com.example.backend.domain.response.ai.ResChatDTO;
import com.example.backend.domain.response.revenue.ResRevenueDashboardDTO;
import com.example.backend.repository.AiChatSessionRepository;
import com.example.backend.repository.BookingRepository;
import com.example.backend.repository.PitchRepository;
import com.example.backend.util.constant.ai.AiProviderEnum;
import com.example.backend.util.constant.booking.BookingStatusEnum;
import com.example.backend.util.constant.pitch.PitchStatusEnum;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Service
public class AiService {

    private static final Logger log = LoggerFactory.getLogger(AiService.class);
    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter T_FMT = DateTimeFormatter.ofPattern("HH:mm");

    @Value("${ai.groq.api-key:}")
    private String groqApiKey;
    @Value("${ai.groq.base-url:https://api.groq.com/openai}")
    private String groqBaseUrl;
    @Value("${ai.groq.model:llama-3.3-70b-versatile}")
    private String groqModel;
    @Value("${ai.gemini.api-key:}")
    private String geminiApiKey;
    @Value("${ai.gemini.model:gemini-2.5-flash}")
    private String geminiModel;
    @Value("${ai.cloudflare.api-token:}")
    private String cloudflareToken;
    @Value("${ai.cloudflare.account-id:}")
    private String cloudflareAccountId;
    @Value("${ai.cloudflare.model:@cf/meta/llama-3-8b-instruct}")
    private String cloudflareModel;
    @Value("${ai.chat.max-off-topic-per-day:15}")
    private int maxOffTopic;
    @Value("${ai.chat.max-on-topic-per-day:100}")
    private int maxOnTopic;
    @Value("${ai.chat.min-message-length:3}")
    private int minMessageLength;
    @Value("${ai.chat.duplicate-user-message-window:12}")
    private int duplicateMessageWindow;
    @Value("${ai.chat.duplicate-user-message-min-repeats:2}")
    private int duplicateMinRepeats;

    private final AiChatSessionRepository sessionRepo;
    private final BookingRepository bookingRepository;
    private final PitchRepository pitchRepository;
    private final RevenueService revenueService;
    private final AiApiKeyService aiApiKeyService;
    private final AiKnowledgeContextService aiKnowledgeContextService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    // ─── Phần tĩnh của system prompt ─────────────────────────────────────────
    private static final String BASE_SYSTEM_PROMPT = "Bạn là trợ lý AI của hệ thống TBU Sport — hệ thống đặt sân bóng đá trực tuyến của Trường Đại học Tây Bắc.\n"
            +
            "Luôn trả lời bằng tiếng Việt, thân thiện, ngắn gọn, rõ ràng.\n" +
            "\n" +
            "## THÔNG TIN HỆ THỐNG\n" +
            "- **Tên**: TBU Sport — đặt sân bóng đá trực tuyến, Trường ĐH Tây Bắc\n" +
            "- **Loại sân**: Sân 3 người (THREE), Sân 7 người (SEVEN)\n" +
            "- **Trạng thái sân**: ACTIVE (đang hoạt động), MAINTENANCE (đang bảo trì)\n" +
            "- **Trạng thái booking**: ACTIVE (đã xác nhận) → PAID (đã thanh toán) | CANCELLED (đã hủy)\n" +
            "- **Thanh toán**: Chuyển khoản Agribank hoặc tiền mặt tại quầy\n" +
            "- **Thông báo**: Nhận thông báo 15 phút trước giờ đá, khi đặt sân thành công, khi admin xác nhận thanh toán\n"
            +
            "- **Thiết bị cho mượn**: Bóng, áo, nón (thêm khi đặt sân hoặc cập nhật sau)\n" +
            "\n" +
            "## CÁCH SỬ DỤNG\n" +
            "1. **Xem & đặt sân**: Vào menu *Sân bóng* → chọn sân → xem timeline trống → điền form đặt lịch\n" +
            "2. **Xem lịch của tôi**: Vào *Lịch đặt của tôi* để xem, quản lý booking\n" +
            "3. **Mượn thiết bị**: Chọn thiết bị khi đặt sân, hoặc cập nhật sau trong mục booking\n" +
            "4. **Thanh toán**: Sau khi admin xác nhận → quét QR Agribank hoặc trả tiền mặt\n" +
            "5. **Hủy booking**: Liên hệ admin để hủy (hiện chưa có tự hủy)\n" +
            "\n" +
            "## NHIỆM VỤ CỦA BẠN\n" +
            "1. Trả lời câu hỏi về cách sử dụng hệ thống\n" +
            "2. Gợi ý sân phù hợp dựa trên nhu cầu (loại sân, giờ trống, giá)\n" +
            "3. Gợi ý sân VIP (sân tốt nhất / giá cao nhất) khi người dùng hỏi\n" +
            "4. Kiểm tra và thông báo sân nào đang trống hôm nay\n" +
            "5. Giải đáp thắc mắc về chính sách, giá cả, thiết bị\n" +
            "\n" +
            "## QUY TẮC\n" +
            "- Trả lời ngắn gọn, dùng emoji nhẹ nhàng khi cần\n" +
            "- Nếu câu hỏi không liên quan đến đặt sân/thể thao, nhẹ nhàng từ chối và hướng về chủ đề chính\n" +
            "- Không bịa đặt thông tin không có trong hệ thống\n" +
            "- Nếu không biết: *'Tôi không có thông tin này, vui lòng liên hệ admin để được hỗ trợ!'*\n" +
            "- Khi gợi ý đặt sân, luôn kèm link hoặc hướng dẫn vào trang /pitch\n" +
            "\n" +
            "## ƯU TIÊN TRẢ LỜI (GIẢM SPAM)\n" +
            "- Một câu trả lời đủ ý; tránh kéo dài hội thoại khi đã giải quyết xong.\n" +
            "- Nếu người dùng chỉ chào hỏi: chào lại ngắn và hỏi *một* câu gợi ý (vd: muốn đặt sân ngày nào).\n" +
            "- Không nhắc lại toàn bộ bảng số; chỉ trích số liệu liên quan câu hỏi.\n";

    private static final String OFF_TOPIC_REGEX = "(?i)(thoi tiet|weather|nau an|recipe|phim|movie|am nhac|music|toan hoc|"
            +
            "lap trinh|programming|crypto|bitcoin|forex|chung khoan|stock|" +
            "chinh tri|politics|tinh yeu|love|hen ho|dating|game|gaming|anime|manga)";

    public AiService(AiChatSessionRepository sessionRepo,
            BookingRepository bookingRepository,
            PitchRepository pitchRepository,
            RevenueService revenueService,
            AiApiKeyService aiApiKeyService,
            AiKnowledgeContextService aiKnowledgeContextService) {
        this.sessionRepo = sessionRepo;
        this.bookingRepository = bookingRepository;
        this.pitchRepository = pitchRepository;
        this.revenueService = revenueService;
        this.aiApiKeyService = aiApiKeyService;
        this.aiKnowledgeContextService = aiKnowledgeContextService;
    }

    // ─── Client chat ─────────────────────────────────────────────────────────
    public ResChatDTO chat(ReqChatDTO req, User user) {
        String raw = req.getMessage() != null ? req.getMessage() : "";
        String trimmed = raw.trim();
        if (trimmed.length() < minMessageLength) {
            return new ResChatDTO(
                    "Vui lòng nhập câu hỏi rõ ràng (ít nhất " + minMessageLength
                            + " ký tự, không tính khoảng trắng thừa) để mình hỗ trợ chính xác nhé!",
                    "SYSTEM", false, getRemainingMessages(user));
        }
        if (isDuplicateUserSpam(req.getHistory(), trimmed)) {
            return new ResChatDTO(
                    "Bạn đã gửi cùng nội dung nhiều lần liên tiếp. Hãy gộp ý trong một tin nhắn hoặc nêu rõ thêm chi tiết "
                            + "(sân, ngày giờ, vấn đề) để tránh spam và được trả lời tốt hơn.",
                    "SYSTEM", false, getRemainingMessages(user));
        }

        String today = LocalDate.now().toString();
        AiChatSession session = getOrCreateSession(user, today);
        boolean offTopic = isOffTopic(trimmed);

        if (offTopic && session.getOffTopicCount() >= maxOffTopic) {
            return new ResChatDTO(
                    "Bạn đã vượt quá giới hạn " + maxOffTopic + " câu hỏi không liên quan trong ngày. " +
                            "Vui lòng hỏi về cách đặt sân, lịch trống hoặc tính năng của TBU Sport nhé!",
                    "SYSTEM", true, 0);
        }
        if (session.getTotalMessageCount() >= maxOnTopic) {
            return new ResChatDTO(
                    "Bạn đã đạt giới hạn " + maxOnTopic + " tin nhắn trong ngày. Vui lòng thử lại vào ngày mai!",
                    "SYSTEM", false, 0);
        }

        String systemPrompt = buildClientSystemPrompt();
        String enriched = offTopic ? trimmed : enrichWithContext(trimmed, user);
        String[] providerUsed = { "" };
        String reply = callWithFallback(buildMessagesWithCustomSystem(req.getHistory(), enriched, systemPrompt),
                providerUsed);

        session.setTotalMessageCount(session.getTotalMessageCount() + 1);
        if (offTopic)
            session.setOffTopicCount(session.getOffTopicCount() + 1);
        sessionRepo.save(session);

        int remaining = offTopic
                ? maxOffTopic - session.getOffTopicCount()
                : maxOnTopic - session.getTotalMessageCount();

        return new ResChatDTO(reply, providerUsed[0], offTopic, remaining);
    }

    // ─── Admin chat ───────────────────────────────────────────────────────────
    public ResChatDTO adminChat(ReqChatDTO req, User user) {
        String raw = req.getMessage() != null ? req.getMessage() : "";
        String trimmed = raw.trim();
        if (trimmed.length() < minMessageLength) {
            return new ResChatDTO(
                    "Nhập yêu cầu ít nhất " + minMessageLength + " ký tự (ví dụ: “Tóm tắt booking tuần này”).",
                    "SYSTEM", false, 999);
        }
        if (isDuplicateUserSpam(req.getHistory(), trimmed)) {
            return new ResChatDTO(
                    "Nội dung trùng lặp nhiều lần — vui lòng gộp câu hỏi hoặc bổ sung chi tiết.",
                    "SYSTEM", false, 999);
        }

        String systemPrompt = buildAdminSystemPrompt();
        String enriched = trimmed + "\n\n[Thời gian: " +
                LocalDateTime.now().format(DT_FMT) + "]";
        String[] providerUsed = { "" };
        String reply = callWithFallback(buildMessagesWithCustomSystem(req.getHistory(), enriched, systemPrompt),
                providerUsed);
        return new ResChatDTO(reply, providerUsed[0], false, 999);
    }

    // ─── Xây system prompt có dữ liệu sân thực tế ────────────────────────────
    private String buildClientSystemPrompt() {
        StringBuilder prompt = new StringBuilder(BASE_SYSTEM_PROMPT);
        prompt.append("\n").append(aiKnowledgeContextService.getFeatureCatalogMarkdown());
        prompt.append("\n").append(aiKnowledgeContextService.buildAggregatedSnapshotMarkdown());
        appendPitchInfo(prompt);
        appendAvailablePitchesToday(prompt);
        return prompt.toString();
    }

    private String buildAdminSystemPrompt() {
        StringBuilder prompt = new StringBuilder(BASE_SYSTEM_PROMPT);
        prompt.append("\n").append(aiKnowledgeContextService.getFeatureCatalogMarkdown());
        prompt.append("\n").append(aiKnowledgeContextService.buildAggregatedSnapshotMarkdown());
        appendPitchInfo(prompt);
        appendAvailablePitchesToday(prompt);

        prompt.append("\n## BÁO CÁO THỐNG KÊ HIỆN TẠI\n");
        try {
            LocalDate today = LocalDate.now();
            ResRevenueDashboardDTO stats = revenueService.getDashboard(today.minusDays(30), today);
            prompt.append(String.format(
                    "- Doanh thu hôm nay: **%s VND**\n" +
                            "- Doanh thu tuần này: **%s VND**\n" +
                            "- Doanh thu tháng này: **%s VND**\n" +
                            "- Tổng booking: %d | Đã thanh toán: %d | Đã hủy: %d\n" +
                            "- Tổng người dùng: %d | Tổng sân: %d\n",
                    stats.getTodayRevenue() != null ? formatMoney(stats.getTodayRevenue().longValue()) : "0",
                    stats.getWeekRevenue() != null ? formatMoney(stats.getWeekRevenue().longValue()) : "0",
                    stats.getMonthRevenue() != null ? formatMoney(stats.getMonthRevenue().longValue()) : "0",
                    stats.getTotalBookings(), stats.getPaidBookings(), stats.getCancelledBookings(),
                    stats.getTotalUsers(), stats.getTotalPitches()));
        } catch (Exception e) {
            prompt.append("- Không thể tải thống kê lúc này.\n");
        }

        prompt.append("\n## VAI TRÒ ADMIN\n")
                .append("- Bạn có thể phân tích thống kê, doanh thu, xu hướng booking\n")
                .append("- Gợi ý các biện pháp tăng doanh thu, tối ưu lịch sân\n")
                .append("- Trả lời mọi câu hỏi của admin (không giới hạn chủ đề)\n")
                .append("- Nếu cần thông tin chi tiết hơn, đề nghị admin xem trang Thống kê\n")
                .append("- Vẫn tuân thủ: không tiết lộ mật khẩu, token, OTP, mã payment, URL chứng từ, API key.\n");

        return prompt.toString();
    }

    /** Thêm danh sách sân thực tế từ DB vào prompt */
    private void appendPitchInfo(StringBuilder prompt) {
        try {
            List<Pitch> pitches = pitchRepository.findAll();
            if (pitches.isEmpty())
                return;

            prompt.append("\n## DANH SÁCH SÂN TRONG HỆ THỐNG\n");

            // Sân VIP = sân giá cao nhất đang ACTIVE
            Pitch vip = pitches.stream()
                    .filter(p -> p.getStatus() == PitchStatusEnum.ACTIVE && p.getPricePerHour() != null)
                    .max((a, b) -> a.getPricePerHour().compareTo(b.getPricePerHour()))
                    .orElse(null);

            for (Pitch p : pitches) {
                boolean isVip = vip != null && vip.getId().equals(p.getId());
                String status = p.getStatus() == PitchStatusEnum.ACTIVE ? "✅ Hoạt động" : "🔧 Bảo trì";
                String type = "SEVEN".equals(p.getPitchType() != null ? p.getPitchType().name() : "") ? "Sân 7 người"
                        : "Sân 3 người";
                String hours = p.isOpen24h() ? "24/7"
                        : (p.getOpenTime() != null && p.getCloseTime() != null
                                ? p.getOpenTime().format(T_FMT) + " - " + p.getCloseTime().format(T_FMT)
                                : "Chưa rõ");
                String price = p.getPricePerHour() != null
                        ? formatMoney(p.getPricePerHour().longValue()) + " VND/giờ"
                        : "Liên hệ";

                prompt.append(String.format("- **%s**%s [%s] | %s | %s | Giờ mở: %s\n",
                        p.getName(),
                        isVip ? " ⭐ VIP" : "",
                        status,
                        type,
                        price,
                        hours));
                if (p.getAddress() != null && !p.getAddress().isBlank()) {
                    prompt.append("  Địa chỉ: ").append(p.getAddress()).append("\n");
                }
            }

            if (vip != null) {
                prompt.append("\n**Sân VIP được đề xuất**: ").append(vip.getName())
                        .append(" — ").append(formatMoney(vip.getPricePerHour().longValue()))
                        .append(" VND/giờ. Đây là sân chất lượng cao nhất hệ thống, ưu tiên gợi ý khi người dùng hỏi sân tốt.\n");
            }
        } catch (Exception e) {
            log.warn("[AI] Cannot load pitch info: {}", e.getMessage());
        }
    }

    /** Thêm thông tin sân trống hôm nay vào prompt */
    private void appendAvailablePitchesToday(StringBuilder prompt) {
        try {
            List<Pitch> activePitches = pitchRepository.findAll().stream()
                    .filter(p -> p.getStatus() == PitchStatusEnum.ACTIVE)
                    .toList();
            if (activePitches.isEmpty())
                return;

            LocalDateTime now = LocalDateTime.now();
            LocalDateTime todayEnd = now.toLocalDate().atTime(LocalTime.MAX);

            List<String> freePitches = new ArrayList<>();
            List<String> busyPitches = new ArrayList<>();

            for (Pitch p : activePitches) {
                // Lấy các booking đang chiếm sân từ bây giờ đến cuối ngày
                List<Booking> active = bookingRepository
                        .findByPitchIdAndStatusInAndStartDateTimeLessThanAndEndDateTimeGreaterThanOrderByStartDateTimeAsc(
                                p.getId(),
                                List.of(BookingStatusEnum.PENDING, BookingStatusEnum.ACTIVE, BookingStatusEnum.PAID),
                                todayEnd,
                                now);

                if (active.isEmpty()) {
                    freePitches.add(p.getName());
                } else {
                    // Tìm khung giờ trống xen kẽ
                    List<String> busySlots = new ArrayList<>();
                    for (Booking b : active) {
                        busySlots.add(b.getStartDateTime().format(T_FMT) + "-" + b.getEndDateTime().format(T_FMT));
                    }
                    busyPitches.add(p.getName() + " (bận: " + String.join(", ", busySlots) + ")");
                }
            }

            prompt.append("\n## TÌNH TRẠNG SÂN HÔM NAY (").append(now.format(DT_FMT)).append(")\n");
            if (!freePitches.isEmpty()) {
                prompt.append("**Sân đang trống cả ngày**: ").append(String.join(", ", freePitches)).append("\n");
            }
            if (!busyPitches.isEmpty()) {
                prompt.append("**Sân đã có lịch**:\n");
                for (String s : busyPitches) {
                    prompt.append("  - ").append(s).append("\n");
                }
            }
            prompt.append("→ Khi người dùng hỏi sân trống, hãy ưu tiên gợi ý các sân đang trống ở trên.\n");
        } catch (Exception e) {
            log.warn("[AI] Cannot load availability: {}", e.getMessage());
        }
    }

    // ─── Enrich user message với context cá nhân ─────────────────────────────
    private String enrichWithContext(String message, User user) {
        StringBuilder ctx = new StringBuilder(message);
        ctx.append("\n\n[Thời gian hiện tại: ").append(LocalDateTime.now().format(DT_FMT)).append("]");

        if (user != null) {
            try {
                List<Booking> upcoming = bookingRepository.findByStatusInAndStartDateTimeBetween(
                        List.of(BookingStatusEnum.ACTIVE, BookingStatusEnum.PAID),
                        LocalDateTime.now(), LocalDateTime.now().plusDays(7));
                List<Booking> mine = upcoming.stream()
                        .filter(b -> b.getUser() != null && b.getUser().getId().equals(user.getId()))
                        .toList();
                if (!mine.isEmpty()) {
                    ctx.append("\n[Lịch sắp tới của bạn: ");
                    for (Booking b : mine) {
                        ctx.append(b.getPitch() != null ? b.getPitch().getName() : "Sân")
                                .append(" lúc ").append(b.getStartDateTime().format(DT_FMT))
                                .append("; ");
                    }
                    ctx.append("]");
                }
            } catch (Exception ignored) {
            }
        }
        return ctx.toString();
    }

    private boolean isOffTopic(String message) {
        return message.matches(".*" + OFF_TOPIC_REGEX + ".*");
    }

    private static final Pattern SPACES = Pattern.compile("\\s+");

    /** Chuẩn hóa để so sánh tin nhắn lặp. */
    private static String normalizeForDuplicate(String s) {
        if (s == null) {
            return "";
        }
        return SPACES.matcher(s.trim().toLowerCase()).replaceAll(" ").strip();
    }

    /**
     * True nếu trong cửa sổ lịch sử gần đây đã có ít nhất {@link #duplicateMinRepeats} tin user trùng nội dung
     * (tin hiện tại là lần lặp thứ 3 trở lên).
     */
    private boolean isDuplicateUserSpam(List<ReqChatDTO.MessageDTO> history, String currentMessage) {
        String cur = normalizeForDuplicate(currentMessage);
        if (cur.length() < 8) {
            return false;
        }
        if (history == null || history.isEmpty()) {
            return false;
        }
        int repeat = 0;
        int start = Math.max(0, history.size() - duplicateMessageWindow);
        for (int i = history.size() - 1; i >= start; i--) {
            ReqChatDTO.MessageDTO m = history.get(i);
            if (m == null || m.getRole() == null || m.getContent() == null) {
                continue;
            }
            if (!"user".equalsIgnoreCase(m.getRole().trim())) {
                continue;
            }
            if (cur.equals(normalizeForDuplicate(m.getContent()))) {
                repeat++;
                if (repeat >= duplicateMinRepeats) {
                    return true;
                }
            }
        }
        return false;
    }

    private List<ObjectNode> buildMessagesWithCustomSystem(List<ReqChatDTO.MessageDTO> history,
            String userMessage, String systemPrompt) {
        List<ObjectNode> messages = new ArrayList<>();
        ObjectNode sys = objectMapper.createObjectNode();
        sys.put("role", "system");
        sys.put("content", systemPrompt);
        messages.add(sys);

        if (history != null) {
            int start = Math.max(0, history.size() - 10);
            for (int i = start; i < history.size(); i++) {
                ReqChatDTO.MessageDTO h = history.get(i);
                ObjectNode m = objectMapper.createObjectNode();
                m.put("role", h.getRole());
                m.put("content", h.getContent());
                messages.add(m);
            }
        }

        ObjectNode cur = objectMapper.createObjectNode();
        cur.put("role", "user");
        cur.put("content", userMessage);
        messages.add(cur);
        return messages;
    }

    // ─── AI provider calls ────────────────────────────────────────────────────

    /**
     * Lấy key theo thứ tự ưu tiên: DB (active) → env fallback.
     */
    private String resolveKey(AiProviderEnum provider, String envKey) {
        String dbKey = aiApiKeyService.getActiveKey(provider);
        return (dbKey != null && !dbKey.isBlank()) ? dbKey : envKey;
    }

    private String callWithFallback(List<ObjectNode> messages, String[] used) {
        // GROQ
        String groqKey = resolveKey(AiProviderEnum.GROQ, groqApiKey);
        if (groqKey != null && !groqKey.isBlank()) {
            try {
                String r = callGroq(messages, groqKey);
                if (r != null) {
                    used[0] = "GROQ";
                    return r;
                }
                // status != 200 nhưng không throw → coi là lỗi key
                aiApiKeyService.markKeyFailed(AiProviderEnum.GROQ, groqKey);
            } catch (Exception e) {
                log.warn("[AI] Groq failed: {}", e.getMessage());
                aiApiKeyService.markKeyFailed(AiProviderEnum.GROQ, groqKey);
            }
        }
        // GEMINI
        String gemKey = resolveKey(AiProviderEnum.GEMINI, geminiApiKey);
        if (gemKey != null && !gemKey.isBlank()) {
            try {
                String r = callGemini(messages, gemKey);
                if (r != null) {
                    used[0] = "GEMINI";
                    return r;
                }
                aiApiKeyService.markKeyFailed(AiProviderEnum.GEMINI, gemKey);
            } catch (Exception e) {
                log.warn("[AI] Gemini failed: {}", e.getMessage());
                aiApiKeyService.markKeyFailed(AiProviderEnum.GEMINI, gemKey);
            }
        }
        // CLOUDFLARE
        String cfKey = resolveKey(AiProviderEnum.CLOUDFLARE, cloudflareToken);
        if (cfKey != null && !cfKey.isBlank()) {
            try {
                String r = callCloudflare(messages, cfKey);
                if (r != null) {
                    used[0] = "CLOUDFLARE";
                    return r;
                }
                aiApiKeyService.markKeyFailed(AiProviderEnum.CLOUDFLARE, cfKey);
            } catch (Exception e) {
                log.warn("[AI] Cloudflare failed: {}", e.getMessage());
                aiApiKeyService.markKeyFailed(AiProviderEnum.CLOUDFLARE, cfKey);
            }
        }
        used[0] = "NONE";
        return "Xin lỗi, hệ thống AI đang bận. Vui lòng thử lại sau ít phút!";
    }

    private String callGroq(List<ObjectNode> messages, String key) throws Exception {
        ObjectNode body = objectMapper.createObjectNode();
        body.put("model", groqModel);
        body.put("max_tokens", 1024);
        ArrayNode arr = body.putArray("messages");
        messages.forEach(arr::add);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(groqBaseUrl + "/v1/chat/completions"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + key)
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
                .build();

        HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() == 429 || resp.statusCode() == 401) {
            log.warn("[AI] Groq key lỗi status {}", resp.statusCode());
            return null; // trigger markKeyFailed
        }
        if (resp.statusCode() != 200) {
            log.warn("[AI] Groq status {}", resp.statusCode());
            return null;
        }
        JsonNode json = objectMapper.readTree(resp.body());
        return json.path("choices").path(0).path("message").path("content").asText(null);
    }

    private String callGemini(List<ObjectNode> messages, String key) throws Exception {
        String systemText = messages.stream()
                .filter(m -> "system".equals(m.path("role").asText()))
                .findFirst().map(m -> m.path("content").asText()).orElse("");

        ObjectNode body = objectMapper.createObjectNode();
        if (!systemText.isBlank()) {
            body.putObject("system_instruction")
                    .putArray("parts").addObject().put("text", systemText);
        }
        ArrayNode contents = body.putArray("contents");
        for (ObjectNode m : messages) {
            if ("system".equals(m.path("role").asText()))
                continue;
            String role = "user".equals(m.path("role").asText()) ? "user" : "model";
            ObjectNode c = contents.addObject();
            c.put("role", role);
            c.putArray("parts").addObject().put("text", m.path("content").asText());
        }

        String url = "https://generativelanguage.googleapis.com/v1beta/models/" +
                geminiModel + ":generateContent?key=" + key;
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
                .build();

        HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() == 429 || resp.statusCode() == 401 || resp.statusCode() == 400) {
            log.warn("[AI] Gemini key lỗi status {}", resp.statusCode());
            return null;
        }
        if (resp.statusCode() != 200) {
            log.warn("[AI] Gemini status {}", resp.statusCode());
            return null;
        }
        JsonNode json = objectMapper.readTree(resp.body());
        return json.path("candidates").path(0).path("content").path("parts").path(0).path("text").asText(null);
    }

    private String callCloudflare(List<ObjectNode> messages, String key) throws Exception {
        ObjectNode body = objectMapper.createObjectNode();
        ArrayNode arr = body.putArray("messages");
        messages.forEach(arr::add);

        String url = "https://api.cloudflare.com/client/v4/accounts/" +
                cloudflareAccountId + "/ai/run/" + cloudflareModel;
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + key)
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
                .build();

        HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() == 429 || resp.statusCode() == 401) {
            log.warn("[AI] Cloudflare key lỗi status {}", resp.statusCode());
            return null;
        }
        if (resp.statusCode() != 200) {
            log.warn("[AI] Cloudflare status {}", resp.statusCode());
            return null;
        }
        JsonNode json = objectMapper.readTree(resp.body());
        return json.path("result").path("response").asText(null);
    }

    // ─── Session ──────────────────────────────────────────────────────────────
    private AiChatSession getOrCreateSession(User user, String today) {
        Optional<AiChatSession> existing = user != null
                ? sessionRepo.findByUserAndSessionDate(user, today)
                : sessionRepo.findByUserIsNullAndSessionDate(today);
        if (existing.isPresent())
            return existing.get();
        AiChatSession s = new AiChatSession();
        s.setUser(user);
        s.setSessionDate(today);
        return sessionRepo.save(s);
    }

    public int getRemainingMessages(User user) {
        String today = LocalDate.now().toString();
        Optional<AiChatSession> s = user != null
                ? sessionRepo.findByUserAndSessionDate(user, today)
                : sessionRepo.findByUserIsNullAndSessionDate(today);
        return s.isEmpty() ? maxOnTopic : Math.max(0, maxOnTopic - s.get().getTotalMessageCount());
    }

    // ─── Helper ───────────────────────────────────────────────────────────────
    private String formatMoney(long amount) {
        return String.format("%,d", amount).replace(',', '.');
    }
}

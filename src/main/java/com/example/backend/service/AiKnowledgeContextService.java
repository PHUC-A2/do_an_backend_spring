package com.example.backend.service;

import java.math.RoundingMode;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.domain.response.dashboard.ResAdminSystemOverviewDTO;

import lombok.RequiredArgsConstructor;

/**
 * Cung cấp ngữ cảnh nghiệp vụ + snapshot thống kê đã lọc (không PII) cho prompt chatbot.
 * Không truyền mật khẩu, token, OTP, mã giao dịch chi tiết, URL chứng từ, API key.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AiKnowledgeContextService {

    private final AdminDashboardService adminDashboardService;

    /** Mô tả chức năng để model “hiểu” hệ thống, trả lời đúng trọng tâm, giảm hỏi lan man. */
    private static final String FEATURE_CATALOG = """
            ## DANH MỤC CHỨC NĂNG TBU SPORT (chỉ mô tả nghiệp vụ, không phải hướng dẫn bảo mật)
            ### Phía khách (client)
            - Đăng ký / xác minh email / đăng nhập / quên mật khẩu.
            - Xem danh sách sân, chi tiết sân, timeline khung giờ trống theo ngày.
            - Tạo booking đặt sân, cập nhật booking (theo luồng cho phép), xem lịch sử đặt.
            - Thanh toán: tạo yêu cầu thanh toán, tải QR / chuyển khoản, chờ admin xác nhận; có thanh toán tiền mặt.
            - Mượn thiết bị kèm booking sân (thiết bị lưu động), biên bản mượn/trả, trạng thái dòng thiết bị.
            - Thông báo trong hệ thống (đặt sân, thanh toán, nhắc trận…).
            - Đánh giá sân / nhận xét, chat trao đổi với admin trên từng đánh giá (nếu được bật).
            - Trợ lý AI chat (giới hạn tin nhắn/ngày; ưu tiên câu hỏi liên quan đặt sân & TBU Sport).

            ### Phía quản trị (admin)
            - Quản lý người dùng, vai trò, quyền (RBAC).
            - Quản lý sân, thiết bị gắn sân, thiết bị kho (equipments), giá giờ, trạng thái sân.
            - Quản lý booking, duyệt/từ chối (nếu có luồng), xem thanh toán, xác nhận thanh toán.
            - Quản lý mượn/trả thiết bị theo booking, xác nhận biên bản trả.
            - Quản lý đánh giá & hỗ trợ / thống kê doanh thu (dashboard).
            - Cấu hình khóa API AI (provider), không tiết lộ giá trị key cho người dùng.

            ## QUY TẮC BẢO MẬT KHI TRẢ LỜI
            - Tuyệt đối không đưa: mật khẩu, refresh token, OTP, nội dung email riêng user khác, mã paymentCode, URL ảnh CK, chuỗi API key.
            - Chỉ được dùng các **số đếm / tổng hợp** trong khối “DỮ LIỆU TỔNG HỢP” bên dưới và mô tả sân đã được cung cấp.
            - Nếu user hỏi dữ liệu cá nhân người khác: từ chối và hướng dẫn liên hệ admin.

            ## HẠN CHẾ SPAM / CHAT VÔ NGHĨA
            - Trả lời ngắn gọn, đủ ý; không khuyến khích đối thoại dài khi đã trả lời xong.
            - Nếu câu hỏi quá mơ hồ: yêu cầu user nêu rõ (sân nào, ngày giờ, vấn đề gì).
            - Không lặp lại nguyên văn khối dữ liệu kỹ thuật; diễn giải cho người dùng phổ thông.
            """;

    public String getFeatureCatalogMarkdown() {
        return FEATURE_CATALOG;
    }

    /**
     * Snapshot chỉ gồm số đếm & tổng tiền pending (không danh sách user/booking chi tiết).
     */
    public String buildAggregatedSnapshotMarkdown() {
        try {
            ResAdminSystemOverviewDTO o = adminDashboardService.getSystemOverview();
            StringBuilder sb = new StringBuilder(2048);
            sb.append("## DỮ LIỆU TỔNG HỢP TỪ CSDL (đã loại trừ trường nhạy cảm — chỉ thống kê)\n");
            sb.append("- Cập nhật snapshot (epoch ms): ").append(o.getGeneratedAt()).append("\n");
            sb.append("### Người dùng & phân quyền\n");
            sb.append(String.format(
                    "- Tổng tài khoản: %d | Hoạt động: %d | Không HĐ: %d | Chờ xác minh: %d | Cấm: %d | Đã xóa/lưu trữ: %d\n",
                    o.getUsersTotal(), o.getUsersActive(), o.getUsersInactive(), o.getUsersPendingVerification(),
                    o.getUsersBanned(), o.getUsersDeleted()));
            sb.append(String.format("- Vai trò: %d | Quyền: %d\n", o.getRolesTotal(), o.getPermissionsTotal()));
            sb.append("### Booking & thanh toán\n");
            sb.append(String.format(
                    "- Booking (chưa ẩn bởi user): %d | Chờ: %d | Đang đặt: %d | Trạng thái PAID: %d | Hủy: %d\n",
                    o.getBookingsTotalVisible(), o.getBookingsPending(), o.getBookingsActive(),
                    o.getBookingsPaidStatus(), o.getBookingsCancelled()));
            sb.append(String.format(
                    "- Giao dịch TT: %d | Chờ: %d | Đã TT: %d | Hủy: %d | Tổng tiền đang chờ xác nhận (VND): %s\n",
                    o.getPaymentsTotal(), o.getPaymentsPendingCount(), o.getPaymentsPaidCount(),
                    o.getPaymentsCancelledCount(),
                    o.getPaymentsPendingAmount() != null
                            ? o.getPaymentsPendingAmount().setScale(0, RoundingMode.HALF_UP).toPlainString()
                            : "0"));
            sb.append("### Sân & thiết bị\n");
            sb.append(String.format(
                    "- Sân: %d | ACTIVE: %d | Bảo trì: %d\n",
                    o.getPitchesTotal(), o.getPitchesActive(), o.getPitchesMaintenance()));
            sb.append(String.format(
                    "- Thiết bị kho (dòng): %d | ACTIVE/MAINT/INACTIVE/BROKEN/LOST: %d/%d/%d/%d/%d\n",
                    o.getEquipmentsTotal(), o.getEquipmentsActive(), o.getEquipmentsMaintenance(),
                    o.getEquipmentsInactive(), o.getEquipmentsBroken(), o.getEquipmentsLost()));
            sb.append(String.format(
                    "- Gán TB–sân (dòng): %d | Dòng mượn TB booking: %d (mượn/trả/mất/hỏng: %d/%d/%d/%d) | Chờ admin xác nhận trả: %d\n",
                    o.getPitchEquipmentLinks(), o.getBookingEquipmentsTotal(),
                    o.getBookingEquipmentsBorrowed(), o.getBookingEquipmentsReturned(),
                    o.getBookingEquipmentsLost(), o.getBookingEquipmentsDamaged(),
                    o.getBookingEquipmentsAwaitingAdminConfirm()));
            sb.append(String.format("- Nhật ký mượn/trả TB: %d\n", o.getEquipmentBorrowLogsTotal()));
            sb.append("### Đánh giá, thông báo, AI\n");
            sb.append(String.format(
                    "- Đánh giá: %d (chờ/duyệt/ẩn: %d/%d/%d) | Tin nhắn chat đánh giá: %d\n",
                    o.getReviewsTotal(), o.getReviewsPending(), o.getReviewsApproved(), o.getReviewsHidden(),
                    o.getReviewMessagesTotal()));
            sb.append(String.format(
                    "- Thông báo: %d | Chưa đọc (toàn hệ thống): %d\n",
                    o.getNotificationsTotal(), o.getNotificationsUnread()));
            sb.append(String.format(
                    "- Khóa AI (chỉ số lượng): tổng %d | đang bật %d | Phiên chat AI: %d\n",
                    o.getAiApiKeysTotal(), o.getAiApiKeysActive(), o.getAiChatSessionsTotal()));
            sb.append(
                    "→ Dùng các con số trên để trả lời câu hỏi dạng “có bao nhiêu…”, “hệ thống đang thế nào”; không bịa thêm.\n");
            return sb.toString();
        } catch (Exception e) {
            return "## DỮ LIỆU TỔNG HỢP\n- (Không tải được snapshot thống kê lúc này.)\n";
        }
    }
}

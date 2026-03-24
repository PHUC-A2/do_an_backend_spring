package com.example.backend.domain.response.dashboard;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO tổng hợp số liệu toàn hệ thống cho dashboard admin (đọc từ các bảng hiện có).
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResAdminSystemOverviewDTO {

    /** Thời điểm sinh báo cáo (epoch millis, server). */
    private long generatedAt;

    // --- Người dùng ---
    private long usersTotal;
    private long usersActive;
    private long usersInactive;
    private long usersPendingVerification;
    private long usersBanned;
    private long usersDeleted;

    // --- Booking (không bị user xóa khỏi lịch sử) ---
    private long bookingsTotalVisible;
    private long bookingsPending;
    private long bookingsActive;
    private long bookingsPaidStatus;
    private long bookingsCancelled;

    // --- Thanh toán ---
    private long paymentsTotal;
    private long paymentsPendingCount;
    private long paymentsPaidCount;
    private long paymentsCancelledCount;
    /** Tổng số tiền các giao dịch đang PENDING (chưa xác nhận). */
    private BigDecimal paymentsPendingAmount;

    // --- Sân ---
    private long pitchesTotal;
    private long pitchesActive;
    private long pitchesMaintenance;

    // --- Thiết bị kho (equipments) ---
    private long equipmentsTotal;
    private long equipmentsActive;
    private long equipmentsMaintenance;
    private long equipmentsInactive;
    private long equipmentsBroken;
    private long equipmentsLost;

    /** Số dòng gán thiết bị vào sân (pitch_equipments). */
    private long pitchEquipmentLinks;

    // --- Mượn thiết bị theo booking sân ---
    private long bookingEquipmentsTotal;
    private long bookingEquipmentsBorrowed;
    private long bookingEquipmentsReturned;
    private long bookingEquipmentsLost;
    private long bookingEquipmentsDamaged;
    /** Dòng mượn cần admin xác nhận biên bản trả. */
    private long bookingEquipmentsAwaitingAdminConfirm;

    private long equipmentBorrowLogsTotal;

    // --- Đánh giá & chat ---
    private long reviewsTotal;
    private long reviewsPending;
    private long reviewsApproved;
    private long reviewsHidden;
    private long reviewMessagesTotal;

    // --- Thông báo (toàn hệ thống) ---
    private long notificationsTotal;
    private long notificationsUnread;

    // --- RBAC ---
    private long rolesTotal;
    private long permissionsTotal;

    // --- AI ---
    private long aiApiKeysTotal;
    private long aiApiKeysActive;
    private long aiChatSessionsTotal;
}

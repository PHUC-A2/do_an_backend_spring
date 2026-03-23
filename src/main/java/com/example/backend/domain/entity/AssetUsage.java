package com.example.backend.domain.entity;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

import com.example.backend.util.SecurityUtil;
import com.example.backend.util.constant.assetusage.AssetUsageStatus;
import com.example.backend.util.constant.assetusage.AssetUsageType;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Đăng ký thuê/mượn tài sản (phòng, kho) — map db.md Bookings nhưng tách bảng/entity khỏi {@link Booking} sân bóng.
 */
@Entity
@Table(name = "asset_usages", indexes = {
        @Index(name = "idx_asset_usage_asset_date", columnList = "asset_id,usage_date"),
        @Index(name = "idx_asset_usage_user", columnList = "user_id"),
        @Index(name = "idx_asset_usage_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class AssetUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "asset_id", nullable = false)
    @JsonIgnore
    private Asset asset;

    @Enumerated(EnumType.STRING)
    @Column(name = "usage_type", nullable = false, length = 32)
    private AssetUsageType usageType;

    /** Ngày sử dụng — cột usage_date (tránh từ khóa SQL {@code date}). */
    @Column(name = "usage_date", nullable = false)
    private LocalDate usageDate;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    @Column(nullable = false, length = 1000)
    private String subject;

    /** SĐT liên hệ của user client (dùng để in biên bản nhận/trả). */
    @Column(length = 32)
    private String contactPhone;

    /** Ghi chú booking (ví dụ: Sinh viên lớp K63...). */
    @Column(length = 1000)
    private String bookingNote;

    /**
     * JSON lưu danh sách thiết bị user chọn mượn kèm booking.
     * Shape: [{deviceId, deviceName, deviceType, quantity, deviceNote?}, ...]
     */
    @Column(columnDefinition = "TEXT")
    private String borrowDevicesJson;

    /** Ghi chú chung tình trạng thiết bị khi mượn/nhận phòng. */
    @Column(length = 1000)
    private String borrowNote;

    /** Xác nhận đã kiểm tra tình trạng thiết bị trước khi gửi yêu cầu nhận/mượn. */
    @Column(nullable = false)
    private boolean borrowConditionAcknowledged = false;

    /** Tùy chọn in/lưu biên bản nhận phòng. */
    @Column(nullable = false)
    private boolean borrowReportPrintOptIn = false;

    /**
     * Xóa khỏi lịch sử của user (soft delete).
     * - Client: ẩn booking khỏi danh sách lịch sử của chính user.
     * - Admin: vẫn thấy booking (vì không xóa khỏi DB).
     */
    @Column(nullable = false)
    private boolean deletedByUser = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private AssetUsageStatus status = AssetUsageStatus.PENDING;

    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;

    @PrePersist
    public void handleBeforeCreate() {
        this.createdBy = SecurityUtil.getCurrentUserLogin().orElse("");
        this.createdAt = Instant.now();
    }

    @PreUpdate
    public void handleBeforeUpdate() {
        this.updatedBy = SecurityUtil.getCurrentUserLogin().orElse("");
        this.updatedAt = Instant.now();
    }
}

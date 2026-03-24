package com.example.backend.domain.entity;

import java.time.Instant;
import java.time.LocalTime;

import com.example.backend.util.SecurityUtil;

import com.example.backend.util.constant.asset.AssetRoomFeeMode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity tài sản (phòng, kho, địa điểm) — map bảng assets theo db.md.
 * Trên client, &quot;Phòng tin học&quot; dùng cùng bảng này: mỗi phòng = một bản ghi Asset (admin tạo tại Quản lý tài sản).
 * Đặt sân bóng là {@code Booking} + {@code Pitch}; đăng ký dùng phòng/tài sản là {@link com.example.backend.domain.entity.AssetUsage}, không gộp vào Booking sân.
 */
@Entity
@Table(name = "assets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Asset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id; // khóa chính, tương đương assetId trong tài liệu

    @Column(nullable = false)
    private String assetName; // tên tài sản (map từ User.name)

    /**
     * Tên người phụ trách phòng (dùng để in biên bản nhận/trả phòng).
     * Admin tự khai khi tạo asset (room).
     */
    private String responsibleName;

    private String location; // vị trí

    private Long capacity; // sức chứa

    /** Khung giờ mở đặt phòng theo ngày (tương tự Pitch). */
    private LocalTime openTime;
    private LocalTime closeTime;
    private boolean open24h = true;

    /**
     * Miễn phí hay có phí khi đăng ký dùng phòng — admin chọn lúc tạo/sửa tài sản; client chỉ hiển thị nhãn.
     * Cột có thể null trên DB cũ — service map ra DTO luôn coi null = FREE.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "room_fee_mode", length = 16)
    private AssetRoomFeeMode roomFeeMode = AssetRoomFeeMode.FREE;

    /** URL ảnh minh họa tài sản (upload qua FileController, cùng cách lưu avatar user) */
    @Column(columnDefinition = "MEDIUMTEXT")
    private String assetsUrl;

    private Instant createdAt; // thời điểm tạo
    private Instant updatedAt; // thời điểm cập nhật
    private String createdBy; // người tạo (email đăng nhập)
    private String updatedBy; // người cập nhật

    @PrePersist
    public void handleBeforeCreate() {
        this.createdBy = SecurityUtil.getCurrentUserLogin().orElse(""); // ghi nhận người tạo
        this.createdAt = Instant.now(); // mốc thời gian tạo
        if (this.roomFeeMode == null) {
            this.roomFeeMode = AssetRoomFeeMode.FREE;
        }
        if (this.open24h) {
            this.openTime = null;
            this.closeTime = null;
        }
    }

    @PreUpdate
    public void handleBeforeUpdate() {
        this.updatedBy = SecurityUtil.getCurrentUserLogin().orElse(""); // ghi nhận người sửa
        this.updatedAt = Instant.now(); // mốc thời gian cập nhật
        if (this.roomFeeMode == null) {
            this.roomFeeMode = AssetRoomFeeMode.FREE;
        }
        if (this.open24h) {
            this.openTime = null;
            this.closeTime = null;
        }
    }
}

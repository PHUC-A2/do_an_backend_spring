package com.example.backend.domain.entity;

import java.time.Instant;

import com.example.backend.util.SecurityUtil;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
 * Lưu ý: đặt chỗ sân bóng là {@code Booking} + {@code Pitch}; nghiệp vụ mượn/phòng tài sản sau này dùng tên miền riêng (vd. AssetUsage), không gộp vào Booking sân.
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

    private String location; // vị trí

    private Long capacity; // sức chứa

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
    }

    @PreUpdate
    public void handleBeforeUpdate() {
        this.updatedBy = SecurityUtil.getCurrentUserLogin().orElse(""); // ghi nhận người sửa
        this.updatedAt = Instant.now(); // mốc thời gian cập nhật
    }
}

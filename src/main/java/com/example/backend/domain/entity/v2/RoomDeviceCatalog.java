package com.example.backend.domain.entity.v2;

import java.time.Instant;

import com.example.backend.util.SecurityUtil;
import com.example.backend.util.constant.v2.devicecatalog.DeviceCatalogStatusEnum;
import com.example.backend.util.constant.v2.devicecatalog.DeviceTypeEnum;
import com.example.backend.util.constant.v2.devicecatalog.MobilityTypeEnum;

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
 * Danh mục loại thiết bị; bảng {@code room_device_catalog_v2}.
 */
@Entity
@Table(name = "room_device_catalog_v2")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class RoomDeviceCatalog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "device_name", nullable = false, length = 255)
    private String deviceName;

    @Enumerated(EnumType.STRING)
    @Column(name = "device_type", nullable = false, length = 32)
    private DeviceTypeEnum deviceType;

    @Enumerated(EnumType.STRING)
    @Column(name = "mobility_type", nullable = false, length = 16)
    private MobilityTypeEnum mobilityType;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "image_url", columnDefinition = "MEDIUMTEXT")
    private String imageUrl;

    @Column(length = 255)
    private String manufacturer;

    @Column(length = 255)
    private String model;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private DeviceCatalogStatusEnum status = DeviceCatalogStatusEnum.ACTIVE;

    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;

    @PrePersist
    public void handleBeforeCreate() {
        this.createdBy = SecurityUtil.getCurrentUserLogin().orElse("");
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    public void handleBeforeUpdate() {
        this.updatedBy = SecurityUtil.getCurrentUserLogin().orElse("");
        this.updatedAt = Instant.now();
    }
}

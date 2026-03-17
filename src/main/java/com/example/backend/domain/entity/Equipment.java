package com.example.backend.domain.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.example.backend.util.SecurityUtil;
import com.example.backend.util.constant.equipment.EquipmentStatusEnum;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "equipments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Equipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false)
    private String name; // tên thiết bị (bóng, áo, nón...)

    private String description; // mô tả

    @Column(nullable = false)
    private Integer totalQuantity; // tổng số lượng

    @Column(nullable = false)
    private Integer availableQuantity; // số lượng có thể cho mượn hiện tại

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price; // giá trị thiết bị (dùng khi tính phí nếu mất/hỏng)

    private String imageUrl; // tên file ảnh, ví dụ: ball.jpg

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EquipmentStatusEnum status = EquipmentStatusEnum.ACTIVE;

    @OneToMany(mappedBy = "equipment", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<BookingEquipment> bookingEquipments = new ArrayList<>();

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

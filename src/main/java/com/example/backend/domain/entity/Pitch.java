package com.example.backend.domain.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalTime;

import com.example.backend.util.SecurityUtil;
import com.example.backend.util.constant.pitch.PitchStatusEnum;
import com.example.backend.util.constant.pitch.PitchTypeEnum;

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
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "pitches")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Pitch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    private PitchTypeEnum pitchType = PitchTypeEnum.SEVEN;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal pricePerHour;

    private String pitchUrl;

    // lấy giờ ko lấy ngày
    private LocalTime openTime;
    private LocalTime closeTime;

    private boolean open24h;

    @Enumerated(EnumType.STRING)
    private PitchStatusEnum status = PitchStatusEnum.ACTIVE;

    private String address;

    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;

    @PrePersist
    public void handleBeforeCreate() {
        this.createdBy = SecurityUtil.getCurrentUserLogin().orElse("");
        this.createdAt = Instant.now(); // tạo ra lúc
    }

    @PreUpdate
    public void handleBeforeUpdate() {
        this.updatedBy = SecurityUtil.getCurrentUserLogin().orElse("");
        this.updatedAt = Instant.now(); // cập nhật lúc
    }
}

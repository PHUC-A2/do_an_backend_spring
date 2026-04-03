package com.example.backend.domain.entity;

import java.math.BigDecimal;
import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "pitch_hourly_prices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PitchHourlyPrice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Quan hệ: một sân có nhiều khung giá theo giờ
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pitch_id", nullable = false)
    private Pitch pitch;

    // Thời gian bắt đầu của khung giờ (áp dụng theo ngày)
    @Column(nullable = false)
    private LocalTime startTime;

    // Thời gian kết thúc của khung giờ (tính theo cơ chế end-exclusive)
    @Column(nullable = false)
    private LocalTime endTime;

    // Giá áp dụng trên mỗi giờ trong khung giờ này
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal pricePerHour;
}


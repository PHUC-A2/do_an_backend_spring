package com.example.backend.domain.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

import com.example.backend.util.SecurityUtil;
import com.example.backend.util.constant.booking.BookingStatusEnum;
import com.example.backend.util.constant.booking.ShirtOptionEnum;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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

@Entity
@Table(name = "bookings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "pitch_id", nullable = false)
    private Pitch pitch;

    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;

    @Column(nullable = false)
    private Long durationMinutes; // thời lượng phút // số phút thuê

    @Enumerated(EnumType.STRING)
    private ShirtOptionEnum shirtOption = ShirtOptionEnum.WITHOUT_PITCH_SHIRT;

    private String contactPhone;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalPrice; // Tổng tiền

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatusEnum status = BookingStatusEnum.ACTIVE; // dùng để xử lý lịch sử đặt sân(thêm,sửa ko cần thêm )

    @Column(nullable = false)
    private Boolean deletedByUser = false;// dùng để xử lý lịch sử đặt sân(thêm,sửa ko cần thêm )

    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;

    // dùng để cập nhật người tạo ra người dùng
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

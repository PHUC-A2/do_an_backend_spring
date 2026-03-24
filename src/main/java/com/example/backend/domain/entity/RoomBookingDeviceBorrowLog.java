package com.example.backend.domain.entity;

import java.time.Instant;

import com.example.backend.util.SecurityUtil;
import com.example.backend.util.constant.equipment.EquipmentBorrowLogTypeEnum;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.PrePersist;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Nhật ký mượn/trả theo dòng thiết bị của rooms — clone 100% từ {@code EquipmentBorrowLog}.
 */
@Entity
@Table(name = "room_device_borrow_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class RoomBookingDeviceBorrowLog {

    /** Khóa chính của log. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    /** Dòng mượn/trả mà log này thuộc về. */
    @ManyToOne(optional = false)
    @JoinColumn(name = "room_booking_device_id", nullable = false)
    private RoomBookingDevice roomBookingDevice;

    /** Loại log: mượn hoặc trả. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private EquipmentBorrowLogTypeEnum logType;

    /** Nội dung ghi chú tại thời điểm mượn/trả. */
    @Column(columnDefinition = "TEXT")
    private String notes;

    /** Tên người thực hiện ghi nhận mượn/trả tại thời điểm log. */
    @Column(length = 200)
    private String actorName;

    /** SĐT người thực hiện tại thời điểm log. */
    @Column(length = 32)
    private String actorPhone;

    /** Thời điểm tạo log. */
    private Instant createdAt;

    /** Tài khoản hệ thống tạo log (nếu có). */
    private String createdBy;

    /** Tự động set createdBy/createdAt khi tạo mới. */
    @PrePersist
    public void handleBeforeCreate() {
        this.createdBy = SecurityUtil.getCurrentUserLogin().orElse("");
        this.createdAt = Instant.now();
    }
}


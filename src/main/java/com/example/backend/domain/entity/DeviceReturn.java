package com.example.backend.domain.entity;

import java.time.Instant;

import com.example.backend.util.SecurityUtil;
import com.example.backend.util.constant.devicereturn.DeviceCondition;
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
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Phiếu trả sau checkout — 1 {@link Checkout} chỉ 1 bản ghi (db.md Returns).
 * Tên class DeviceReturn vì {@code Return} là từ khóa Java.
 */
@Entity
@Table(
        name = "returns",
        uniqueConstraints = @UniqueConstraint(name = "uk_return_checkout", columnNames = "checkout_id"),
        indexes = {
                @Index(name = "idx_return_return_time", columnList = "return_time"),
                @Index(name = "idx_return_device_status", columnList = "device_status")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class DeviceReturn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "checkout_id", nullable = false, unique = true)
    @JsonIgnore
    private Checkout checkout;

    @Column(name = "return_time", nullable = false)
    private Instant returnTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "device_status", nullable = false, length = 32)
    private DeviceCondition deviceStatus;

    // ====== Các field dùng để lập biên bản trả (mirroring booking sân) ======

    /** Trả tốt (tổng) — phụ thuộc vào borrowDevicesJson (phần FE gửi). */
    @Column(name = "quantity_returned_good")
    private Integer quantityReturnedGood = 0;

    /** Mất (tổng) — nếu > 0 thì bắt buộc ký xác nhận. */
    @Column(name = "quantity_lost")
    private Integer quantityLost = 0;

    /** Hỏng (tổng) — nếu > 0 thì bắt buộc ký xác nhận. */
    @Column(name = "quantity_damaged")
    private Integer quantityDamaged = 0;

    /** Họ tên người mượn ký xác nhận khi có mất/hỏng. */
    @Column(name = "borrower_sign_name", length = 256)
    private String borrowerSignName;

    /** Họ tên nhân viên / bên giao nhận ký xác nhận khi có mất/hỏng. */
    @Column(name = "staff_sign_name", length = 256)
    private String staffSignName;

    /** Snapshot tên người trả (ưu tiên input tại thời điểm trả, fallback user). */
    @Column(name = "returner_name_snapshot", length = 256)
    private String returnerNameSnapshot;

    /** Snapshot SĐT người trả (ưu tiên input tại thời điểm trả, fallback contactPhone). */
    @Column(name = "returner_phone_snapshot", length = 64)
    private String returnerPhoneSnapshot;

    /** Snapshot tên người nhận thiết bị tại sân. */
    @Column(name = "receiver_name_snapshot", length = 256)
    private String receiverNameSnapshot;

    /** Snapshot SĐT người nhận thiết bị tại sân. */
    @Column(name = "receiver_phone_snapshot", length = 64)
    private String receiverPhoneSnapshot;

    /** Ghi chú biên bản khi trả (tương tự booking sân). */
    @Column(name = "return_condition_note", length = 2000)
    private String returnConditionNote;

    /** Tùy chọn in/lưu biên bản trả phòng (để admin có thể tra cứu). */
    @Column(name = "return_report_print_opt_in", nullable = false)
    private boolean returnReportPrintOptIn = false;

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

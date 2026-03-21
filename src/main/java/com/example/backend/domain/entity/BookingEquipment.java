package com.example.backend.domain.entity;

import com.example.backend.util.constant.booking.BookingEquipmentStatusEnum;
import com.example.backend.util.constant.equipment.EquipmentMobilityEnum;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "booking_equipments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class BookingEquipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @ManyToOne
    @JoinColumn(name = "equipment_id", nullable = false)
    private Equipment equipment;

    @Column(nullable = false)
    private Integer quantity; // số lượng mượn

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingEquipmentStatusEnum status = BookingEquipmentStatusEnum.BORROWED;

    // Tiền phạt khi mất thiết bị (LOST = quantity × equipment.price)
    @Column(nullable = false, columnDefinition = "BIGINT DEFAULT 0")
    private Long penaltyAmount = 0L;

    // Soft-delete phía client: true = ẩn khỏi danh sách của client, admin vẫn thấy
    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean deletedByClient = false;

    /** Cố định / lưu động — phải khớp cấu hình pitch_equipments. */
    @Enumerated(EnumType.STRING)
    @Column(name = "equipment_mobility", length = 16)
    private EquipmentMobilityEnum equipmentMobility;

    @Column(columnDefinition = "TEXT")
    private String borrowConditionNote;

    @Column(columnDefinition = "TEXT")
    private String returnConditionNote;

    /** Số mảnh trả lại tình trạng tốt (khi đã hoàn tất kiểm đếm). */
    @Column(nullable = false, columnDefinition = "INTEGER DEFAULT 0")
    private Integer quantityReturnedGood = 0;

    /** Số mảnh báo mất. */
    @Column(nullable = false, columnDefinition = "INTEGER DEFAULT 0")
    private Integer quantityLost = 0;

    /** Số mảnh báo hỏng (đã nhận lại nhưng không còn dùng được). */
    @Column(nullable = false, columnDefinition = "INTEGER DEFAULT 0")
    private Integer quantityDamaged = 0;

    /** Người mượn ký xác nhận (bắt buộc khi có mất/hỏng). */
    @Column(length = 120)
    private String borrowerSignName;

    /** Nhân viên / bên giao nhận ký (bắt buộc khi có mất/hỏng). */
    @Column(length = 120)
    private String staffSignName;

    /**
     * Họ tên tài khoản đặt sân (snapshot lúc hoàn tất biên bản) — luôn lưu để in/audit,
     * không phụ thuộc đổi tên user sau này.
     */
    @Column(length = 200)
    private String bookingBorrowerSnapshot;
}

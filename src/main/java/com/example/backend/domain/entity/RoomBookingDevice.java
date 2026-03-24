package com.example.backend.domain.entity;

import java.time.Instant;

import com.example.backend.util.SecurityUtil;
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

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Dòng mượn/trả theo booking phòng (rooms) — clone map 100% từ {@code BookingEquipment}.
 *
 * <p>
 * Mỗi bản ghi tương ứng 1 thiết bị (Device) nằm trong {@code AssetUsage.borrowDevicesJson} của cùng 1 booking (AssetUsage).
 * </p>
 */
@Entity
@Table(name = "room_booking_devices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class RoomBookingDevice {

    /** Khóa chính — ID dòng thiết bị theo booking phòng. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    /** Booking phòng (AssetUsage) mà dòng này thuộc về. */
    @ManyToOne(optional = false)
    @JoinColumn(name = "asset_usage_id", nullable = false)
    @JsonIgnore
    private AssetUsage assetUsage;

    /** Thiết bị thuộc tài sản (Device) được mượn/trả trong booking phòng. */
    @ManyToOne(optional = false)
    @JoinColumn(name = "device_id", nullable = false)
    @JsonIgnore
    private Device device;

    /** Số lượng mượn trên dòng này. */
    @Column(nullable = false)
    private Integer quantity;

    /** Trạng thái dòng mượn/trả — clone từ BookingEquipmentStatusEnum. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private BookingEquipmentStatusEnum status = BookingEquipmentStatusEnum.BORROWED;

    /** Tiền phạt khi mất (rooms hiện chưa có price nên mặc định 0 để giữ contract UI). */
    @Column(nullable = false, columnDefinition = "BIGINT DEFAULT 0")
    private Long penaltyAmount = 0L;

    /** Giá tham chiếu để tính phạt (giữ đúng shape giống BookingEquipment). */
    @Column(nullable = false, columnDefinition = "BIGINT DEFAULT 0")
    private Long equipmentPrice = 0L;

    /**
     * Soft-delete phía client: true = ẩn khỏi danh sách của client, admin vẫn thấy.
     *
     * <p>
     * Ở rooms, hiện tại client xóa soft-delete ở mức booking (deletedByUser).
     * Nhưng để clone y hệt booking sân, dòng vẫn có deletedByClient.
     * </p>
     */
    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean deletedByClient = false;

    /** Cố định / lưu động — clone từ EquipmentMobilityEnum để FE reuse UI và print template. */
    @Enumerated(EnumType.STRING)
    @Column(name = "equipment_mobility", length = 16)
    private EquipmentMobilityEnum equipmentMobility;

    /** Ghi chú biên bản mượn cho riêng dòng thiết bị. */
    @Column(columnDefinition = "TEXT")
    private String borrowConditionNote;

    /** Ghi chú biên bản trả cho riêng dòng thiết bị. */
    @Column(columnDefinition = "TEXT")
    private String returnConditionNote;

    /** Trả tốt (chi tiết) — bắt nguồn từ g. */
    @Column(nullable = false, columnDefinition = "INTEGER DEFAULT 0")
    private Integer quantityReturnedGood = 0;

    /** Trả mất (chi tiết) — bắt nguồn từ l. */
    @Column(nullable = false, columnDefinition = "INTEGER DEFAULT 0")
    private Integer quantityLost = 0;

    /** Trả hỏng (chi tiết) — bắt nguồn từ d. */
    @Column(nullable = false, columnDefinition = "INTEGER DEFAULT 0")
    private Integer quantityDamaged = 0;

    /** Họ tên người mượn ký xác nhận khi có mất/hỏng. */
    @Column(length = 120)
    private String borrowerSignName;

    /** Họ tên nhân viên / bên giao nhận ký xác nhận khi có mất/hỏng. */
    @Column(length = 120)
    private String staffSignName;

    /**
     * Họ tên người đặt (snapshot) — luôn lưu để in/audit,
     * không phụ thuộc đổi tên user sau này.
     */
    @Column(length = 200)
    private String bookingBorrowerSnapshot;

    /** Client đã xác nhận kiểm tra tình trạng trước khi mượn hay chưa (biên bản mượn). */
    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private boolean borrowConditionAcknowledged = true;

    /** Client muốn in/lưu biên bản mượn hay không. */
    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private boolean borrowReportPrintOptIn = true;

    /** Snapshot tên người trả thực tế. */
    @Column(length = 200)
    private String returnerNameSnapshot;

    /** Snapshot SĐT người trả thực tế. */
    @Column(length = 32)
    private String returnerPhoneSnapshot;

    /** Tùy chọn in/lưu biên bản trả tại thời điểm hoàn tất trả. */
    private Boolean returnReportPrintOptIn;

    /** Người nhận thiết bị tại sân (snapshot khi trả). */
    @Column(length = 200)
    private String receiverNameSnapshot;

    /** SĐT người nhận thiết bị tại sân (snapshot khi trả). */
    @Column(length = 32)
    private String receiverPhoneSnapshot;

    /**
     * Admin xác nhận biên bản trả.
     * <p>
     * Khi dòng mới được tạo (status BORROWED) thì trường này không dùng cho confirm.
     * Khi client cập nhật trả xong thì service sẽ set false để admin xác nhận.
     * </p>
     */
    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private boolean returnAdminConfirmed = true;

    /** Thời điểm admin xác nhận trả. */
    private Instant returnAdminConfirmedAt;

    /** Admin xác nhận trả (snapshot login). */
    @Column(length = 200)
    private String returnAdminConfirmedBy;

    /** Hook audit khi client/admin update qua service. */
    public void setReturnAdminConfirmedByCurrentUser() {
        // Ghi snapshot người thực hiện (luồng clone từ booking sân).
        this.returnAdminConfirmedBy = SecurityUtil.getCurrentUserLogin().orElse("");
    }
}


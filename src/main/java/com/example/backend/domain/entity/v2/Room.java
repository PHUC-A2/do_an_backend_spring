package com.example.backend.domain.entity.v2;

import java.time.Instant;

import com.example.backend.util.constant.v2.room.RoomStatusEnum;
import com.example.backend.util.SecurityUtil;

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
 * Thực thể phòng tin học; ánh xạ bảng {@code rooms_v2} (API {@code /api/v2/...}).
 */
@Entity
@Table(name = "rooms_v2")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Room {

    /** Khóa chính, tự tăng. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    /**
     * Tên hiển thị của phòng (ví dụ: Phòng A411).
     * Cột CSDL: {@code room_code} — giữ tên cột cũ để tương thích dữ liệu đã có.
     */
    @Column(name = "room_code", nullable = false, unique = true, length = 128)
    private String roomName;

    /** Tên tòa nhà / khu (ví dụ: Nhà A). */
    @Column(nullable = false)
    private String building;

    /** Số tầng (số nguyên). */
    @Column(nullable = false)
    private Integer floor;

    /** Số phòng trên tầng (ví dụ: 11). */
    @Column(nullable = false)
    private Integer roomNumber;

    /** Sức chứa (số chỗ ngồi hoặc quy ước nội bộ). */
    @Column(nullable = false)
    private Integer capacity;

    /** Mô tả phòng (nội dung dài). */
    @Column(columnDefinition = "TEXT")
    private String description;

    /** Trạng thái vận hành: hoạt động / ngưng / bảo trì. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private RoomStatusEnum status = RoomStatusEnum.ACTIVE;

    /** URL ảnh minh họa phòng (sau khi upload). */
    @Column(columnDefinition = "MEDIUMTEXT")
    private String roomUrl;

    /** Họ tên người phụ trách phòng. */
    private String contactPerson;

    /** Số điện thoại liên hệ khi cần. */
    private String contactPhone;

    /** Nơi lấy / trả chìa khóa (hướng dẫn cho người mượn). */
    private String keyLocation;

    /** Ghi chú thêm: thủ tục mượn, lưu ý đặc biệt. */
    @Column(columnDefinition = "TEXT")
    private String notes;

    /** Thời điểm tạo bản ghi. */
    private Instant createdAt;

    /** Thời điểm cập nhật gần nhất. */
    private Instant updatedAt;

    /** Tài khoản (email/login) thực hiện tạo bản ghi. */
    private String createdBy;

    /** Tài khoản cập nhật lần cuối. */
    private String updatedBy;

    /**
     * Trước khi lưu mới: ghi người tạo và thời gian tạo.
     */
    @PrePersist
    public void handleBeforeCreate() {
        this.createdBy = SecurityUtil.getCurrentUserLogin().orElse("");
        this.createdAt = Instant.now();
    }

    /**
     * Trước khi cập nhật: ghi người sửa và thời gian cập nhật.
     */
    @PreUpdate
    public void handleBeforeUpdate() {
        this.updatedBy = SecurityUtil.getCurrentUserLogin().orElse("");
        this.updatedAt = Instant.now();
    }
}

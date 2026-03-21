package com.example.backend.domain.response.v2;

import java.time.Instant;

import com.example.backend.util.constant.v2.room.RoomStatusEnum;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Phản hồi sau khi cập nhật phòng thành công (kèm audit sửa).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResUpdateRoomDTO {
    /** Khóa chính. */
    private Long id;
    /** Tên hiển thị. */
    private String roomName;
    /** Tòa nhà / khu. */
    private String building;
    /** Số tầng. */
    private Integer floor;
    /** Số phòng. */
    private Integer roomNumber;
    /** Sức chứa. */
    private Integer capacity;
    /** Mô tả. */
    private String description;
    /** Trạng thái. */
    private RoomStatusEnum status;
    /** URL ảnh. */
    private String roomUrl;
    /** Người liên hệ. */
    private String contactPerson;
    /** Điện thoại. */
    private String contactPhone;
    /** Vị trí chìa khóa. */
    private String keyLocation;
    /** Ghi chú. */
    private String notes;
    /** Thời điểm cập nhật gần nhất. */
    private Instant updatedAt;
    /** Tài khoản cập nhật lần cuối. */
    private String updatedBy;
}

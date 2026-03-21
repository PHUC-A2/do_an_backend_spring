package com.example.backend.domain.response.v2;

import java.time.Instant;

import com.example.backend.util.constant.v2.room.RoomStatusEnum;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO phòng đầy đủ: dùng cho chi tiết và từng phần tử trong danh sách có phân trang.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResRoomDTO {
    /** Khóa chính. */
    private Long id;
    /** Tên hiển thị phòng. */
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
    /** Trạng thái vận hành. */
    private RoomStatusEnum status;
    /** URL ảnh. */
    private String roomUrl;
    /** Người liên hệ. */
    private String contactPerson;
    /** Điện thoại liên hệ. */
    private String contactPhone;
    /** Vị trí chìa khóa. */
    private String keyLocation;
    /** Ghi chú. */
    private String notes;
    /** Thời điểm tạo. */
    private Instant createdAt;
    /** Thời điểm cập nhật gần nhất. */
    private Instant updatedAt;
    /** Người tạo. */
    private String createdBy;
    /** Người cập nhật lần cuối. */
    private String updatedBy;
}

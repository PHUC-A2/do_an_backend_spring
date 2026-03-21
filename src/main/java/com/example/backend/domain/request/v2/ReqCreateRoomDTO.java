package com.example.backend.domain.request.v2;

import com.example.backend.util.constant.v2.room.RoomStatusEnum;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Body HTTP tạo phòng tin học (POST {@code /api/v2/admin/rooms}).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReqCreateRoomDTO {

    /** Tên hiển thị phòng (sẽ được chuẩn hóa trim + gộp khoảng trắng). */
    @NotBlank(message = "Tên phòng không được để trống")
    private String roomName;

    /** Tên tòa nhà / khu. */
    @NotBlank(message = "Tên nhà không được để trống")
    private String building;

    /** Số tầng. */
    @NotNull(message = "Số tầng không được để trống")
    private Integer floor;

    /** Số phòng trên tầng. */
    @NotNull(message = "Số phòng không được để trống")
    private Integer roomNumber;

    /** Sức chứa (tối thiểu 1). */
    @NotNull(message = "Sức chứa không được để trống")
    @Min(value = 1, message = "Sức chứa phải ít nhất là 1")
    private Integer capacity;

    /** Mô tả phòng (tùy chọn). */
    private String description;

    /** Trạng thái ban đầu; mặc định {@link RoomStatusEnum#ACTIVE}. */
    private RoomStatusEnum status = RoomStatusEnum.ACTIVE;

    /** URL ảnh phòng sau upload (tùy chọn). */
    private String roomUrl;

    /** Người liên hệ phụ trách. */
    private String contactPerson;
    /** Số điện thoại liên hệ. */
    private String contactPhone;
    /** Nơi lấy/trả chìa khóa. */
    private String keyLocation;
    /** Ghi chú thêm. */
    private String notes;
}

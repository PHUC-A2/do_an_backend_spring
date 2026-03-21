package com.example.backend.domain.request.v2;

import com.example.backend.util.constant.v2.room.RoomStatusEnum;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Body HTTP cập nhật đầy đủ thông tin phòng (PUT {@code /api/v2/admin/rooms/{id}}).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReqUpdateRoomDTO {

    /** Tên hiển thị phòng (chuẩn hóa khi lưu). */
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

    /** Mô tả phòng. */
    private String description;

    /** Trạng thái vận hành (bắt buộc trong PUT). */
    @NotNull(message = "Trạng thái phòng không được để trống")
    private RoomStatusEnum status;

    /** URL ảnh phòng. */
    private String roomUrl;

    /** Người liên hệ. */
    private String contactPerson;
    /** Số điện thoại liên hệ. */
    private String contactPhone;
    /** Nơi lấy/trả chìa khóa. */
    private String keyLocation;
    /** Ghi chú. */
    private String notes;
}

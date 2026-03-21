package com.example.backend.domain.request.v2;

import com.example.backend.util.constant.v2.room.RoomStatusEnum;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Body HTTP chỉ đổi trạng thái phòng (PATCH {@code /api/v2/admin/rooms/{id}/status}).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReqUpdateRoomStatusDTO {

    /** Trạng thái mới: hoạt động / ngưng / bảo trì. */
    @NotNull(message = "Trạng thái phòng không được để trống")
    private RoomStatusEnum status;
}

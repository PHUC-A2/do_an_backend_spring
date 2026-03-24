package com.example.backend.domain.request.asset;

import java.time.LocalTime;

import com.example.backend.util.constant.asset.AssetRoomFeeMode;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Body cập nhật tài sản — cấu trúc tương tự ReqUpdateUserDTO (các field cập nhật được).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReqUpdateAssetDTO {

    @NotBlank(message = "Tên tài sản không được để trống")
    private String assetName; // đổi tên tài sản

    /** Tên người phụ trách phòng/tài sản (tùy chọn). */
    private String responsibleName;

    private String location; // đổi vị trí

    private Long capacity; // đổi sức chứa

    private LocalTime openTime;
    private LocalTime closeTime;
    private boolean open24h;

    /** Miễn phí / có phí — null thì giữ nguyên hoặc FREE khi tạo lần đầu (update luôn gửi từ admin form). */
    private AssetRoomFeeMode roomFeeMode;

    private String assetsUrl; // ảnh tài sản (tùy chọn)
}

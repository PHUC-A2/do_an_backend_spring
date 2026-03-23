package com.example.backend.domain.request.asset;

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

    private String location; // đổi vị trí

    private Long capacity; // đổi sức chứa

    private String assetsUrl; // ảnh tài sản (tùy chọn)
}

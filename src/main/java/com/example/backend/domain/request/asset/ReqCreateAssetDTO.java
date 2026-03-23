package com.example.backend.domain.request.asset;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Body tạo mới tài sản — validate giống tinh thần ReqCreateUserDTO (NotBlank cho trường bắt buộc).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReqCreateAssetDTO {

    @NotBlank(message = "Tên tài sản không được để trống")
    private String assetName; // tên hiển thị, bắt buộc

    /** Tên người phụ trách phòng/tài sản (tùy chọn). */
    private String responsibleName;

    private String location; // vị trí, tùy chọn

    private Long capacity; // sức chứa, tùy chọn

    private String assetsUrl; // đường dẫn ảnh sau upload (tùy chọn)
}

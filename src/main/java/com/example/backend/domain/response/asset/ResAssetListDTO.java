package com.example.backend.domain.response.asset;

import java.time.Instant;
import java.time.LocalTime;

import com.example.backend.util.constant.asset.AssetRoomFeeMode;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Dòng danh sách tài sản — tương đương ResUserListDTO (không có quan hệ phụ).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResAssetListDTO {

    private Long id;
    private String assetName;
    private String responsibleName;
    private String location;
    private Long capacity;
    private LocalTime openTime;
    private LocalTime closeTime;
    private boolean open24h;
    private AssetRoomFeeMode roomFeeMode;
    private String assetsUrl;
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;
}
